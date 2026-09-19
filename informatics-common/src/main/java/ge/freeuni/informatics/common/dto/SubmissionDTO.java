package ge.freeuni.informatics.common.dto;


import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionKind;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskScoreType;

import java.util.Date;
import java.util.List;

public record SubmissionDTO(
    long id,
    String username,
    SubmissionStatus status,
    Integer currentTest,
    Float score,
    /**
     * The submission's per-subtask awards, encoded by
     * {@link ge.freeuni.informatics.common.model.submission.SubtaskScores}. Null when the
     * submission has no breakdown - it never compiled, or the task has too many scoring units
     * to track.
     *
     * <p>Carried only by {@link #toDTOFull}: a breakdown is worth showing when one submission is
     * open, and a list of submissions shows totals, so sending it on every row of a list would be
     * payload nobody reads.
     */
    String subtaskScores,
    Float maxScore,
    long taskId,
    long contestId,
    String taskName,
    String contestName,
    String language,
    /** Source code or uploaded outputs; see {@link SubmissionKind}. */
    SubmissionKind kind,
    String fileName,
    String text,
    Date submissionTime,
    Long time,
    Integer memory,
    String compilationMessage,
    /**
     * Scoring shape of the task, so the results view can group tests under their subtasks
     * exactly as GROUP_MIN scores them.
     */
    TaskScoreType taskScoreType,
    String taskScoreParameter,
    List<SubmissionTestResultDTO> results
) {
    /** A submission being created: everything the judge fills in later is still absent. */
    public SubmissionDTO(
            String language,
            SubmissionKind kind,
            String username,
            long contestId,
            long taskId,
            Date submissionTime,
            String fileName) {
        this(
                0,
                username,
                null,
                null,
                null,
                null,
                null,
                taskId,
                contestId,
                null,
                null,
                language,
                kind,
                fileName,
                null,
                submissionTime,
                null,
                null,
                null,
                null,
                null,
                null
        );

    }

    public static SubmissionDTO toDtoLight(Submission submission) {
        return new SubmissionDTO(
            submission.getId(),
            submission.getUser().getUsername(),
            submission.getStatus(),
            submission.getCurrentTest(),
            submission.getScore(),
            // A list row shows the total; the breakdown belongs to the open submission.
            null,
            computeMaxScore(submission.getTask()),
            submission.getTask().getId(),
            submission.getContest().getId(),
            submission.getTask().getTitle(),
            submission.getContest().getName(),
            submission.getLanguage(),
            submission.getKind(),
            null,
            null,
            submission.getSubmissionTime(),
            submission.getTime(),
            submission.getMemory(),
            submission.getCompilationMessage(),
            submission.getTask().getTaskScoreType(),
            submission.getTask().getTaskScoreParameter(),
            null
        );
    }

    public static SubmissionDTO toDTOFull(Submission submission, String code, List<SubmissionTestResultDTO> results) {
        return new SubmissionDTO(
            submission.getId(),
            submission.getUser().getUsername(),
            submission.getStatus(),
            submission.getCurrentTest(),
            submission.getScore(),
            submission.getSubtaskScores(),
            computeMaxScore(submission.getTask()),
            submission.getTask().getId(),
            submission.getContest().getId(),
            submission.getTask().getTitle(),
            submission.getContest().getName(),
            submission.getLanguage(),
            submission.getKind(),
            null,
            code,
            submission.getSubmissionTime(),
            submission.getTime(),
            submission.getMemory(),
            submission.getCompilationMessage(),
            submission.getTask().getTaskScoreType(),
            submission.getTask().getTaskScoreParameter(),
            results
        );
    }

    public static Submission fromDTO(SubmissionDTO submissionDTO) {
        Submission submission = new Submission();
        // Submission can't be edited by user, so id should not be set here.
        submission.setLanguage(submissionDTO.language());
        submission.setKind(submissionDTO.kind() == null ? SubmissionKind.SOURCE : submissionDTO.kind());
        submission.setSubmissionTime(submissionDTO.submissionTime());
        submission.setCompilationMessage(submissionDTO.compilationMessage());
        submission.setScore(submissionDTO.score());
        // Not copied: a submission being created has no breakdown, and the judge is what writes
        // one once the submission has been scored.
        submission.setStatus(submissionDTO.status());
        submission.setCurrentTest(submissionDTO.currentTest());
        submission.setFileName(submissionDTO.fileName());
        return submission;
    }

    private static Float computeMaxScore(Task task) {
        if (task == null || task.getTaskScoreType() == null || task.getTaskScoreParameter() == null) return null;
        try {
            int testcaseCount = task.getTestcases() != null ? task.getTestcases().size() : 0;
            return task.getTaskScoreType().computeMaxScore(task.getTaskScoreParameter(), testcaseCount);
        } catch (Exception e) {
            return null;
        }
    }
}
