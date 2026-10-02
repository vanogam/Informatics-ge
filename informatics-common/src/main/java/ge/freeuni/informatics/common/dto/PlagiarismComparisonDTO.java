package ge.freeuni.informatics.common.dto;

/** One row of a run's similarity-sorted report. */
public record PlagiarismComparisonDTO(
    long id,
    String usernameA,
    String usernameB,
    float similarity,
    long submissionAId,
    long submissionBId
) {
}
