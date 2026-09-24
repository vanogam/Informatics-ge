package ge.freeuni.informatics.server.submission;

import ge.freeuni.informatics.common.dto.UserProblemDTO;
import ge.freeuni.informatics.common.dto.RejudgeResultDTO;
import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.dto.OutputSubmissionDTO;
import ge.freeuni.informatics.common.dto.SubmissionTestResultDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.submission.RejudgeAction;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionKind;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import ge.freeuni.informatics.common.model.submission.TestStatus;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TestKeys;
import ge.freeuni.informatics.common.model.task.Testcase;
import ge.freeuni.informatics.common.model.user.ProblemAttemptStatus;
import ge.freeuni.informatics.judgeintegration.IJudgeIntegration;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.submission.SubmissionJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.repository.task.TestcaseRepository;
import ge.freeuni.informatics.repository.user.SolvedProblemJpaRepository;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.files.FileManager;
import ge.freeuni.informatics.server.task.TaskManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static ge.freeuni.informatics.common.model.contestroom.ContestRoom.GLOBAL_ROOM_ID;

@Service
public class SubmissionManager implements ISubmissionManager {

    /**
     * Ceiling on one re-judge request. Every submission in it means a database write and a burst
     * of Kafka messages, so the cap is what keeps a mis-click from flooding the judge queue.
     */
    private static final int MAX_REJUDGE_BATCH = 200;

    /**
     * Caps on an output upload. One test's answer is a text file; anything far past this is a
     * mistake or an attempt to fill the disk, and the whole upload is held in memory while it is
     * mapped to tests.
     */
    private static final int MAX_OUTPUT_FILE_BYTES = 32 * 1024 * 1024;
    private static final long MAX_OUTPUT_SUBMISSION_BYTES = 256L * 1024 * 1024;

    /** Stored in the NOT NULL language column of a submission that carries no source at all. */
    private static final String OUTPUT_SUBMISSION_LANGUAGE = "OUTPUT";

    /** Shown against a test of an output submission that uploaded no answer for it. */
    private static final String NO_OUTPUT_SUBMITTED_MESSAGE = "No output submitted for this test";

    private static final List<SubmissionStatus> ONGOING_STATUSES = List.of(
            SubmissionStatus.IN_QUEUE,
            SubmissionStatus.COMPILING,
            SubmissionStatus.RUNNING
    );
    private static final Logger log = LoggerFactory.getLogger(SubmissionManager.class);
    private final TestcaseRepository testcaseRepository;

    @Value("${ge.freeuni.informatics.Task.submissionDirectoryAddress}")
    private String submissionDirectory;

    private final SubmissionJpaRepository submissionRepository;

    private final IUserManager userManager;

    private final ContestJpaRepository contestRepository;

    private final IContestRoomManager roomManager;

    private final TaskRepository taskRepository;

    private final IJudgeIntegration judgeIntegration;

    private final TaskManager taskManager;

    private final SolvedProblemJpaRepository solvedProblemRepository;

    private final ContestRoomJpaRepository contestRoomRepository;

    private final FileManager fileManager;

    @Autowired
    public SubmissionManager(SubmissionJpaRepository submissionRepository,
                             IUserManager userManager,
                             ContestJpaRepository contestRepository,
                             IContestRoomManager roomManager,
                             TaskRepository taskRepository,
                             IJudgeIntegration judgeIntegration,
                             TestcaseRepository testcaseRepository,
                             TaskManager taskManager,
                             SolvedProblemJpaRepository solvedProblemRepository,
                             ContestRoomJpaRepository contestRoomRepository,
                             FileManager fileManager) {
        this.submissionRepository = submissionRepository;
        this.userManager = userManager;
        this.contestRepository = contestRepository;
        this.roomManager = roomManager;
        this.taskRepository = taskRepository;
        this.judgeIntegration = judgeIntegration;
        this.testcaseRepository = testcaseRepository;
        this.taskManager = taskManager;
        this.solvedProblemRepository = solvedProblemRepository;
        this.contestRoomRepository = contestRoomRepository;
        this.fileManager = fileManager;
    }

