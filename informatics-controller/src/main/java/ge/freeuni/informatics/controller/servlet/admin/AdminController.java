package ge.freeuni.informatics.controller.servlet.admin;

import ge.freeuni.informatics.common.dto.WorkerDTO;
import ge.freeuni.informatics.common.exception.ExceptionType;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.annotation.AdminRestricted;
import ge.freeuni.informatics.server.annotation.WorkerRestricted;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.server.worker.IWorkerManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    final Logger log;
    final IWorkerManager workerManager;
    final IUserManager userManager;

    @Autowired
    public AdminController(IWorkerManager workerManager, IUserManager userManager, Logger log) {
        this.workerManager = workerManager;
        this.userManager = userManager;
        this.log = log;
    }

    @PostMapping("/workers/{workerId}/heartbeat")
    @WorkerRestricted
    public ResponseEntity<Void> heartbeat(@PathVariable String workerId, @RequestBody(required = false) HeartbeatRequest request) {
        try {
            Long jobsProcessed = request != null ? request.getJobsProcessed() : null;
            Boolean isWorking = request != null && request.getWorking() != null ? request.getWorking() : false;
            workerManager.updateHeartbeat(workerId, jobsProcessed, isWorking);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            log.error("Error processing heartbeat for worker: {}", workerId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/workers/{workerId}")
    @AdminRestricted
    public ResponseEntity<InformaticsResponse> deleteWorkerInstance(@PathVariable String workerId) {
        try {
            workerManager.deleteWorkerInstance(workerId);
            log.info("Worker instance deleted: {}", workerId);
            return ResponseEntity.ok(new InformaticsResponse(null));
        } catch (Exception ex) {
            log.error("Error deleting worker instance: {}", workerId, ex);
            String errorMessage = ex.getMessage() != null && ex.getMessage().contains("not found") 
                ? "workerNotFound" 
                : "internalError";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new InformaticsResponse(errorMessage));
        }
    }

    @PostMapping("/workers")
    @AdminRestricted
    public ResponseEntity<AddWorkersResponse> addWorkerInstances(@RequestBody AddWorkersRequest request) {
        try {
            if (request.getCount() == null || request.getCount() <= 0) {
                return ResponseEntity.badRequest().body(new AddWorkersResponse("invalidCount", null));
            }

            List<WorkerDTO> createdWorkers = workerManager.addWorkerInstances(request.getCount());
            log.info("Added {} worker instances", request.getCount());
            return ResponseEntity.ok(new AddWorkersResponse(null, createdWorkers));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new AddWorkersResponse("invalidCount", null));
        } catch (Exception ex) {
            log.error("Error adding worker instances", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AddWorkersResponse("internalError", null));
        }
    }

    @DeleteMapping("/workers")
    @AdminRestricted
    public ResponseEntity<InformaticsResponse> stopAllWorkers() {
        try {
            workerManager.stopAllWorkers();
            log.info("All worker instances stopped");
            return ResponseEntity.ok(new InformaticsResponse(null));
        } catch (Exception ex) {
            log.error("Error stopping all workers", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new InformaticsResponse("internalError"));
        }
    }

    @GetMapping("/workers")
    @AdminRestricted
    public ResponseEntity<WorkersResponse> getWorkers() {
        try {
            List<WorkerDTO> workers = workerManager.getAllWorkers();
            return ResponseEntity.ok(new WorkersResponse(workers));
        } catch (Exception ex) {
            log.error("Error getting workers list", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new WorkersResponse("internalError"));
        }
    }

}

