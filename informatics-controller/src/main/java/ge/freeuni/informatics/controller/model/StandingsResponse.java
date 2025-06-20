package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.ContestantResultDTO;

import java.util.List;
import java.util.Map;

public class StandingsResponse extends InformaticsResponse {

    private List<ContestantResultDTO> standings;

    private Map<String, String> taskNameMap;

    public StandingsResponse(String status, String message) {
        super(message);
    }

    public List<ContestantResultDTO> getStandings() {
        return standings;
    }

    public Map<String, String> getTaskNameMap() {
        return taskNameMap;
    }

    public void setTaskNameMap(Map<String, String> taskNameMap) {
        this.taskNameMap = taskNameMap;
    }

    public void setStandings(List<ContestantResultDTO> standings) {
        this.standings = standings;
    }
}
