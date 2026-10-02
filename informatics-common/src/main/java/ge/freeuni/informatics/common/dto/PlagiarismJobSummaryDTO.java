package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.plagiarism.PlagiarismJobStatus;

import java.util.Date;

/**
 * One row of the plagiarism jobs list: who/what the check covered and how it's going, without
 * the per-run detail {@link PlagiarismJobDetailDTO} carries.
 */
public record PlagiarismJobSummaryDTO(
    long id,
    /** "CONTEST" or "TASK" - which of the two the job was scoped to. */
    String scopeType,
    /** The contest's name or the task's title, for display without a second lookup. */
    String scopeName,
    PlagiarismJobStatus status,
    String errorMessage,
    Date createdAt,
    Date finishedAt,
    int runCount,
    long totalComparisons
) {
}
