package ge.freeuni.informatics.cmsintegration.model;

public class SubmissionResultRequest {

    Integer cmsID;

    String score;

    TestResult[] submissionResult;

    public Integer getCmsID() {
        return cmsID;
    }

    public void setCmsID(Integer cmsID) {
        this.cmsID = cmsID;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public TestResult[] getSubmissionResult() {
        return submissionResult;
    }

    public void setSubmissionResult(TestResult[] submissionResult) {
        this.submissionResult = submissionResult;
    }
}
