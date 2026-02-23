package ge.freeuni.informatics.controller.model;

public class SubmitResponse extends InformaticsResponse {
    
    private Long submissionId;
    
    public SubmitResponse() {
        super();
    }
    
    public SubmitResponse(Long submissionId) {
        super();
        this.submissionId = submissionId;
    }
    
    public SubmitResponse(String message) {
        super(message);
    }
    
    public Long getSubmissionId() {
        return submissionId;
    }
    
    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }
}
