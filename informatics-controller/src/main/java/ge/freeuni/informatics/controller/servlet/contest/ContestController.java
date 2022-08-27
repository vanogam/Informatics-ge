package ge.freeuni.informatics.controller.servlet.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.contest.ContestService;
import ge.freeuni.informatics.server.contest.IContestManager;
import ge.freeuni.informatics.server.submission.ISubmissionManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
public class ContestController {

    @Autowired
    private IContestManager contestManager;

    @Autowired
    private ContestService contestService;

    @Autowired
    private IUserManager userManager;

    @Autowired
    private ISubmissionManager submissionManager;

    @GetMapping("/contest-list")
    public ContestResponse getContestList(@RequestBody ContestListRequest request) {
        ContestResponse response = new ContestResponse();
        response.setContests(contestManager.getContests(Long.valueOf(request.getId()), null, null, null, null, null));
        response.setStatus("SUCCESS");
        return response;
    }

    @PostMapping("/create-contest")
    public InformaticsResponse createContest(@RequestBody CreateContestRequest contestRequest) {
        ContestDTO contestDTO = new ContestDTO();
        contestDTO.setName(contestRequest.getName());
        contestDTO.setRoomId(contestRequest.getRoomId());
        contestDTO.setDurationInSeconds(contestRequest.getDurationInSeconds());
        contestDTO.setStartDate(convertToDate(contestRequest.getStartDate()));
        contestDTO.setUpsolvingAfterFinish(contestRequest.isUpsolvingAfterFinish());
        contestDTO.setUpsolving(false);
        contestDTO.setScoringType(contestRequest.getScoringType());
        InformaticsResponse response = new InformaticsResponse();
        try {
            contestManager.createContest(contestDTO);
            response.setStatus("SUCCESS");
        } catch (InformaticsServerException ex) {
            response.setStatus("FAIL");
            response.setMessage(ex.getCode());
        }
        return response;
    }

    @DeleteMapping("/contests/{contestId}")
    public InformaticsResponse deleteContest(@PathVariable Long contestId) {
        try {
            contestManager.deleteContest(contestId);
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @PostMapping("/contests/{contestId}/register")
    public InformaticsResponse register(@PathVariable String contestId) {
        try {
            contestManager.registerUser(Long.parseLong(contestId));
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @PostMapping("/contests/{contestId}/unregister")
    public InformaticsResponse unregister(@PathVariable String contestId) {
        try {
            contestManager.unregisterUser(Long.parseLong(contestId));
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @GetMapping("/contest/{contestId}/standings")
    public StandingsResponse getStandings(@PathVariable String contestId, @RequestParam PagingRequest request) {
        StandingsResponse response = new StandingsResponse("SUCCESS", null);
        try {
            List<ContestantResult> result = contestService.getStandings(Long.parseLong(contestId), request.getOffset(), request.getLimit());
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
        try {
            response.setSubmissions(submissionManager.filter(userManager.getAuthenticatedUser().getId(),
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
