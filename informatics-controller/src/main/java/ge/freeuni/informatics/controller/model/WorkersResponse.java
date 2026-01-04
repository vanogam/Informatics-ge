package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.WorkerDTO;

import java.util.List;

public class WorkersResponse {
    private String message;
    private List<WorkerDTO> workers;

    public WorkersResponse(String message) {
        this.message = message;
    }

    public WorkersResponse(List<WorkerDTO> workers) {
        this.workers = workers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<WorkerDTO> getWorkers() {
        return workers;
    }

    public void setWorkers(List<WorkerDTO> workers) {
        this.workers = workers;
    }
}

