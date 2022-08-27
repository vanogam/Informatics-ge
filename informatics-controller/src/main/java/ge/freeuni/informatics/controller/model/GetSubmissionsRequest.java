package ge.freeuni.informatics.controller.model;

public class GetSubmissionsRequest extends PagingRequest {

    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
