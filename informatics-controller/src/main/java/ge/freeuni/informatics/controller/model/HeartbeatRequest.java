package ge.freeuni.informatics.controller.model;

public class HeartbeatRequest {
    private Long jobsProcessed;
    private Boolean working;

    public Long getJobsProcessed() {
        return jobsProcessed;
    }

    public void setJobsProcessed(Long jobsProcessed) {
        this.jobsProcessed = jobsProcessed;
    }

    public Boolean getWorking() {
        return working;
    }

    public void setWorking(Boolean working) {
        this.working = working;
    }
}

