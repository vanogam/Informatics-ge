package ge.freeuni.informatics.controller.servlet.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.ContestListRequest;
import ge.freeuni.informatics.controller.model.ContestResponse;
import ge.freeuni.informatics.controller.model.CreateContestRequest;
import ge.freeuni.informatics.controller.model.InformaticsResponse;
import ge.freeuni.informatics.server.contest.IContestManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

@RestController
public class ContestController {

    @Autowired
    private IContestManager contestManager;

    @GetMapping("/contest-list")
    public ContestResponse getContestList(@RequestBody ContestListRequest request) {
        ContestResponse response = new ContestResponse();
        response.setContests(contestManager.getContests(Long.valueOf(request.getId()), null, null, null, null));
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

    public Date convertToDate(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
    }
}
