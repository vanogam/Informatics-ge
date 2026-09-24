package ge.freeuni.informatics.controller.servlet.admin;

import ge.freeuni.informatics.common.dto.WorkerDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.annotation.AdminRestricted;
import ge.freeuni.informatics.server.submission.ISubmissionManager;
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
    final ISubmissionManager submissionManager;

    @Autowired
    public AdminController(IWorkerManager workerManager, IUserManager userManager,
                           ISubmissionManager submissionManager, Logger log) {
        this.workerManager = workerManager;
        this.userManager = userManager;
        this.submissionManager = submissionManager;
        this.log = log;
    }

    /**
     * Re-judges submissions after the task they belong to has changed - a fixed grader, an added
     * testcase, a corrected scoring parameter - and rescues ones left stuck by a dead worker.
     *
     * <p>Answers 200 whenever the request itself is well formed, even if every submission in it was
     * turned away: the reasons are per-submission rows in the body.
     */
    @PostMapping("/submissions/rejudge")
    @AdminRestricted
    public ResponseEntity<RejudgeResponse> rejudgeSubmissions(@RequestBody(required = false) RejudgeRequest request)
            throws InformaticsServerException {
        if (request == null) {
            throw InformaticsServerException.INVALID_REJUDGE_REQUEST;
        }
        return ResponseEntity.ok(new RejudgeResponse(
                submissionManager.rejudge(request.submissionIds(), request.action())));
    }

    @PostMapping("/workers/{workerId}/heartbeat")
    @WorkerRestricted
    public ResponseEntity<Void> heartbeat(@PathVariable String workerId, @RequestBody(required = false) HeartbeatRequest request) throws InformaticsServerException {
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
    public ResponseEntity<InformaticsResponse> deleteWorkerInstance(@PathVariable String workerId) throws InformaticsServerException {
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
    public ResponseEntity<AddWorkersResponse> addWorkerInstances(@RequestBody AddWorkersRequest request) throws InformaticsServerException {
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
    public ResponseEntity<InformaticsResponse> stopAllWorkers() throws InformaticsServerException {
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
    public ResponseEntity<WorkersResponse> getWorkers() throws InformaticsServerException {
        try {
            List<WorkerDTO> workers = workerManager.getAllWorkers();
            return ResponseEntity.ok(new WorkersResponse(workers));
        } catch (Exception ex) {
            log.error("Error getting workers list", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new WorkersResponse("internalError"));
        }
    }

}

