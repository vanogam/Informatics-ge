package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.TaskDTO;

public class SaveTaskResponse extends InformaticsResponse {

    private TaskDTO taskDTO;

    public SaveTaskResponse() {
    }

    public SaveTaskResponse(String status, String message, TaskDTO taskDTO) {
        super(status, message);
        this.taskDTO = taskDTO;
    }

    public TaskDTO getTaskDTO() {
        return taskDTO;
    }

    public void setTaskDTO(TaskDTO taskDTO) {
        this.taskDTO = taskDTO;
    }
}
