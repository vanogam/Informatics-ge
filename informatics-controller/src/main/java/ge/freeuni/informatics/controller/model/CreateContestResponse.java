package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.ContestDTO;

public class CreateContestResponse extends InformaticsResponse {

    private ContestDTO contest;

    public CreateContestResponse() {
    }

    public CreateContestResponse(String status, String message, ContestDTO contest) {
        super(message);
        this.contest = contest;
    }

    public ContestDTO getContest() {
        return contest;
    }

    public void setContest(ContestDTO contest) {
        this.contest = contest;
    }
}
