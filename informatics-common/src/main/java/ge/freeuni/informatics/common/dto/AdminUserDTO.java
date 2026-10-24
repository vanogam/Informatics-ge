package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.user.User;

import java.util.Date;

/** One row of the admin "users" listing - a narrower view than {@link UserDTO}, scoped to what
 *  that page actually shows, so it can carry {@code disabled} without touching every other
 *  {@link UserDTO} call site (registration, login, profile). */
public record AdminUserDTO(
    long id,
    String username,
    String firstName,
    String lastName,
    String role,
    Date lastLogin,
    boolean disabled
) {
    public static AdminUserDTO toDTO(User user) {
        return new AdminUserDTO(
            user.getId(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.getLastLogin(),
            user.isDisabled()
        );
    }
}
