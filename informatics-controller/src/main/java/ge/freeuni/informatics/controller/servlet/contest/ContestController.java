package ge.freeuni.informatics.controller.servlet.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.contest.ContestService;
import ge.freeuni.informatics.server.contest.IContestManager;
import ge.freeuni.informatics.server.submission.ISubmissionManager;
import ge.freeuni.informatics.server.task.ITaskManager;
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
    private ITaskManager taskManager;

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

    @PostMapping("/contests/{contestId}/register")
    public InformaticsResponse register(@PathVariable String contestId) {
        try {
            contestManager.registerUser(Long.parseLong(contestId));
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @PostMapping("/contests/{contestId}/standings")
    public StandingsResponse getStandings(@PathVariable String contestId, @RequestBody PagingRequest request) {
        StandingsResponse response = new StandingsResponse("SUCCESS", null);
        try {
            List<ContestantResult> result = contestService.getStandings(Long.parseLong(contestId), request.getOffset(), request.getLimit());
            response.setStandings(result);
        } catch (InformaticsServerException ex) {
            return new StandingsResponse("FAIL", ex.getCode());
        }
        return response;
    }

    @GetMapping("/contests/{contestId}/tasks")
    public TasksResponse getContestTasks(@PathVariable String contestId) {
        TasksResponse response = new TasksResponse("SUCCESS", null);
        try {
            response.setTasks(contestManager.getContest(Long.parseLong(contestId)).getTasks());
        } catch (InformaticsServerException ex) {
            return new TasksResponse("FAIL", ex.getCode());
        }
        return response;
    }

    @GetMapping("/contests/{contestId}/submissions")
    public SubmissionListResponse getSubmissionsList(@PathVariable String contestId,
                                                     @RequestParam(required = false) Long taskId,
                                                     @RequestParam(defaultValue = "1") Integer page) {
        SubmissionListResponse response = new SubmissionListResponse("SUCCESS", null);
        try {
            response.setSubmissions(submissionManager.filter(userManager.getAuthenticatedUser().getId(),
                    taskId,
                    Long.parseLong(contestId),
                    null,
                    20 * (page - 1),
                    20));
        } catch (InformaticsServerException ex) {
            return new SubmissionListResponse("FAIL", ex.getCode());
        }
        return response;
    }

    public Date convertToDate(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
    }
}
