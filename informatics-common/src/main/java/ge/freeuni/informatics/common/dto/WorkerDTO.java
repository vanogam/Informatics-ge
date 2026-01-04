package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.worker.Worker;
import ge.freeuni.informatics.common.model.worker.WorkerStatus;

import java.util.Date;

public record WorkerDTO(
    String workerId,
    WorkerStatus status,
    Date lastHeartbeat,
    Long uptimeSeconds,
    Long jobsProcessed
) {
    public static WorkerDTO toDTO(Worker worker, long uptimeSeconds) {
        if (worker == null) {
            return null;
        }
        return new WorkerDTO(
            worker.getWorkerId(),
            worker.getStatus(),
            worker.getLastHeartbeat(),
            uptimeSeconds,
            worker.getJobsProcessed()
        );
    }
}

