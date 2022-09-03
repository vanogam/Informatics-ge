package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.ContestantResultDTO;

import java.util.List;

public class StandingsResponse extends InformaticsResponse {

    private List<ContestantResultDTO> standings;

    public StandingsResponse(String status, String message) {
        super(status, message);
    }

    public List<ContestantResultDTO> getStandings() {
        return standings;
    }

    public void setStandings(List<ContestantResultDTO> standings) {
        this.standings = standings;
    }
}
