package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.ContestDTO;

import java.util.List;

public class ContestResponse extends InformaticsResponse {

    List<ContestDTO> contests;

    public List<ContestDTO> getContests() {
        return contests;
    }

    public void setContests(List<ContestDTO> contests) {
        this.contests = contests;
    }
}
