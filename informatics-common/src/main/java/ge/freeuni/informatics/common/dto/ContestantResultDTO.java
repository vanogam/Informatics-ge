package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contest.TaskResult;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record ContestantResultDTO(
        Float totalScore,
        Long contestantId,
        Long contestId,
        String username,
        Map<String, TaskResultDTO> taskResults,
        Map<String, String> taskNames
) implements Comparable<ContestantResultDTO> {

    public ContestantResultDTO {
        taskResults = taskResults != null ? Map.copyOf(taskResults) : Map.of();
        taskNames = taskNames != null ? Map.copyOf(taskNames) : Map.of();
    }

    public TaskResultDTO getTaskResult(String taskCode) {
        return taskResults.get(taskCode);
    }

    @Override
    public int compareTo(ContestantResultDTO o) {
        int scoreDiff = this.totalScore.compareTo(o.totalScore) * -1;
        if (scoreDiff == 0) {
            return this.contestantId.compareTo(o.contestantId);
        }
        return scoreDiff;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ContestantResultDTO other)) {
            return false;
        }
        return this.contestantId.equals(other.contestantId);
    }

    @Override
    public int hashCode() {
        return contestantId.hashCode();
    }

    public static ContestantResultDTO toDTO(ContestantResult contestantResult) {
        return toDTO(contestantResult, null);
    }

    public static ContestantResultDTO toDTO(ContestantResult contestantResult, String username) {
        Map<String, TaskResultDTO> taskResultsMap = contestantResult
                .getTaskResults()
                .values()
                .stream()
                .map(TaskResultDTO::toDTO)
                .collect(Collectors.toMap(TaskResultDTO::getTaskCode, taskResultDTO -> taskResultDTO));

        return new ContestantResultDTO(
                contestantResult.getTotalScore(),
                contestantResult.getContestantId(),
                contestantResult.getContest().getId(),
                username,
                taskResultsMap,
                null
        );
    }

    public static ContestantResult fromDTO(ContestantResultDTO contestantResultDTO, Contest contest) {
        ContestantResult contestantResult = new ContestantResult();
        contestantResult.setTaskResults(contestantResultDTO
                .taskResults()
                .values()
                .stream()
                .map(TaskResultDTO::fromDTO)
                .collect(Collectors.toMap(TaskResult::getTaskCode, taskResult -> taskResult)));
        contestantResult.setTotalScore(contestantResultDTO.totalScore());
        contestantResult.setContestant(contestantResultDTO.contestantId());
        contestantResult.setContest(contest);
        return contestantResult;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ContestantResultDTO source) {
        return new Builder(source);
    }

    public static class Builder {
        private Float totalScore;
        private Long contestantId;
        private Long contestId;
        private String username;
        private Map<String, TaskResultDTO> taskResults;
        private Map<String, String> taskNames;

        public Builder() {
            this.taskResults = new HashMap<>();
            this.taskNames = new HashMap<>();
        }

        public Builder(ContestantResultDTO source) {
            this.totalScore = source.totalScore();
            this.contestantId = source.contestantId();
            this.contestId = source.contestId();
            this.username = source.username();
            this.taskResults = source.taskResults() != null ? new HashMap<>(source.taskResults()) : new HashMap<>();
            this.taskNames = source.taskNames() != null ? new HashMap<>(source.taskNames()) : new HashMap<>();
        }

        public Builder totalScore(Float totalScore) {
            this.totalScore = totalScore;
            return this;
        }

        public Builder contestantId(Long contestantId) {
            this.contestantId = contestantId;
            return this;
        }

        public Builder contestId(Long contestId) {
            this.contestId = contestId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder taskResults(Map<String, TaskResultDTO> taskResults) {
            this.taskResults = taskResults != null ? new HashMap<>(taskResults) : new HashMap<>();
            return this;
        }

        public Builder taskNames(Map<String, String> taskNames) {
            this.taskNames = taskNames != null ? new HashMap<>(taskNames) : new HashMap<>();
            return this;
        }

        public Builder putTaskResult(String taskCode, TaskResultDTO taskResult) {
            if (this.taskResults == null) {
                this.taskResults = new HashMap<>();
            }
            this.taskResults.put(taskCode, taskResult);
            return this;
        }

        public Builder putTaskName(String taskCode, String taskName) {
            if (this.taskNames == null) {
                this.taskNames = new HashMap<>();
            }
            this.taskNames.put(taskCode, taskName);
            return this;
        }

        public Builder removeTaskResult(String taskCode) {
            if (this.taskResults != null) {
                this.taskResults.remove(taskCode);
            }
            return this;
        }

        public ContestantResultDTO build() {
            return new ContestantResultDTO(
                    totalScore,
                    contestantId,
                    contestId,
                    username,
                    taskResults,
                    taskNames
            );
        }
    }
}
