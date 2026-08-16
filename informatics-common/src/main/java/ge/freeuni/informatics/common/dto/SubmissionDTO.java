package ge.freeuni.informatics.common.dto;


import ge.freeuni.informatics.common.model.submission.Submission;
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
    Float maxScore,
    long taskId,
    long contestId,
    String taskName,
    String contestName,
    String language,
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
    public SubmissionDTO(
            String language,
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
                taskId,
                contestId,
                null,
                null,
                language,
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
            computeMaxScore(submission.getTask()),
            submission.getTask().getId(),
            submission.getContest().getId(),
            submission.getTask().getTitle(),
            submission.getContest().getName(),
            submission.getLanguage(),
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
            computeMaxScore(submission.getTask()),
            submission.getTask().getId(),
            submission.getContest().getId(),
            submission.getTask().getTitle(),
            submission.getContest().getName(),
            submission.getLanguage(),
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
        submission.setSubmissionTime(submissionDTO.submissionTime());
        submission.setCompilationMessage(submissionDTO.compilationMessage());
        submission.setScore(submissionDTO.score());
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
