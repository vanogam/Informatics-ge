package ge.freeuni.informatics.controller.model;

public class LoginResponse extends InformaticsResponse {

    private String username;

    public LoginResponse(String username) {
        this.username = username;
    }

    public LoginResponse(String username, String message) {
        this.username = username;
        super.setMessage(message);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
