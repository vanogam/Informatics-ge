package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.model.contest.ContestantResult;

import java.util.List;

public class StandingsResponse extends InformaticsResponse {

    private List<ContestantResult> standings;

    public StandingsResponse(String status, String message) {
        super(status, message);
    }

    public List<ContestantResult> getStandings() {
        return standings;
    }

    public void setStandings(List<ContestantResult> standings) {
        this.standings = standings;
    }
}
