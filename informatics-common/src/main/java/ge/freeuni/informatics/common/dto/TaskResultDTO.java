package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.contest.TaskResult;

public class TaskResultDTO {
        private String taskCode;
        private Float score;
        private Integer attempts;
        private Long successTime;

    // Default constructor for Jackson
    public TaskResultDTO() {
    }

    public TaskResultDTO(String taskCode, Float score) {
        this.taskCode = taskCode;
        this.score = score;
        this.attempts = 0;
        this.successTime = null;
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

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
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