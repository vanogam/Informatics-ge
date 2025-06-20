package ge.freeuni.informatics.common.dto;


import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;

import java.util.ArrayList;
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
    Date submissionTime,
    String compilationResult,
    String compilationMessage,
    List<SubmissionTestResult> results
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
                submissionTime,
                null,
                null,
                null
        );

    }

    public static SubmissionDTO toDTO(Submission submission) {
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
            submission.getSubmissionTime(),
            submission.getCompilationResult(),
            submission.getCompilationMessage(),
            submission.getSubmissionTestResults()
        );
    }

    public static Submission fromDTO(SubmissionDTO submissionDTO) {
        Submission submission = new Submission();
        // Submission can't be edited by user, so id should not be set here.
        submission.setLanguage(submissionDTO.language());
        submission.setSubmissionTime(submissionDTO.submissionTime());
        submission.setCompilationResult(submissionDTO.compilationResult());
        submission.setCompilationMessage(submissionDTO.compilationMessage());
        submission.setScore(submissionDTO.score());
        submission.setStatus(submissionDTO.status());
        submission.setCurrentTest(submissionDTO.currentTest());
        submission.setFileName(submissionDTO.fileName());
        return submission;
    }

    public static List<SubmissionDTO> toDTOs(List<Submission> submissions) {
        List<SubmissionDTO> submissionDTOs = new ArrayList<>();
        for (Submission submission : submissions) {
            submissionDTOs.add(toDTO(submission));
        }
        return submissionDTOs;
    }
}