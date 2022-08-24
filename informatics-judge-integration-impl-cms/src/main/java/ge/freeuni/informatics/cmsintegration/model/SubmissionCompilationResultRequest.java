package ge.freeuni.informatics.cmsintegration.model;

public class SubmissionCompilationResultRequest {

    private Integer cmsID;

    private String result;

    private String message;

    public Integer getCmsID() {
        return cmsID;
    }

    public void setCmsID(Integer cmsID) {
        this.cmsID = cmsID;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
