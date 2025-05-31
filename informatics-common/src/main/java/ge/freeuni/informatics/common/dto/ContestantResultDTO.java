package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contest.ScoringType;
import ge.freeuni.informatics.common.model.contest.TaskResult;

import java.util.Map;
import java.util.stream.Collectors;

public class ContestantResultDTO implements Comparable<ContestantResultDTO> {

    private Float totalScore;

    private Long contestantId;

    private String username;

    private Map<String, TaskResultDTO> taskResults;

    private Map<String, String> taskNames;

    public Float getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Float totalScore) {
        this.totalScore = totalScore;
    }

    public Long getContestantId() {
        return contestantId;
    }

    public void setContestantId(Long contestantId) {
        this.contestantId = contestantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, TaskResultDTO> getTaskResults() {
        return taskResults;
    }

    public void setTaskResults(Map<String, TaskResultDTO> taskResults) {
        this.taskResults = taskResults;
    }

    public Map<String, String> getTaskNames() {
        return taskNames;
    }

    public void setTaskNames(Map<String, String> taskNames) {
        this.taskNames = taskNames;
    }

    public static ContestantResultDTO toDTO(ContestantResult contestantResult) {
        ContestantResultDTO contestantResultDTO = new ContestantResultDTO();
        contestantResultDTO.setContestantId(contestantResult.getContestantId());
        contestantResultDTO.setTaskResults(contestantResult
                .getTaskResults()
                .values()
                .stream()
                .map(TaskResultDTO::toDTO)
                .collect(Collectors.toMap(TaskResultDTO::taskCode, taskResultDTO -> taskResultDTO)));
        contestantResultDTO.setTotalScore(contestantResult.getTotalScore());
        return contestantResultDTO;
    }
    public static ContestantResult fromDTO(ContestantResultDTO contestantResultDTO) {
        ContestantResult contestantResult = new ContestantResult();
        contestantResult.setTaskResults(contestantResultDTO
                .getTaskResults()
                .values()
                .stream()
                .map(TaskResultDTO::fromDTO)
                .collect(Collectors.toMap(TaskResult::getTaskCode, taskResult -> taskResult)));
        contestantResultDTO.setTotalScore(contestantResult.getTotalScore());
        return contestantResult;
    }

    public void setTaskResult(TaskResultDTO taskResult, ScoringType type) {
        float initialScore = 0;
        if (!this.taskResults.containsKey(taskResult.taskCode())) {
            this.taskResults.put(taskResult.taskCode(), taskResult);
        } else {
            initialScore = this.taskResults.get(taskResult.taskCode()).score();
        }
        if (type == ScoringType.BEST_SUBMISSION) {
            totalScore = totalScore + Math.max(taskResult.score(), initialScore) - initialScore;
        } else {
            totalScore = totalScore + taskResult.score() - initialScore;
        }

        this.taskResults.put(taskResult.taskCode(), taskResult);
    }

    public TaskResultDTO getTaskResult(String taskCode) {
        if (!this.taskResults.containsKey(taskCode)) {
            return null;
        }
        return taskResults.get(taskCode);
    }

    @Override
    public int compareTo(ContestantResultDTO o) {
        return this.totalScore.compareTo(o.totalScore) * -1; // Sort in descending order
    }
}
