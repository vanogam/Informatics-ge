package ge.freeuni.informatics.server.plagiarism;

import de.jplag.JPlag;
import de.jplag.JPlagComparison;
import de.jplag.JPlagResult;
import de.jplag.Language;
import de.jplag.cpp.CPPLanguage;
import de.jplag.options.JPlagOptions;
import de.jplag.python3.PythonLanguage;
import ge.freeuni.informatics.common.dto.PlagiarismComparisonDTO;
import ge.freeuni.informatics.common.dto.PlagiarismComparisonDetailDTO;
import ge.freeuni.informatics.common.dto.PlagiarismJobDetailDTO;
import ge.freeuni.informatics.common.dto.PlagiarismJobSummaryDTO;
import ge.freeuni.informatics.common.dto.PlagiarismRunDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.plagiarism.PlagiarismComparison;
import ge.freeuni.informatics.common.model.plagiarism.PlagiarismJob;
import ge.freeuni.informatics.common.model.plagiarism.PlagiarismJobStatus;
import ge.freeuni.informatics.common.model.plagiarism.PlagiarismRun;
import ge.freeuni.informatics.common.model.plagiarism.PlagiarismRunStatus;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.plagiarism.PlagiarismComparisonRepository;
import ge.freeuni.informatics.repository.plagiarism.PlagiarismJobRepository;
import ge.freeuni.informatics.repository.plagiarism.PlagiarismRunRepository;
import ge.freeuni.informatics.repository.submission.SubmissionJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.server.files.FileManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class PlagiarismManager implements IPlagiarismManager {

    private static final Logger log = LoggerFactory.getLogger(PlagiarismManager.class);

    /** Matches the {@code VARCHAR(1000)} error-message columns on both tables. */
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private final PlagiarismJobRepository jobRepository;
    private final PlagiarismRunRepository runRepository;
    private final PlagiarismComparisonRepository comparisonRepository;
    private final SubmissionJpaRepository submissionRepository;
    private final ContestJpaRepository contestRepository;
    private final TaskRepository taskRepository;
    private final IUserManager userManager;
    private final FileManager fileManager;
    private final ExecutorService plagiarismExecutor;

    @Autowired
    public PlagiarismManager(PlagiarismJobRepository jobRepository,
                             PlagiarismRunRepository runRepository,
                             PlagiarismComparisonRepository comparisonRepository,
                             SubmissionJpaRepository submissionRepository,
                             ContestJpaRepository contestRepository,
                             TaskRepository taskRepository,
                             IUserManager userManager,
                             FileManager fileManager,
                             ExecutorService plagiarismExecutor) {
        this.jobRepository = jobRepository;
        this.runRepository = runRepository;
        this.comparisonRepository = comparisonRepository;
        this.submissionRepository = submissionRepository;
        this.contestRepository = contestRepository;
        this.taskRepository = taskRepository;
        this.userManager = userManager;
        this.fileManager = fileManager;
        this.plagiarismExecutor = plagiarismExecutor;
    }

    @Override
    public long startJob(Long contestId, Long taskId, List<String> usernames) throws InformaticsServerException {
        if ((contestId == null) == (taskId == null)) {
            throw InformaticsServerException.INVALID_PLAGIARISM_SCOPE;
        }
        PlagiarismJob job = new PlagiarismJob();
        job.setCreatedByUserId(userManager.getAuthenticatedUser().id());
        job.setContestId(contestId);
        job.setTaskId(taskId);
        job.setUsernames(usernames == null || usernames.isEmpty() ? null : String.join(",", usernames));
        job.setStatus(PlagiarismJobStatus.PENDING);
        job.setCreatedAt(new Date());
        job = jobRepository.save(job);

        long jobId = job.getId();
        log.info("Plagiarism job {} queued (contestId={}, taskId={}, usernames={})", jobId, contestId, taskId, job.getUsernames());
        plagiarismExecutor.submit(() -> runJob(jobId));
        return jobId;
    }

    /**
     * Runs on {@link #plagiarismExecutor}, off the request thread. Every repository call here
     * opens and commits its own transaction - nothing touches a lazy association afterwards - so
     * no session needs to be held open across the whole job.
     */
    private void runJob(long jobId) {
        PlagiarismJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("Plagiarism job {} vanished before it could run", jobId);
            return;
        }
        job.setStatus(PlagiarismJobStatus.RUNNING);
        jobRepository.save(job);

        try {
            List<Task> tasks = resolveTasks(job);
            List<String> usernames = parseUsernames(job);
            for (Task task : tasks) {
                runTask(job, task, usernames);
            }
            job.setStatus(PlagiarismJobStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Plagiarism job {} failed", jobId, e);
            job.setStatus(PlagiarismJobStatus.FAILED);
            job.setErrorMessage(truncate(e.getMessage()));
        } finally {
            job.setFinishedAt(new Date());
            jobRepository.save(job);
        }
    }

    private List<Task> resolveTasks(PlagiarismJob job) throws InformaticsServerException {
        if (job.getTaskId() != null) {
            return List.of(taskRepository.findById(job.getTaskId())
                    .orElseThrow(() -> InformaticsServerException.TASK_NOT_FOUND));
        }
        return taskRepository.findByContestId(job.getContestId());
    }

    private static List<String> parseUsernames(PlagiarismJob job) {
        return job.getUsernames() == null || job.getUsernames().isBlank()
                ? null
                : Arrays.asList(job.getUsernames().split(","));
    }

    /**
     * One task's share of the job: picks each selected user's best submission, groups by
     * language, and runs one {@link PlagiarismRun} per language group with at least two
     * submissions - JPlag can only ever compare submissions written in the same language.
     */
    private void runTask(PlagiarismJob job, Task task, List<String> usernames) {
        List<Submission> candidates = submissionRepository.findSourceSubmissionsForPlagiarism(task.getId(), usernames);
        Map<String, List<Submission>> byLanguage = groupComparableSubmissionsByLanguage(candidates);

        for (Map.Entry<String, List<Submission>> entry : byLanguage.entrySet()) {
            runLanguageGroup(job, task, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Picks each user's best (then latest) submission out of {@code candidates} and groups the
     * result by language, keeping only groups with at least two submissions - the smallest set
     * JPlag could ever find a match in. Package-private and side-effect-free so it can be tested
     * without a database or JPlag itself.
     */
    static Map<String, List<Submission>> groupComparableSubmissionsByLanguage(List<Submission> candidates) {
        // The query behind `candidates` orders each user's rows best-then-latest first, so keeping
        // only the first row seen per user id is exactly "that user's best, latest-tiebreak
        // submission" - no window function needed.
        Map<Long, Submission> bestByUser = new LinkedHashMap<>();
        for (Submission submission : candidates) {
            bestByUser.putIfAbsent(submission.getUser().getId(), submission);
        }

        Map<String, List<Submission>> byLanguage = bestByUser.values().stream()
                .collect(Collectors.groupingBy(Submission::getLanguage, LinkedHashMap::new, Collectors.toList()));
        byLanguage.values().removeIf(submissions -> submissions.size() < 2);
        return byLanguage;
    }

    private void runLanguageGroup(PlagiarismJob job, Task task, String language, List<Submission> submissions) {
        Language jplagLanguage = jplagLanguageFor(language);
        if (jplagLanguage == null) {
            // Not a language JPlag knows how to parse (e.g. a stray OUTPUT-kind row) - nothing to run.
            return;
        }

        PlagiarismRun run = new PlagiarismRun();
        run.setJobId(job.getId());
        run.setTaskId(task.getId());
        run.setLanguage(language);
        run.setStatus(PlagiarismRunStatus.RUNNING);
        run.setSubmissionCount(submissions.size());
        run.setStartedAt(new Date());
        run = runRepository.save(run);

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("plagiarism-run-" + run.getId() + "-");
            Map<String, Submission> submissionByDirName = new HashMap<>();
            String suffix = "." + CodeLanguage.valueOf(language).getSuffix();
            for (Submission submission : submissions) {
                String dirName = submission.getId() + "_" + submission.getUser().getUsername();
                Path submissionDir = tempDir.resolve(dirName);
                Files.createDirectories(submissionDir);
                String code = fileManager.readSourceFile(task.getId(), submission.getFileName());
                Files.writeString(submissionDir.resolve("solution" + suffix), code);
                submissionByDirName.put(dirName, submission);
            }

            JPlagOptions options = new JPlagOptions(jplagLanguage, Set.of(tempDir.toFile()), Set.of());
            JPlagResult result = JPlag.run(options);

            List<PlagiarismComparison> comparisons = new ArrayList<>();
            for (JPlagComparison comparison : result.getAllComparisons()) {
                Submission a = submissionByDirName.get(comparison.firstSubmission().getName());
                Submission b = submissionByDirName.get(comparison.secondSubmission().getName());
                if (a == null || b == null) {
                    continue;
                }
                PlagiarismComparison entity = new PlagiarismComparison();
                entity.setRunId(run.getId());
                entity.setSubmissionAId(a.getId());
                entity.setSubmissionBId(b.getId());
                entity.setUsernameA(a.getUser().getUsername());
                entity.setUsernameB(b.getUser().getUsername());
                entity.setSimilarity((float) comparison.similarity());
                comparisons.add(entity);
            }
            comparisonRepository.saveAll(comparisons);

            run.setStatus(PlagiarismRunStatus.COMPLETED);
            run.setComparisonCount(comparisons.size());
        } catch (Exception e) {
            log.error("Plagiarism run {} (task {}, language {}) failed", run.getId(), task.getId(), language, e);
            run.setStatus(PlagiarismRunStatus.FAILED);
            run.setErrorMessage(truncate(e.getMessage()));
        } finally {
            run.setFinishedAt(new Date());
            runRepository.save(run);
            if (tempDir != null) {
                deleteRecursively(tempDir);
            }
        }
    }

    private static Language jplagLanguageFor(String language) {
        if (language == null) {
            return null;
        }
        return switch (language) {
            case "CPP" -> new CPPLanguage();
            case "PYTHON" -> new PythonLanguage();
            default -> null;
        };
    }

    private static void deleteRecursively(Path dir) {
        try (var paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("Could not delete temporary plagiarism file {}", path, e);
                }
            });
        } catch (IOException e) {
            log.warn("Could not clean up temporary plagiarism directory {}", dir, e);
        }
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > MAX_ERROR_MESSAGE_LENGTH ? message.substring(0, MAX_ERROR_MESSAGE_LENGTH) : message;
    }

    @Override
    public List<PlagiarismJobSummaryDTO> listJobs(Integer offset, Integer limit) {
        int off = offset == null ? 0 : offset;
        int lim = limit == null ? 20 : limit;
        return jobRepository.findAllOrderByCreatedAtDesc(off, lim).stream()
                .map(this::toSummaryDTO)
                .toList();
    }

    @Override
    public long countJobs() {
        return jobRepository.count();
    }

    @Override
    public PlagiarismJobDetailDTO getJob(long jobId) throws InformaticsServerException {
        PlagiarismJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> InformaticsServerException.PLAGIARISM_JOB_NOT_FOUND);
        List<PlagiarismRunDTO> runs = runRepository.findByJobIdOrderByIdAsc(jobId).stream()
                .map(this::toRunDTO)
                .toList();
        return new PlagiarismJobDetailDTO(toSummaryDTO(job), runs);
    }

    @Override
    public List<PlagiarismComparisonDTO> listComparisons(long runId, Integer offset, Integer limit) throws InformaticsServerException {
        if (!runRepository.existsById(runId)) {
            throw InformaticsServerException.PLAGIARISM_RUN_NOT_FOUND;
        }
        int off = offset == null ? 0 : offset;
        int lim = limit == null ? 20 : limit;
        return comparisonRepository.findByRunIdOrderBySimilarityDesc(runId, off, lim).stream()
                .map(c -> new PlagiarismComparisonDTO(c.getId(), c.getUsernameA(), c.getUsernameB(),
                        c.getSimilarity(), c.getSubmissionAId(), c.getSubmissionBId()))
                .toList();
    }

    @Override
    public long countComparisons(long runId) throws InformaticsServerException {
        if (!runRepository.existsById(runId)) {
            throw InformaticsServerException.PLAGIARISM_RUN_NOT_FOUND;
        }
        return comparisonRepository.countByRunId(runId);
    }

    @Override
    public PlagiarismComparisonDetailDTO getComparisonDetail(long comparisonId) throws InformaticsServerException {
        PlagiarismComparison comparison = comparisonRepository.findById(comparisonId)
                .orElseThrow(() -> InformaticsServerException.PLAGIARISM_COMPARISON_NOT_FOUND);
        PlagiarismRun run = runRepository.findById(comparison.getRunId())
                .orElseThrow(() -> InformaticsServerException.PLAGIARISM_RUN_NOT_FOUND);
        Submission a = submissionRepository.findById(comparison.getSubmissionAId())
                .orElseThrow(() -> InformaticsServerException.SUBMISSION_NOT_FOUND);
        Submission b = submissionRepository.findById(comparison.getSubmissionBId())
                .orElseThrow(() -> InformaticsServerException.SUBMISSION_NOT_FOUND);
        String taskTitle = taskRepository.findById(run.getTaskId()).map(Task::getTitle).orElse("?");
        try {
            String codeA = fileManager.readSourceFile(run.getTaskId(), a.getFileName());
            String codeB = fileManager.readSourceFile(run.getTaskId(), b.getFileName());
            return new PlagiarismComparisonDetailDTO(comparison.getId(), taskTitle, run.getLanguage(), comparison.getSimilarity(),
                    comparison.getUsernameA(), codeA, a.getSubmissionTime(),
                    comparison.getUsernameB(), codeB, b.getSubmissionTime());
        } catch (IOException e) {
            log.error("Could not read source for plagiarism comparison {}", comparisonId, e);
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }
    }

    @Override
    public List<String> listEligibleUsers(Long contestId, Long taskId) throws InformaticsServerException {
        if ((contestId == null) == (taskId == null)) {
            throw InformaticsServerException.INVALID_PLAGIARISM_SCOPE;
        }
        List<Task> tasks = taskId != null
                ? List.of(taskRepository.findById(taskId).orElseThrow(() -> InformaticsServerException.TASK_NOT_FOUND))
                : taskRepository.findByContestId(contestId);
        Set<String> usernames = new TreeSet<>();
        for (Task task : tasks) {
            usernames.addAll(submissionRepository.findDistinctUsernamesWithSourceSubmission(task.getId()));
        }
        return new ArrayList<>(usernames);
    }

    private PlagiarismJobSummaryDTO toSummaryDTO(PlagiarismJob job) {
        String scopeType;
        String scopeName;
        if (job.getContestId() != null) {
            scopeType = "CONTEST";
            scopeName = contestRepository.findById(job.getContestId()).map(Contest::getName).orElse("?");
        } else {
            scopeType = "TASK";
            scopeName = taskRepository.findById(job.getTaskId()).map(Task::getTitle).orElse("?");
        }
        List<PlagiarismRun> runs = runRepository.findByJobIdOrderByIdAsc(job.getId());
        long totalComparisons = runs.isEmpty() ? 0 : comparisonRepository.countByRunIdIn(
                runs.stream().map(PlagiarismRun::getId).toList());
        return new PlagiarismJobSummaryDTO(job.getId(), scopeType, scopeName, job.getStatus(), job.getErrorMessage(),
                job.getCreatedAt(), job.getFinishedAt(), runs.size(), totalComparisons);
    }

    private PlagiarismRunDTO toRunDTO(PlagiarismRun run) {
        String taskTitle = taskRepository.findById(run.getTaskId()).map(Task::getTitle).orElse("?");
        return new PlagiarismRunDTO(run.getId(), run.getTaskId(), taskTitle, run.getLanguage(), run.getStatus(),
                run.getSubmissionCount(), run.getComparisonCount(), run.getErrorMessage(),
                run.getStartedAt(), run.getFinishedAt());
    }
}
