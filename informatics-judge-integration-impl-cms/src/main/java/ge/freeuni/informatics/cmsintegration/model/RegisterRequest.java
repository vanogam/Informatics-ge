package ge.freeuni.informatics.cmsintegration.model;

public class RegisterRequest {

    Integer cmsID;

    Integer appID;

    public Integer getCmsID() {
        return cmsID;
    }

    public void setCmsID(Integer cmsID) {
        this.cmsID = cmsID;
    }

    public Integer getAppID() {
        return appID;
    }

    public void setAppID(Integer appID) {
        this.appID = appID;
    }
}
