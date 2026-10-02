package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.plagiarism.PlagiarismRunStatus;

import java.util.Date;

/** One JPlag invocation within a plagiarism job - one task, one language. */
public record PlagiarismRunDTO(
    long id,
    long taskId,
    String taskTitle,
    String language,
    PlagiarismRunStatus status,
    Integer submissionCount,
    Integer comparisonCount,
    String errorMessage,
    Date startedAt,
    Date finishedAt
) {
}
