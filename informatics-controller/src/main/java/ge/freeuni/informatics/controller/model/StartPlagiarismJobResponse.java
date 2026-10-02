package ge.freeuni.informatics.controller.model;

public class StartPlagiarismJobResponse extends InformaticsResponse {

    private long jobId;

    public StartPlagiarismJobResponse() {
    }

    public StartPlagiarismJobResponse(long jobId) {
        this.jobId = jobId;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }
}
