package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import ge.freeuni.informatics.common.model.submission.TestStatus;

public record SubmissionTestResultDTO (
        Float score,
        String testKey,
        String input,
        String outcome,
        String correctOutput,
        String message,
        Integer time,
        Integer memory,
        TestStatus testStatus
){
    public static SubmissionTestResultDTO toDto(SubmissionTestResult submissionTestResult, String input, String correctOutput) {
        return new SubmissionTestResultDTO(
                submissionTestResult.getScore(),
                submissionTestResult.getTestKey(),
                input,
                submissionTestResult.getOutcome(),
                correctOutput,
                submissionTestResult.getMessage(),
                submissionTestResult.getTime(),
                submissionTestResult.getMemory(),
                submissionTestResult.getTestStatus()
        );
    }
}
