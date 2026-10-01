package ge.freeuni.informatics.controller.model;

/** Filters for the admin "all submissions" view - nothing here is fixed by the page, unlike the
 *  contest/room/user scoped submission lists. */
public class AdminSubmissionListRequest extends PagingRequest {

    private String username;

    private Long taskId;

    private Long contestId;

    private String language;

    private String status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
