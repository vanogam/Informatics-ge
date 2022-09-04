package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contest.ScoringType;

import java.util.Map;

public class ContestantResultDTO {

    private Float totalScore;

    private long contestantId;

    private String username;

    private Map<String, Float> scores;

    private Map<String, String> taskNames;

    private ScoringType type;

    public Float getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Float totalScore) {
        this.totalScore = totalScore;
    }

    public long getContestantId() {
        return contestantId;
    }

    public void setContestantId(long contestantId) {
        this.contestantId = contestantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Float> getScores() {
        return scores;
    }

    public void setScores(Map<String, Float> scores) {
        this.scores = scores;
    }

    public Map<String, String> getTaskNames() {
        return taskNames;
    }

    public void setTaskNames(Map<String, String> taskNames) {
        this.taskNames = taskNames;
    }

    public ScoringType getType() {
        return type;
    }

    public void setType(ScoringType type) {
        this.type = type;
    }

    public static ContestantResultDTO toDTO(ContestantResult contestantResult) {
        ContestantResultDTO contestantResultDTO = new ContestantResultDTO();
        contestantResultDTO.setContestantId(contestantResult.getContestantId());
        contestantResultDTO.setScores(contestantResult.getScores());
        contestantResultDTO.setTotalScore(contestantResult.getTotalScore());
        contestantResultDTO.setType(contestantResult.getType());
        return contestantResultDTO;
    }
}
