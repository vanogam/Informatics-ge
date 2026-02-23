package ge.freeuni.informatics.common.dto;

import java.util.Date;

public record UserProblemDTO(
    Long taskId,
    String taskName,
    String contestName,
    Date lastAttemptDate
) {
}

