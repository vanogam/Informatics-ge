package ge.freeuni.informatics.controller.model;

import java.util.List;

public class TaskNamesResponse extends InformaticsResponse {

    List<String> taskNames;

    public TaskNamesResponse(String status, String message, List<String> taskNames) {
        super(message);
        this.taskNames = taskNames;
    }

    public List<String> getTaskNames() {
        return taskNames;
    }

    public void setTaskNames(List<String> taskNames) {
        this.taskNames = taskNames;
    }
}
