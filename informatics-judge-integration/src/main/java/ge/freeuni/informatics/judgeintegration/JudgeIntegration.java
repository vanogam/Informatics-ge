package ge.freeuni.informatics.judgeintegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.dto.RejudgeResultDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.customtest.CustomTestRun;
import ge.freeuni.informatics.common.model.submission.RejudgeAction;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionKind;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import ge.freeuni.informatics.common.model.submission.SubtaskScores;
import ge.freeuni.informatics.common.model.submission.TestStatus;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskScoreType;
import ge.freeuni.informatics.common.model.task.TestKeys;
import ge.freeuni.informatics.common.model.task.Testcase;
import ge.freeuni.informatics.judgeintegration.model.KafkaCallback;
import ge.freeuni.informatics.judgeintegration.model.KafkaTask;
import ge.freeuni.informatics.judgeintegration.model.Stage;
import ge.freeuni.informatics.repository.submission.SubmissionJpaRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JudgeIntegration implements IJudgeIntegration{

    @Autowired
    Logger log;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private SubmissionJpaRepository submissionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static final HashMap<Long, TreeMap<String, Integer>> testCompletionMap = new HashMap<>();

    private static final ConcurrentHashMap<Long, Object> submissionLocks = new ConcurrentHashMap<>();

    /**
     * Submissions whose current judging run was started by a re-judge rather than by a contestant.
     * Set when the re-judge is accepted and cleared when the run finishes, so that the event the
     * standings listen on can say which it was.
     *
     * <p>In memory like the maps above, and lost on restart for the same reason: a run still in
     * flight when the core restarts then completes as an ordinary submission, which is exactly
     * today's behaviour and no worse than it.
     */
    private static final Set<Long> rejudgedRuns = ConcurrentHashMap.newKeySet();

    private static final int COMPILATION_MESSAGE_MAX_LENGTH = 1000;
    private static final int TEST_RESULT_MESSAGE_MAX_LENGTH = 1000;
    private static final int TEST_RESULT_OUTCOME_MAX_LENGTH = 1000;
    private static final String TRUNCATION_SUFFIX = "...";

    /**
     * Statuses that mean judging has not reached a conclusion. A submission left in one of these
     * across a restart has no in-memory tracking left and would otherwise sit there forever.
     */
    private static final List<SubmissionStatus> IN_FLIGHT_STATUSES = List.of(
            SubmissionStatus.IN_QUEUE, SubmissionStatus.COMPILING, SubmissionStatus.RUNNING);

    /**
     * Beyond this age an in-flight submission is failed rather than re-judged, so a long-forgotten
     * submission cannot silently re-score a finished contest.
     */
    @Value("${ge.freeuni.informatics.judge.recovery.maxAgeHours:24}")
    private long recoveryMaxAgeHours;

    @Value("${ge.freeuni.informatics.judge.recovery.enabled:true}")
    private boolean recoveryEnabled;

    private String truncateToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        if (maxLength <= TRUNCATION_SUFFIX.length()) {
            return value.substring(0, maxLength);
        }
        return value.substring(0, maxLength - TRUNCATION_SUFFIX.length()) + TRUNCATION_SUFFIX;
    }

    @Override
    public void addSubmission(Task task, Submission submission) throws InformaticsServerException {
        // putIfAbsent, not put: replacing a live lock would leave a callback already inside the
        // synchronized block holding an object nobody else can see, and the mutual exclusion the
        // lock exists for would silently stop working.
        submissionLocks.putIfAbsent(submission.getId(), new Object());
        submission.nextJudgeToken();
        submissionRepository.save(submission);
        if (submission.isOutputSubmission()) {
            // The contestant uploaded the answers themselves: there is no program to build, so
            // judging starts at the tests.
            startOutputJudging(task, submission);
            return;
        }
        publishCompilationMessage(task, submission);
    }

    /**
     * Starts an output submission at the testing stage.
     *
     * <p>The tests it uploaded no output for have already been recorded as zeros by the caller,
     * which is what keeps them out of {@link #outstandingTests} and out of the fan-out: a test
     * with no answer has nothing to run and its verdict is known.
     */
    private void startOutputJudging(Task task, Submission submission) throws InformaticsServerException {
        submission.setStatus(SubmissionStatus.RUNNING);
        submission.setCurrentTest(1);
        submissionRepository.save(submission);

        TreeMap<String, Integer> outstanding = outstandingTests(submission);
        testCompletionMap.put(submission.getId(), outstanding);
        if (outstanding.isEmpty()) {
            // Every test the task has was already answered with a zero, so there is nothing to
            // wait for and the submission can be scored now.
            finalizeSubmission(submission, submission.getCompilationMessage());
            return;
        }
        sendTestMessages(task, submission, outstanding.keySet());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RejudgeResultDTO rejudge(long submissionId, RejudgeAction action) {
        // Its own lock entry, its own transaction: re-judging a hundred submissions must commit a
        // hundred times rather than risk rolling back rows whose Kafka messages already went out.
        Object created = new Object();
        Object existing = submissionLocks.putIfAbsent(submissionId, created);
        boolean lockIsOurs = existing == null;
        Object lock = lockIsOurs ? created : existing;
        try {
            synchronized (lock) {
                Submission submission = submissionRepository.findById(submissionId).orElse(null);
                if (submission == null) {
                    return refuse(submissionId, lockIsOurs, lock, "submissionNotFound");
                }
                Task task = submission.getTask();
                if (task == null) {
                    return refuse(submissionId, lockIsOurs, lock, "taskNotFound");
                }
                // A submission that is still in flight is not refused: one that is stuck - its
                // worker died, its callback was lost - is exactly what these actions exist to
                // rescue, and stuck submissions never leave the in-flight statuses. Each action
                // moves the submission onto a new judge token, so whatever the abandoned run
                // still reports is discarded on arrival instead of mixing into the new verdict.
                if (submission.isOutputSubmission() && action != RejudgeAction.RESCORE) {
                    // Neither action means anything here. There is no source to compile, and a
                    // re-run would re-judge every testcase - including the ones the contestant
                    // uploaded no answer for, whose zeros were recorded up front precisely so
                    // that they would never be sent to a worker. Scoring the results it already
                    // has is the only re-judge an output submission has a use for.
                    return refuse(submissionId, lockIsOurs, lock, "cantRecompileOrRerun");
                }
                if (submission.getStatus() == SubmissionStatus.COMPILATION_ERROR
                        && action != RejudgeAction.RECOMPILE) {
                    // There is no binary to run and no result to score. Only a recompile can help.
                    return refuse(submissionId, lockIsOurs, lock, "submissionNotCompiled");
                }
                if (action != RejudgeAction.RESCORE
                        && (task.getTestcases() == null || task.getTestcases().isEmpty())) {
                    return refuse(submissionId, lockIsOurs, lock, "taskHasNoTestcases");
                }

                // Marked before the action runs: RESCORE finalizes the submission inline, so a
                // mark set afterwards would arrive after the event it is meant to describe.
                rejudgedRuns.add(submissionId);
                try {
                    switch (action) {
                        case RECOMPILE -> recompile(task, submission);
                        case RERUN -> rerun(task, submission);
                        case RESCORE -> rescore(submission);
                    }
                } catch (Exception e) {
                    // Nothing was started, so nothing will arrive to clear the mark.
                    rejudgedRuns.remove(submissionId);
                    throw e;
                }
                log.info("Re-judged submission {} with action {}", submissionId, action);
                return RejudgeResultDTO.accepted(submissionId);
            }
        } catch (Exception e) {
            log.error("Failed to re-judge submission {} with action {}", submissionId, action, e);
            return refuse(submissionId, lockIsOurs, lock, "rejudgeFailed");
        }
    }

    /**
     * Releases the lock again when this call was the one that installed it, so that a refusal
     * leaves no entry behind for a later callback to synchronize on with no tracking beside it.
     */
    private RejudgeResultDTO refuse(long submissionId, boolean lockIsOurs, Object lock, String code) {
        if (lockIsOurs) {
            submissionLocks.remove(submissionId, lock);
        }
        log.info("Refusing to re-judge submission {}: {}", submissionId, code);
        return RejudgeResultDTO.refused(submissionId, code);
    }

    /**
     * Starts the whole job again: a new binary built from the task's current graders, then every
     * test, then the score. Whatever the previous run still has in flight is left to arrive and be
     * discarded - it carries the token of a run that no longer exists.
     */
    private void recompile(Task task, Submission submission) throws InformaticsServerException {
        submission.setStatus(SubmissionStatus.IN_QUEUE);
        submission.setCurrentTest(null);
        submission.setCompilationMessage(null);
        submission.setSubmissionTestResults(new ArrayList<>());
        submission.nextJudgeToken();
        submissionRepository.save(submission);
        // Nothing is outstanding until the compilation reports back and publishes the tests.
        testCompletionMap.remove(submission.getId());
        publishCompilationMessage(task, submission);
    }

    /**
     * Scores the submission on the results it already has. Any test still outstanding is abandoned
     * rather than waited for - which is the point when a run is stuck - so the token moves on and a
     * late arrival cannot reopen a submission that has just been scored.
     */
    private void rescore(Submission submission) {
        submission.nextJudgeToken();
        testCompletionMap.remove(submission.getId());
        finalizeSubmission(submission, submission.getCompilationMessage());
    }

    /**
     * Re-runs the existing binary over the task's testcases as they stand now. Clearing the results
     * first is what makes {@link #outstandingTests} enumerate all of them again, and is also what
     * drops results for testcases the task no longer has.
     */
    private void rerun(Task task, Submission submission) throws InformaticsServerException {
        submission.setStatus(SubmissionStatus.RUNNING);
        submission.setCurrentTest(1);
        submission.setSubmissionTestResults(new ArrayList<>());
        submission.nextJudgeToken();
        submissionRepository.save(submission);
        sendTestMessages(task, submission);
    }

    /**
     * Publishes the message that starts a submission's compilation. Split out from
     * {@link #addSubmission} so that a re-judge, which already holds the submission's lock, can
     * send it without touching the lock table.
     */
    private void publishCompilationMessage(Task task, Submission submission) throws InformaticsServerException {
        KafkaTask kafkaTask = new KafkaTask(
                String.valueOf(task.getId()),
                String.valueOf(task.getContest().getId()),
                String.valueOf(submission.getId()),
                String.valueOf(submission.getFileName()),
                submissionLanguage(submission),
                task.getTimeLimitMillis(),
                task.getMemoryLimitMB() * 1024,
                null,
                null,
                null,
                task.getCheckerType(),
                task.getTaskType(),
                numProcesses(task),
                Stage.COMPILATION,
                submission.getJudgeToken(),
                submission.getKind()
        );
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String message = objectMapper.writeValueAsString(kafkaTask);
            log.debug("Publishing compilation message: {}", message);
            kafkaProducerService.sendMessage("submission-topic", message);
            log.info("Queued compilation for submission {}", submission.getId());
        } catch (IOException e) {
            log.error("Failed to serialize compilation kafka message", e);
            throw new InformaticsServerException("serializationError", e);
        }
    }

    @Override
    public void addCustomTest(Task task, CustomTestRun run, CodeLanguage language) throws InformaticsServerException {
        String contestId = task.getContest() != null ? String.valueOf(task.getContest().getId()) : "0";

        KafkaTask kafkaTask = new KafkaTask(
                String.valueOf(task.getId()),
                contestId,
                String.valueOf(run.getId()),
                run.getSubmissionFile(),
                language,
                task.getTimeLimitMillis(),
                task.getMemoryLimitMB() * 1024,
                null,
                null,
                null,
                task.getCheckerType(),
                task.getTaskType(),
                numProcesses(task),
                Stage.COMPILATION,
                null,
                // A custom test is always run against code the contestant just typed.
                SubmissionKind.SOURCE
        );
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String message = objectMapper.writeValueAsString(kafkaTask);
            log.debug("Publishing custom test compilation message: {}", message);
            kafkaProducerService.sendMessage("submission-topic", message);
        } catch (IOException e) {
            log.error("Failed to serialize custom test compilation kafka message", e);
            throw new InformaticsServerException("serializationError", e);
        }
    }

    private void sendTestMessages(Task task, Submission submission) throws InformaticsServerException {
        sendTestMessages(task, submission, null);
    }

    /**
     * @param only test keys to publish, or null for every test of the task
     */
    private void sendTestMessages(Task task, Submission submission, Set<String> only)
            throws InformaticsServerException {
        List<Testcase> testcases = task.getTestcases().stream()
                .sorted(Comparator.comparing(Testcase::getKey))
                .filter(tc -> only == null || only.contains(tc.getKey()))
                .toList();
        // Seeded before publishing, not after: a worker can answer the first test before this
        // method returns, and a callback that finds no tracking has to fall back to rebuilding it.
        if (only == null) {
            testCompletionMap.put(submission.getId(), outstandingTests(submission));
        }
        for (Testcase testcase : testcases) {
            KafkaTask kafkaTask = new KafkaTask(
                    String.valueOf(task.getId()),
                    String.valueOf(task.getContest().getId()),
                    String.valueOf(submission.getId()),
                    String.valueOf(submission.getFileName()),
                    submissionLanguage(submission),
                    task.getTimeLimitMillis(),
                    task.getMemoryLimitMB() * 1024,
                    testcase.getKey(),
                    testcase.getInputFileAddress().substring(testcase.getInputFileAddress().lastIndexOf("/") + 1),
                    testcase.getOutputFileAddress().substring(testcase.getOutputFileAddress().lastIndexOf("/") + 1),
                    task.getCheckerType(),
                    task.getTaskType(),
                    numProcesses(task),
                    Stage.TESTING,
                    submission.getJudgeToken(),
                    submission.getKind()
            );
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String message = objectMapper.writeValueAsString(kafkaTask);
                log.debug("Kafka message: {}", message);
                kafkaProducerService.sendMessage("submission-topic",
                        submission.getId() + ":" + testcase.getKey(), message);
                log.info("Sent submission to Kafka: {}", submission.getId());
            } catch (IOException e) {
                log.error("Failed to serialize KafkaTask: {}", e.getMessage());
                throw new InformaticsServerException("serializationError", e);
            }
        }
    }

    @KafkaListener(topics = "submission-callback", groupId = "core")
    @Transactional
    protected void listenToCompletionTopic(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            KafkaCallback callback = objectMapper.readValue(message, KafkaCallback.class);
            log.info("`Received callback for submission`: {}, {}, {}, worker={}", callback.submissionId(), callback.messageType(), callback.testcaseKey(), callback.workerId());

            // If this callback is not for a regular submission (e.g., custom test run),
            // ignore it here and let other listeners handle it.
            Submission submission = submissionRepository.findById(callback.submissionId()).orElse(null);
            if (submission == null) {
                log.info("No Submission entity found for id {}, skipping in JudgeIntegration", callback.submissionId());
                return;
            }
            if (isStaleJudgeRun(submission, callback)) {
                return;
            }
            if (!submissionLocks.containsKey(submission.getId())) {
                // Tracking lives in memory and is lost on restart. Rebuild it from the database
                // rather than dropping the result the worker just spent time producing.
                if (!restoreTracking(submission)) {
                    return;
                }
            }
            synchronized (submissionLocks.get(submission.getId())) {
                switch (callback.messageType()) {
                    case COMPILATION_STARTED:
                        submission.setStatus(SubmissionStatus.COMPILING);
                        submissionRepository.save(submission);
                        break;
                    case COMPILATION_COMPLETED:
                        submission.setStatus(SubmissionStatus.RUNNING);
                        submission.setCurrentTest(1);
                        submission.setSubmissionTestResults(new java.util.ArrayList<>());
                        submission.setCompilationMessage(truncateToLength(callback.message(), COMPILATION_MESSAGE_MAX_LENGTH));
                        sendTestMessages(submission.getTask(), submission);
                        submissionRepository.save(submission);
                        break;
                    case COMPILATION_FAILED:
                        submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
                        finalizeSubmission(submission, callback);
                        log.error("Compilation failed for submission: {}", submission.getId());
                        break;
                    case SYSTEM_ERROR:
                        submission.setStatus(SubmissionStatus.SYSTEM_ERROR);
                        submission.setCompilationMessage(truncateToLength(callback.message(), COMPILATION_MESSAGE_MAX_LENGTH));
                        finalizeSubmission(submission, callback);
                        log.error("System error for submission: {}, message: {}", submission.getId(), callback.message());
                        break;
                    case TEST_COMPLETED:
                        if (!testCompletionMap.get(submission.getId()).containsKey(callback.testcaseKey())) {
                            return;
                        }
                        
                        // Create and store test result
                        SubmissionTestResult testResult = createTestResult(callback);
                        if (submission.getSubmissionTestResults() == null) {
                            submission.setSubmissionTestResults(new ArrayList<>());
                        }
                        submission.getSubmissionTestResults().add(testResult);
                        
                        testCompletionMap.get(submission.getId()).remove(callback.testcaseKey());
                        if (testCompletionMap.get(submission.getId()).isEmpty()) {
                            finalizeSubmission(submission, callback);
                            return;
                        }
                        submission.setCurrentTest(testCompletionMap.get(submission.getId()).firstEntry().getValue());
                        submissionRepository.save(submission);
                        log.info("Test completed for submission: {}, test case: {}, score: {}, status: {}", 
                                submission.getId(), callback.testcaseKey(), testResult.getScore(), testResult.getTestStatus());
                        break;
                    default:
                }
            }
        } catch (Exception e) {
            log.error("Error while processing submission message", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * True when a callback does not belong to the submission's current judging run - the submission
     * was re-judged while this worker was still busy with the previous one. Those results describe
     * a binary or a testcase set that is no longer current, so recording them would mix two runs
     * into one verdict.
     *
     * <p>A callback carrying no token at all comes from a worker built before this mechanism, and is
     * dropped like any other mismatch. That leaves such a worker unable to finish anything, which is
     * why the log says so outright: the fix is to restart the workers on the current build.
     */
    private boolean isStaleJudgeRun(Submission submission, KafkaCallback callback) {
        Integer current = submission.getJudgeToken();
        Integer reported = callback.judgeToken();
        if (Objects.equals(current, reported)) {
            return false;
        }
        if (reported == null) {
            log.warn("Dropping {} callback for submission {}: the worker reported no judge token, "
                            + "so it is running a build older than the current one and needs restarting",
                    callback.messageType(), submission.getId());
        } else {
            log.info("Dropping {} callback for submission {} from superseded judging run {} (current is {})",
                    callback.messageType(), submission.getId(), reported, current);
        }
        return true;
    }

    /**
     * The language a worker should build and run the submission with - absent for an output
     * submission, whose language column holds a marker rather than a {@link CodeLanguage}.
     */
    private static CodeLanguage submissionLanguage(Submission submission) {
        return submission.isOutputSubmission() ? null : CodeLanguage.valueOf(submission.getLanguage());
    }

    private int numProcesses(Task task) {
        return task.getNumProcesses() == null ? 1 : task.getNumProcesses();
    }


    /**
     * Recreates the in-memory tracking for a submission whose judging predates a restart.
     *
     * @return false when the submission is already finished, so the callback should be ignored
     */
    private boolean restoreTracking(Submission submission) {
        if (!IN_FLIGHT_STATUSES.contains(submission.getStatus())) {
            log.debug("Ignoring callback for already finished submission {}", submission.getId());
            return false;
        }
        submissionLocks.putIfAbsent(submission.getId(), new Object());
        testCompletionMap.put(submission.getId(), outstandingTests(submission));
        log.info("Restored tracking for submission {} after restart, {} test(s) outstanding",
                submission.getId(), testCompletionMap.get(submission.getId()).size());
        return true;
    }

    /**
     * Tests that still owe a result: every testcase of the task minus those already recorded.
     * Judging progress is therefore derivable from the database and never has to be persisted
     * separately. Values are the index used to report which test is currently running.
     */
    private TreeMap<String, Integer> outstandingTests(Submission submission) {
        TreeMap<String, Integer> outstanding = new TreeMap<>();
        Task task = submission.getTask();
        if (task == null || task.getTestcases() == null) {
            return outstanding;
        }
        Set<String> completed = new HashSet<>();
        if (submission.getSubmissionTestResults() != null) {
            submission.getSubmissionTestResults().stream()
                    .map(SubmissionTestResult::getTestKey)
                    .forEach(completed::add);
        }
        List<Testcase> ordered = task.getTestcases().stream()
                .sorted(Comparator.comparing(Testcase::getKey))
                .toList();
        for (int i = 0; i < ordered.size(); i++) {
            String key = ordered.get(i).getKey();
            if (!completed.contains(key)) {
                outstanding.put(key, i);
            }
        }
        return outstanding;
    }

    /**
     * Re-drives submissions that were mid-judgement when the application last stopped. Without
     * this they keep their in-flight status forever: the worker's callbacks are long gone and
     * nothing else ever revisits the row.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void recoverInFlightSubmissions() {
        if (!recoveryEnabled) {
            log.info("Submission recovery disabled, skipping");
            return;
        }
        List<Submission> inFlight = submissionRepository.getAllByStatusIn(IN_FLIGHT_STATUSES);
        if (inFlight.isEmpty()) {
            return;
        }
        log.info("Found {} in-flight submission(s) to recover", inFlight.size());
        long cutoff = System.currentTimeMillis() - recoveryMaxAgeHours * 3600_000L;

        for (Submission submission : inFlight) {
            try {
                recoverSubmission(submission, cutoff);
            } catch (Exception e) {
                log.error("Failed to recover submission {}", submission.getId(), e);
            }
        }
    }

    private void recoverSubmission(Submission submission, long cutoff) throws InformaticsServerException {
        Date submittedAt = submission.getSubmissionTime();
        if (submittedAt != null && submittedAt.getTime() < cutoff) {
            log.warn("Submission {} is older than {}h, failing it instead of re-judging",
                    submission.getId(), recoveryMaxAgeHours);
            submission.setStatus(SubmissionStatus.SYSTEM_ERROR);
            submission.setCompilationMessage("Judging was interrupted and could not be resumed");
            submission.setScore(0f);
            submissionRepository.save(submission);
            return;
        }

        TreeMap<String, Integer> outstanding = outstandingTests(submission);
        submissionLocks.putIfAbsent(submission.getId(), new Object());

        if (submission.getStatus() == SubmissionStatus.RUNNING && !outstanding.isEmpty()) {
            // It compiled, so only the tests that never reported back need re-running.
            log.info("Resuming submission {}: re-queueing {} outstanding test(s)",
                    submission.getId(), outstanding.size());
            testCompletionMap.put(submission.getId(), outstanding);
            sendTestMessages(submission.getTask(), submission, outstanding.keySet());
            return;
        }
        if (submission.getStatus() == SubmissionStatus.RUNNING) {
            // Every test reported; it just never got finalised.
            log.info("Finalising submission {}: all tests already have results", submission.getId());
            testCompletionMap.put(submission.getId(), outstanding);
            finalizeSubmission(submission, submission.getCompilationMessage());
            return;
        }
        // Never got past compilation - start the whole job again.
        log.info("Re-queueing submission {} from compilation", submission.getId());
        submission.setStatus(SubmissionStatus.IN_QUEUE);
        submission.setSubmissionTestResults(new ArrayList<>());
        submissionRepository.save(submission);
        addSubmission(submission.getTask(), submission);
    }


    /**
     * Rounds the total. Each subtask has already been rounded as it was awarded (see
     * {@link TaskScoreType#roundScore}); this clears any residue from summing those.
     */
    static float roundScore(float score) {
        return TaskScoreType.roundScore(score);
    }

    private SubmissionTestResult createTestResult(KafkaCallback callback) {
        SubmissionTestResult testResult = new SubmissionTestResult();
        testResult.setTestKey(callback.testcaseKey());
        testResult.setTestStatus(callback.status());
        testResult.setMessage(truncateToLength(callback.message(), TEST_RESULT_MESSAGE_MAX_LENGTH));
        testResult.setOutcome(truncateToLength(callback.outcome(), TEST_RESULT_OUTCOME_MAX_LENGTH));
        
        // The worker always reports a fraction in [0, 1]; partial scores must survive intact.
        testResult.setScore(callback.score() == null ? 0.0f : callback.score().floatValue());
        
        // Convert time and memory from Long to Integer
        if (callback.timeMillis() != null) {
            testResult.setTime(callback.timeMillis().intValue());
        }
        if (callback.memoryKB() != null) {
            testResult.setMemory(callback.memoryKB().intValue());
        }
        
        return testResult;
    }

    private void finalizeSubmission(Submission submission, KafkaCallback callback) {
        finalizeSubmission(submission, callback == null ? null : callback.message());
    }

    private void finalizeSubmission(Submission submission, String message) {
        // A submission that failed to compile ran no tests, so there is nothing to score: it is
        // zero by definition. Scoring it anyway is not merely wasted work - GROUP_MIN reads the
        // result list positionally and throws on an empty one, which turned a plain compile
        // error into a SYSTEM_ERROR and hid the compiler's message from the contestant.
        if (submission.getStatus() == SubmissionStatus.COMPILATION_ERROR) {
            submission.setScore(0f);
            submission.setSubtaskScores(null);
            submission.setCompilationMessage(truncateToLength(message, COMPILATION_MESSAGE_MAX_LENGTH));
            completeSubmission(submission);
            return;
        }

        // Likewise for a submission that has no results for any other reason - a run abandoned
        // while it was still stuck, most often. Zero, without asking the scorers to read a list
        // that is not there: GROUP_MIN would index past its end and SUM would find its per-test
        // parameter the wrong length, and both would surface as SYSTEM_ERROR.
        if (submission.getSubmissionTestResults() == null || submission.getSubmissionTestResults().isEmpty()) {
            submission.setStatus(SubmissionStatus.FAILED);
            submission.setScore(0f);
            submission.setSubtaskScores(null);
            completeSubmission(submission);
            return;
        }

        float testScoreSum = submission.getSubmissionTestResults().stream().map(SubmissionTestResult::getScore).reduce(0f, (sum, result) -> sum + result);
        // A checker or manager that crashed on even one test taints the whole verdict: the
        // contestant's actual result for that test was never established, so nothing else about
        // this submission (partial credit included) can be trusted either.
        if (submission.getSubmissionTestResults().stream().anyMatch(res -> res.getTestStatus() == TestStatus.SYSTEM_ERROR)) {
            submission.setStatus(SubmissionStatus.SYSTEM_ERROR);
        } else if (testScoreSum == 0f) {
            submission.setStatus(SubmissionStatus.FAILED);
        } else if (submission.getSubmissionTestResults().stream().allMatch(res -> res.getScore() == 1f)) {
            submission.setStatus(SubmissionStatus.CORRECT);
        } else {
            submission.setStatus(SubmissionStatus.PARTIAL);
        }

        float finalScore = 0f;
        List<Float> subtaskAwards = null;
        try {
            // GROUP_MIN slices this list positionally, and results arrive in whatever order the
            // workers finish - which, now that tests are spread across partitions, is arbitrary.
            // Sorting here is what aligns each slice with the subtask it was configured for.
            submission.getSubmissionTestResults()
                    .sort(Comparator.comparing(SubmissionTestResult::getTestKey, TestKeys.NATURAL_ORDER));
            // Scored once, as a vector: the total is its sum, so the per-subtask figures the
            // standings accumulate can never disagree with the score shown beside them.
            subtaskAwards = submission.getTask().getTaskScoreType().evaluateSubtasks(
                    submission.getSubmissionTestResults(), submission.getTask().getTaskScoreParameter());
            finalScore = SubtaskScores.total(subtaskAwards);
        } catch (Exception e) {
            log.error("Error evaluating task score for submission: {}", submission.getId(), e);
            submission.setStatus(SubmissionStatus.SYSTEM_ERROR);
            subtaskAwards = null;
        }
        submission.setScore(roundScore(finalScore));
        submission.setSubtaskScores(SubtaskScores.format(subtaskAwards));
        completeSubmission(submission);
    }

    /**
     * Persists a finished submission and drops the bookkeeping that tracked it while it ran.
     * Every path out of {@link #finalizeSubmission} ends here, so a submission is never left
     * holding a lock or a completion counter.
     */
    private void completeSubmission(Submission submission) {
        submissionRepository.save(submission);
        testCompletionMap.remove(submission.getId());
        submissionLocks.remove(submission.getId());
        boolean rejudged = rejudgedRuns.remove(submission.getId());
        log.info("Submission {} finalized with status: {}{}", submission.getId(), submission.getStatus(),
                rejudged ? " (re-judged)" : "");

        eventPublisher.publishEvent(new SubmissionEvent(submission, rejudged));
    }
}

