package ge.freeuni.informatics.common.dto;

public record AuthenticationDetails(
        String username,
        String password,
        Boolean rememberMe
        ) {
    public AuthenticationDetails(String username, String password) {
        this(username, password, false);
    }
}
