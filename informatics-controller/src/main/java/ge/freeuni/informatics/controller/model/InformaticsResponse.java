package ge.freeuni.informatics.controller.model;

public class InformaticsResponse {

    public InformaticsResponse() {

    }

    public InformaticsResponse(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
