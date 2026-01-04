package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.WorkerDTO;

import java.util.List;

public class AddWorkersResponse {
    private String message;
    private List<WorkerDTO> createdWorkers;

    public AddWorkersResponse(String message, List<WorkerDTO> createdWorkers) {
        this.message = message;
        this.createdWorkers = createdWorkers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<WorkerDTO> getCreatedWorkers() {
        return createdWorkers;
    }

    public void setCreatedWorkers(List<WorkerDTO> createdWorkers) {
        this.createdWorkers = createdWorkers;
    }
}

