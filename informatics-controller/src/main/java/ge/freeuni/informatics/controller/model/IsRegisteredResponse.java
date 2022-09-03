package ge.freeuni.informatics.controller.model;

public class IsRegisteredResponse extends InformaticsResponse {

    Boolean registered;

    public IsRegisteredResponse(String status, String message, Boolean registered) {
        super(status, message);
        this.registered = registered;
    }

    public Boolean isRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }
}
