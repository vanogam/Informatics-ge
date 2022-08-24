package ge.freeuni.informatics.controller.model;

public class InformaticsResponse {

    public InformaticsResponse() {

    }

    public InformaticsResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    private String message;

    private String status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
