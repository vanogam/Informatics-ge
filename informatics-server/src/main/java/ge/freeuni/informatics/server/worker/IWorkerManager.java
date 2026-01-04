package ge.freeuni.informatics.server.worker;

import ge.freeuni.informatics.common.dto.WorkerDTO;

import java.util.List;

public interface IWorkerManager {
    void updateHeartbeat(String workerId, Long jobsProcessed, Boolean isWorking);
    List<WorkerDTO> getAllWorkers();
    WorkerDTO createWorkerInstance();
    void deleteWorkerInstance(String workerId);
    List<WorkerDTO> addWorkerInstances(int count);
    void stopAllWorkers();
    int getCurrentWorkerCount();
}

