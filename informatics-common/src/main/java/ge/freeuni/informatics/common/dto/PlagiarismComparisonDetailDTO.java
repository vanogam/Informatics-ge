package ge.freeuni.informatics.common.dto;

import java.util.Date;

/** A comparison's full detail: both submissions' source, for the side-by-side inspection view. */
public record PlagiarismComparisonDetailDTO(
    long id,
    String taskTitle,
    String language,
    float similarity,
    String usernameA,
    String codeA,
    Date submissionTimeA,
    String usernameB,
    String codeB,
    Date submissionTimeB
) {
}
