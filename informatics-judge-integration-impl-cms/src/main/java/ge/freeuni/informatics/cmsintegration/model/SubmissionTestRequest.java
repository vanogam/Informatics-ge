package ge.freeuni.informatics.cmsintegration.model;

public class SubmissionTestRequest {

    private Integer cmsID;

    private String testNumber;

    public Integer getCmsID() {
        return cmsID;
    }

    public void setCmsID(Integer cmsID) {
        this.cmsID = cmsID;
    }

    public String getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(String testNumber) {
        this.testNumber = testNumber;
    }
}
