package ge.freeuni.informatics.common.dto;

import java.util.Date;

public record UserProfileDTO(
    String username,
    long solvedProblemsCount,
    Date lastLogin,
    Date registrationTime
) {
}


