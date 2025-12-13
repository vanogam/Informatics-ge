package ge.freeuni.informatics.common.dto;


import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;

import java.util.Date;
import java.util.List;

public record SubmissionDTO(
    long id,
    String username,
    SubmissionStatus status,
    Integer currentTest,
    Float score,
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
}