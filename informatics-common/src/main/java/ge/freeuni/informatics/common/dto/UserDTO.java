package ge.freeuni.informatics.common.dto;


import ge.freeuni.informatics.common.model.user.User;
import java.util.Date;

public record UserDTO(
    long id,
    String username,
    String email,
    String firstName,
    String lastName,
    Integer version,
    String role,
    Date lastLogin
) {
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getVersion(),
            user.getRole(),
            user.getLastLogin()
        );
    }

    public static User fromDTO(UserDTO userDTO) {
        User user = new User();

        user.setId(userDTO.id());
        user.setUsername(userDTO.username());
        user.setFirstName(userDTO.firstName());
        user.setLastName(userDTO.lastName());
        user.setEmail(userDTO.email());
        user.setVersion(userDTO.version());
        user.setRole(userDTO.role());
        user.setLastLogin(userDTO.lastLogin());

        return user;
    }
}