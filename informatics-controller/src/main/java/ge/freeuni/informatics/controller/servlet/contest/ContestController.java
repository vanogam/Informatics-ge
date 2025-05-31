package ge.freeuni.informatics.controller.servlet.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.ContestantResultDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.contest.ContestService;
import ge.freeuni.informatics.server.contest.IContestManager;
import ge.freeuni.informatics.server.submission.ISubmissionManager;
import ge.freeuni.informatics.server.task.ITaskManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ContestController {

    @Value("${ge.freeuni.informatics.defaultPageSize}")
    Integer defaultPageSize;

    @Value("${ge.freeuni.informatics.maxPageSize}")
    Integer maxPageSize;

    @Autowired
    private IContestManager contestManager;

    @Autowired
    private ContestService contestService;

    @Autowired
    private IUserManager userManager;

    @Autowired
    private ITaskManager taskManager;

    @Autowired
    private ISubmissionManager submissionManager;

    @GetMapping("/contest/{id}")
    public ResponseEntity<ContestDTO> getContest(@PathVariable long id) {
        try {
            return ResponseEntity.ok(contestManager.getContest(id));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.notFound().build();
        }
    }
    
   @GetMapping("/contests")
    public ResponseEntity<ContestResponse> getContestList(ContestListRequest request) {
        ContestResponse response = new ContestResponse();
        try {
            response.setContests(contestManager.getContests(Long.valueOf(request.getRoomId()), null, null, null, null, null, null));
        } catch (InformaticsServerException e) {
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        response.setStatus("SUCCESS");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/contest/{id}/is-registered")
    public IsRegisteredResponse isRegistered(@PathVariable Long id) {
        try {
            return new IsRegisteredResponse("SUCCESS", null, contestManager.isCurrentUserRegistered(id));
        } catch (InformaticsServerException ex) {
            return new IsRegisteredResponse("FAIL", ex.getCode(), null);
        }
    }

    @GetMapping("/contest/{id}/registrants")
    public RegistrantsResponse getRegistrants(@PathVariable Long id) {
        try {
            return new RegistrantsResponse("SUCCESS", null, contestManager.getRegistrants(id));
        } catch (InformaticsServerException ex) {
            return new RegistrantsResponse("FAIL", ex.getCode(), null);
        }
    }

    @PostMapping("/create-contest")
    public ResponseEntity<CreateContestResponse> createContest(@RequestBody CreateContestRequest contestRequest) {
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(convertToDate(contestRequest.getStartDate()));
        endDate.add(Calendar.SECOND, contestRequest.getDurationInSeconds());
        ContestDTO contestDTO = new ContestDTO();
        contestDTO.setId(contestRequest.getContestId());
        contestDTO.setName(contestRequest.getName());
        contestDTO.setRoomId(contestRequest.getRoomId());
        contestDTO.setEndDate(endDate.getTime());
        contestDTO.setStartDate(convertToDate(contestRequest.getStartDate()));
        contestDTO.setUpsolvingAfterFinish(contestRequest.isUpsolvingAfterFinish());
        contestDTO.setUpsolving(contestRequest.isUpsolving());
        contestDTO.setScoringType(contestRequest.getScoringType());
        try {
            return ResponseEntity.ok(new CreateContestResponse("SUCCESS", null, contestManager.createContest(contestDTO)));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new CreateContestResponse());
        }
    }

    @DeleteMapping("/contest/{contestId}")
    public InformaticsResponse deleteContest(@PathVariable Long contestId) {
        try {
            contestManager.deleteContest(contestId);
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @PostMapping("/contest/{contestId}/register")
    public InformaticsResponse register(@PathVariable Long contestId) {
        try {
            contestManager.registerUser(contestId);
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @PostMapping("/contest/{contestId}/unregister")
    public InformaticsResponse unregister(@PathVariable String contestId) {
        try {
            contestManager.unregisterUser(Long.parseLong(contestId));
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @GetMapping("/contest/{contestId}/standings")
    public StandingsResponse getStandings(@PathVariable Long contestId, PagingRequest request) {
        StandingsResponse response = new StandingsResponse("SUCCESS", null);
        try {
            List<ContestantResultDTO> result = contestService.getStandings(contestId, request.getOffset(), request.getLimit());
            response.setTaskNameMap(taskManager.fillTaskNames(contestId));
            response.setStandings(result);
        } catch (InformaticsServerException ex) {
            return new StandingsResponse("FAIL", ex.getCode());
        }
        return response;
    }

    @GetMapping("/contest/{contestId}/submissions")
    public SubmissionListResponse getSubmissionsList(@PathVariable String contestId,
                                                     GetSubmissionsRequest request) {
        SubmissionListResponse response = new SubmissionListResponse("SUCCESS", null);
        if (request.getLimit() == null) {
            request.setLimit(defaultPageSize);
        }
        try {
            response.setSubmissions(submissionManager.filter(userManager.getAuthenticatedUser().id(),
                    request.getTaskId(),
                    Long.parseLong(contestId),
                    null,
                    request.getOffset(),
                    request.getLimit()));
        } catch (InformaticsServerException ex) {
            return new SubmissionListResponse("FAIL", ex.getCode());
        }
        return response;
    }

    @GetMapping("/contest/{contestId}/status")
    public SubmissionListResponse getStatus(@PathVariable String contestId,
                                                     GetSubmissionsRequest request) {
        SubmissionListResponse response = new SubmissionListResponse("SUCCESS", null);
        try {
            response.setSubmissions(submissionManager.filter(null,
                    request.getTaskId(),
                    Long.parseLong(contestId),
                    null,
                    request.getOffset(),
                    request.getLimit()));
        } catch (InformaticsServerException ex) {
            return new SubmissionListResponse("FAIL", ex.getCode());
        }
        return response;
    }

    public Date convertToDate(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
    }
}
