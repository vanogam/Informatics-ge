package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.model.task.TaskInfo;

import java.util.List;

public class GetTasksResponse extends InformaticsResponse {

    List<TaskInfo> tasks;

    public GetTasksResponse(String status, String message) {
        super(status, message);
    }

    public List<TaskInfo> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskInfo> tasks) {
        this.tasks = tasks;
    }
}
