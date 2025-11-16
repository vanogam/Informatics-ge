package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.contest.TaskResult;

import java.util.Map;

public class TaskResultDTO {
        private final String taskCode;
        private final Float score;
        private Integer attempts;
        private Long successTime;

    public TaskResultDTO(String taskCode, Float score) {
        this(taskCode, score, 0, null);
    }

    public TaskResultDTO(String taskCode, Float score, Integer attempts, Long successTime) {
        this.taskCode = taskCode;
        this.score = score;
        this.attempts = attempts;
        this.successTime = successTime;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public Float getScore() {
        return score;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Long getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(Long successTime) {
        this.successTime = successTime;
    }

    public static TaskResultDTO toDTO(TaskResult taskResult) {
        return new TaskResultDTO(
                taskResult.getTaskCode(),
                taskResult.getScore(),
                taskResult.getAttempts(),
                taskResult.getSuccessTime()
        );
    }

    public static TaskResult fromDTO(TaskResultDTO taskResultDTO) {
        TaskResult taskResult = new TaskResult();
        taskResult.setTaskCode(taskResultDTO.getTaskCode());
        taskResult.setScore(taskResultDTO.getScore());
        taskResult.setAttempts(taskResultDTO.getAttempts());
        taskResult.setSuccessTime(taskResultDTO.getSuccessTime());
        return taskResult;
    }
}