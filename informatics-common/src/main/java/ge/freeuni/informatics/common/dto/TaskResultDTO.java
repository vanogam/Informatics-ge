package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.contest.TaskResult;

import java.util.Map;

public record TaskResultDTO(
        String taskCode,
        Float score,
        Integer attempts,
        Long successTime
) {
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
        taskResult.setTaskCode(taskResultDTO.taskCode());
        taskResult.setScore(taskResultDTO.score());
        taskResult.setAttempts(taskResultDTO.attempts());
        taskResult.setSuccessTime(taskResultDTO.successTime());
        return taskResult;
    }
}