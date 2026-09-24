package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.model.task.TaskInfo;

import java.util.List;

public class GetTasksResponse extends InformaticsResponse {

    List<TaskInfo> tasks;

    long totalCount;

    public  GetTasksResponse() {}

    public GetTasksResponse(String message) {
        super(message);
    }

    public List<TaskInfo> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskInfo> tasks) {
        this.tasks = tasks;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
