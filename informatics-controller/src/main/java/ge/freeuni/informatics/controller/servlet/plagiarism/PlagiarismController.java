package ge.freeuni.informatics.controller.servlet.plagiarism;

import ge.freeuni.informatics.common.dto.PlagiarismComparisonDetailDTO;
import ge.freeuni.informatics.common.dto.PlagiarismJobDetailDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.PagingRequest;
import ge.freeuni.informatics.controller.model.PlagiarismComparisonListResponse;
import ge.freeuni.informatics.controller.model.PlagiarismJobListResponse;
import ge.freeuni.informatics.controller.model.StartPlagiarismJobRequest;
import ge.freeuni.informatics.controller.model.StartPlagiarismJobResponse;
import ge.freeuni.informatics.server.annotation.AdminRestricted;
import ge.freeuni.informatics.server.plagiarism.IPlagiarismManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/plagiarism")
public class PlagiarismController {

    private final IPlagiarismManager plagiarismManager;

    @Autowired
    public PlagiarismController(IPlagiarismManager plagiarismManager) {
        this.plagiarismManager = plagiarismManager;
    }

    @GetMapping("/eligible-users")
    @AdminRestricted
    public ResponseEntity<List<String>> getEligibleUsers(@RequestParam(required = false) Long contestId,
                                                          @RequestParam(required = false) Long taskId)
            throws InformaticsServerException {
        return ResponseEntity.ok(plagiarismManager.listEligibleUsers(contestId, taskId));
    }

    @PostMapping("/jobs")
    @AdminRestricted
    public ResponseEntity<StartPlagiarismJobResponse> startJob(@RequestBody StartPlagiarismJobRequest request)
            throws InformaticsServerException {
        if (request == null) {
            throw InformaticsServerException.INVALID_PLAGIARISM_SCOPE;
        }
        long jobId = plagiarismManager.startJob(request.contestId(), request.taskId(), request.usernames());
        return ResponseEntity.ok(new StartPlagiarismJobResponse(jobId));
    }

    @GetMapping("/jobs")
    @AdminRestricted
    public ResponseEntity<PlagiarismJobListResponse> getJobs(PagingRequest request) {
        Integer offset = request.getOffset() == null ? 0 : request.getOffset();
        Integer limit = request.getLimit() == null ? 20 : request.getLimit();
        PlagiarismJobListResponse response = new PlagiarismJobListResponse();
        response.setJobs(plagiarismManager.listJobs(offset, limit));
        response.setTotalCount(plagiarismManager.countJobs());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs/{jobId}")
    @AdminRestricted
    public ResponseEntity<PlagiarismJobDetailDTO> getJob(@PathVariable long jobId) throws InformaticsServerException {
        return ResponseEntity.ok(plagiarismManager.getJob(jobId));
    }

    @GetMapping("/runs/{runId}/comparisons")
    @AdminRestricted
    public ResponseEntity<PlagiarismComparisonListResponse> getComparisons(@PathVariable long runId, PagingRequest request)
            throws InformaticsServerException {
        Integer offset = request.getOffset() == null ? 0 : request.getOffset();
        Integer limit = request.getLimit() == null ? 20 : request.getLimit();
        PlagiarismComparisonListResponse response = new PlagiarismComparisonListResponse();
        response.setComparisons(plagiarismManager.listComparisons(runId, offset, limit));
        response.setTotalCount(plagiarismManager.countComparisons(runId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comparisons/{comparisonId}")
    @AdminRestricted
    public ResponseEntity<PlagiarismComparisonDetailDTO> getComparisonDetail(@PathVariable long comparisonId)
            throws InformaticsServerException {
        return ResponseEntity.ok(plagiarismManager.getComparisonDetail(comparisonId));
    }
}