    @Override
    public SubmissionDTO loadFullSubmission(long id) throws InformaticsServerException {
        Submission submission = submissionRepository.getReferenceById(id);

        long currentUserId = userManager.getAuthenticatedUserIdOrAnonymous();
        Contest contest = submission.getContest();
        if (contest.getStatus() == ContestStatus.LIVE) {
            if (submission.getUser().getId() != currentUserId) {
                throw InformaticsServerException.PERMISSION_DENIED;
            }
        } else {
            ContestRoom room = contestRoomRepository.getReferenceById(contest.getRoomId());
            if (!room.isMember(currentUserId)) {
                throw InformaticsServerException.PERMISSION_DENIED;
            }
        }

        try {
            // An output submission's file name is a directory of uploaded answers, not a source
            // file: reading it as text would throw. The names of the files it holds are what the
            // results view shows in place of the code.
            String code = submission.isOutputSubmission()
                    ? String.join("\n", fileManager.listSubmittedOutputs(
                            submission.getTask().getId(), submission.getFileName()))
                    : Files.readString(Path.of(submissionDirectory.replace(":taskId", String.valueOf(submission.getTask().getId())) + "/" + submission.getFileName()));
            List<SubmissionTestResultDTO> testResults = submission.getSubmissionTestResults()
                    .stream()
                    .map(result -> {
                        Testcase tc = testcaseRepository.findFirstByTaskIdAndKey(submission.getTask().getId(), result.getTestKey());
                        return SubmissionTestResultDTO.toDto(result, tc.getInputSnippet(), tc.getOutputSnippet());
                    })
                    .sorted(Comparator.comparing(SubmissionTestResultDTO::testKey, TestKeys.NATURAL_ORDER))
                    .toList();
            return SubmissionDTO.toDTOFull(submission, code, testResults);
        } catch (Exception e) {
            log.error("unexpected exception:", e);
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }
    }

    /**
     * Resolves the effective contest/room and viewer's admin status for a submissions filter, and
     * enforces that the viewer is allowed to see that room at all. Shared by {@link #filter} and
     * {@link #countFilter} so the two can never disagree about which submissions are in scope.
     */
    private record FilterScope(Long contestId, Long roomId, boolean viewerIsAdmin) {}

    private FilterScope resolveFilterScope(Long taskId, Long contestId, Long roomId) throws InformaticsServerException {
        if (contestId == null && roomId == null) {
            roomId = GLOBAL_ROOM_ID;
        }
        if (taskId != null) {
            Task task = taskRepository.getReferenceById(taskId);
            if (contestId == null) {
                contestId = task.getContest().getId();
            } else if (!task.getContest().getId().equals(contestId)) {
                throw new InformaticsServerException("taskNotInContest");
            }
        }
        Contest contest = contestId == null ? null : contestRepository.getReferenceById(contestId);

        if (roomId == null) {
            roomId = contest.getRoomId();
        }
        if (contest != null && !Objects.equals(contest.getRoomId(), roomId)) {
            throw new InformaticsServerException("contestNotInRoom");
        }
        ContestRoom room = roomManager.getRoom(roomId);
        long currentUserId = userManager.getAuthenticatedUserIdOrAnonymous();
        if (!room.isMember(currentUserId)) {
            throw InformaticsServerException.PERMISSION_DENIED;
        }
        return new FilterScope(contestId, roomId, userManager.isAdmin(currentUserId));
    }

    @Override
    public List<SubmissionDTO> filter(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit) throws InformaticsServerException {
        FilterScope scope = resolveFilterScope(taskId, contestId, roomId);
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = 20;
        }
        return submissionRepository.findSubmissions(userId, taskId, scope.contestId(), scope.roomId(), scope.viewerIsAdmin(), offset, limit)
                .stream()
                .map(SubmissionDTO::toDtoLight)
                .toList();
    }

    @Override
    public long countFilter(Long userId, Long taskId, Long contestId, Long roomId) throws InformaticsServerException {
        FilterScope scope = resolveFilterScope(taskId, contestId, roomId);
        return submissionRepository.countSubmissions(userId, taskId, scope.contestId(), scope.roomId(), scope.viewerIsAdmin());
    }

    @Override
    @Transactional
    public Long addSubmission(SubmissionDTO submissionDTO) throws InformaticsServerException {
        return addSubmission(submissionDTO, List.of());
    }

    /**
     * @param knownResults verdicts already decided before judging starts. An output submission
     *                     records a zero for every test it uploaded no answer for; the judge then
     *                     treats those tests as done and never sends them to a worker.
     */
    private Long addSubmission(SubmissionDTO submissionDTO, List<SubmissionTestResult> knownResults)
            throws InformaticsServerException {
        Submission submission = SubmissionDTO.fromDTO(submissionDTO);
        long userId = userManager.getAuthenticatedUser().id();
        submission.setUser(userManager.getUser(userId));
        Task task = taskRepository.getReferenceById(submissionDTO.taskId());
        if (submission.getKind() == SubmissionKind.SOURCE && !task.isCodeSubmissionAllowed()) {
            throw InformaticsServerException.CODE_SUBMISSION_NOT_ALLOWED;
        }
        Contest contest = task.getContest();
        requireSubmissionsOpen(contest, userId);
        submission.setRoomId(contest.getRoomId());
        submission.setStatus(SubmissionStatus.IN_QUEUE);
        submission.setTask(task);
        submission.setContest(contest);
        submission.setSubmissionTestResults(new ArrayList<>(knownResults));
        submission = submissionRepository.save(submission);

        log.info("Submission {} created for task {} by user {}, queueing for judging", submission.getId(), task.getId(), userId);
        judgeIntegration.addSubmission(task, submission);
        log.info("Submission {} handed off to judge integration", submission.getId());

        return submission.getId();
    }

    /**
     * The window in which a contestant may submit at all: a contest that is running, or one that
     * has been opened for upsolving. Shared by both submission kinds so that a route added later
     * cannot quietly skip it.
     */
    private void requireSubmissionsOpen(Contest contest, long userId) throws InformaticsServerException {
        if (contest.getStatus() != ContestStatus.LIVE && !contest.isUpsolving()) {
            throw InformaticsServerException.CONTEST_NOT_LIVE;
        }
        if (contest.getStatus() == ContestStatus.LIVE
                && contest.getParticipants().stream().noneMatch(u -> u.getId() == userId)) {
            throw InformaticsServerException.NOT_REGISTERED;
        }
    }

    @Override
    @Transactional
    public OutputSubmissionDTO addOutputSubmission(long taskId, String uploadName, byte[] content, String testKey)
            throws InformaticsServerException {
        Task task = taskRepository.getReferenceById(taskId);
        if (!task.isOutputSubmissionAllowed()) {
            throw InformaticsServerException.OUTPUT_SUBMISSION_NOT_ALLOWED;
        }
        requireSubmissionsOpen(task.getContest(), userManager.getAuthenticatedUser().id());

        List<String> testKeys = task.getTestcases() == null ? List.of() : task.getTestcases().stream()
                .map(Testcase::getKey)
                .toList();
        List<String> unmatched = new ArrayList<>();
        Map<String, byte[]> outputs = isZip(uploadName, content)
                ? readOutputZip(content, task, testKeys, unmatched)
                : readSingleOutput(uploadName, content, task, testKeys, testKey, unmatched);

        if (outputs.isEmpty()) {
            throw InformaticsServerException.NO_MATCHING_OUTPUTS;
        }

        Date submissionTime = new Date();
        String directory = fileManager.saveOutputSubmission(taskId, submissionTime, outputs);
        SubmissionDTO submissionDTO = new SubmissionDTO(
                // The language column is NOT NULL and no CodeLanguage applies; the literal keeps
                // the row honest, and SubmissionKind is what any code branches on.
                OUTPUT_SUBMISSION_LANGUAGE,
                SubmissionKind.OUTPUT,
                userManager.getAuthenticatedUser().username(),
                task.getContest().getId(),
                taskId,
                submissionTime,
                directory
        );
        return new OutputSubmissionDTO(
                addSubmission(submissionDTO, missingOutputResults(testKeys, outputs.keySet())),
                outputs.size(),
                testKeys.size(),
                unmatched
        );
    }

    /**
     * A zero for every test the upload did not answer.
     *
     * <p>A partial upload is the normal case for an output-only task - the contestant solves one
     * test at a time - and an unanswered test is simply wrong, not an error. Recording the
     * verdict up front is also what lets the score be computed over the task's whole test list,
     * which GROUP_MIN needs in order to line its groups up.
     */
    private static List<SubmissionTestResult> missingOutputResults(List<String> testKeys, Set<String> answered) {
        List<SubmissionTestResult> results = new ArrayList<>();
        for (String key : testKeys) {
            if (answered.contains(key)) {
                continue;
            }
            SubmissionTestResult result = new SubmissionTestResult();
            result.setTestKey(key);
            result.setScore(0f);
            result.setTestStatus(TestStatus.WRONG_ANSWER);
            result.setMessage(NO_OUTPUT_SUBMITTED_MESSAGE);
            result.setTime(0);
            result.setMemory(0);
            results.add(result);
        }
        return results;
    }

    private static boolean isZip(String uploadName, byte[] content) {
        // Contestants rename files; the local file header signature is what actually decides.
        return content.length >= 2 && content[0] == 'P' && content[1] == 'K'
                || uploadName != null && uploadName.toLowerCase(Locale.ROOT).endsWith(".zip");
    }

    /**
     * Reads a zip of output files, keeping the ones that map to a test of this task.
     *
     * <p>Only each entry's file name is ever used - never its path - so a zip carrying
     * {@code ../../etc/passwd} simply fails to match a test and is reported back as unmatched.
     */
    private Map<String, byte[]> readOutputZip(byte[] content, Task task, List<String> testKeys, List<String> unmatched)
            throws InformaticsServerException {
        Map<String, byte[]> outputs = new LinkedHashMap<>();
        long totalBytes = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = Path.of(entry.getName()).getFileName().toString();
                String key = matchTestKey(name, task, testKeys);
                if (key == null) {
                    unmatched.add(name);
                    continue;
                }
                byte[] data = readCapped(zip);
                totalBytes += data.length;
                if (totalBytes > MAX_OUTPUT_SUBMISSION_BYTES) {
                    throw InformaticsServerException.OUTPUT_TOO_LARGE;
                }
                outputs.put(key, data);
            }
        } catch (IOException e) {
            log.error("Could not read the uploaded output archive", e);
            throw InformaticsServerException.NO_MATCHING_OUTPUTS;
        }
        return outputs;
    }

    private Map<String, byte[]> readSingleOutput(String uploadName,
                                                 byte[] content,
                                                 Task task,
                                                 List<String> testKeys,
                                                 String testKey,
                                                 List<String> unmatched) throws InformaticsServerException {
        if (content.length > MAX_OUTPUT_FILE_BYTES) {
            throw InformaticsServerException.OUTPUT_TOO_LARGE;
        }
        // An explicit test key wins: a contestant uploading one file has been asked which test it
        // answers, and the name it happens to carry locally means nothing.
        String key = testKey != null && !testKey.isBlank()
                ? (testKeys.contains(testKey) ? testKey : null)
                : matchTestKey(uploadName == null ? "" : Path.of(uploadName).getFileName().toString(), task, testKeys);
        if (key == null) {
            unmatched.add(uploadName);
            return Map.of();
        }
        return Map.of(key, content);
    }

    /**
     * The test a file answers: by the task's output template first, then by the file name simply
     * containing a test key - which is how the IOI convention {@code output_00-01.txt} resolves
     * to test {@code 00-01} without the task having to be re-configured.
     */
    private static String matchTestKey(String fileName, Task task, List<String> testKeys) {
        String fromTemplate = TestKeys.fromTemplate(fileName, task.getOutputTemplate());
        if (fromTemplate != null && testKeys.contains(fromTemplate)) {
            return fromTemplate;
        }
        if (testKeys.contains(fileName)) {
            return fileName;
        }
        // Longest first, so "1-10" is not decided by the "1-1" that is also a substring of it.
        return testKeys.stream()
                .filter(key -> fileName.contains(key))
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
    }

    private static byte[] readCapped(InputStream in) throws IOException, InformaticsServerException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) > 0) {
            out.write(buffer, 0, read);
            if (out.size() > MAX_OUTPUT_FILE_BYTES) {
                // Read incrementally rather than trusting the entry's declared size, which a
                // hand-built archive can understate.
                throw InformaticsServerException.OUTPUT_TOO_LARGE;
            }
        }
        return out.toByteArray();
    }


    @Override
    public List<UserProblemDTO> getUserProblems(Long userId, ProblemAttemptStatus status) throws InformaticsServerException {
        return solvedProblemRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(solvedProblem -> {
                    Task task = solvedProblem.getTask();
                    Contest contest = task.getContest();
                    return new UserProblemDTO(
                            task.getId(),
                            task.getTitle(),
                            contest.getName(),
                            solvedProblem.getLastAttemptAt()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Deliberately not transactional: each submission is re-judged in its own transaction, and a
     * rollback spanning the batch could undo rows whose messages have already reached a worker.
     */
    @Override
    public List<RejudgeResultDTO> rejudge(List<Long> submissionIds, RejudgeAction action)
            throws InformaticsServerException {
        if (action == null || submissionIds == null || submissionIds.isEmpty()) {
            throw InformaticsServerException.INVALID_REJUDGE_REQUEST;
        }
        // Distinct, order preserved: re-judging the same submission twice in one request would
        // abandon the run the first pass just started.
        List<Long> ids = submissionIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty() || ids.size() > MAX_REJUDGE_BATCH) {
            throw InformaticsServerException.INVALID_REJUDGE_REQUEST;
        }
        log.info("Re-judging {} submission(s) with action {}", ids.size(), action);
        return ids.stream().map(id -> judgeIntegration.rejudge(id, action)).toList();
    }
}
