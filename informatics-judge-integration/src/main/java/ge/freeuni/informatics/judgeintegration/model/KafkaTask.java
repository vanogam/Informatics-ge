package ge.freeuni.informatics.judgeintegration.model;

import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.task.CheckerType;
import ge.freeuni.informatics.common.model.task.TaskType;

public record KafkaTask(
        String taskId,
        String contestId,
        String submissionId,
        String submissionName,
        CodeLanguage language,
        long timeLimitMillis,
        int memoryLimitKB,
        String testId,
        String inputName,
        String outputName,
        CheckerType checkerType,
        TaskType taskType,
        int numProcesses,
        Stage stage
)   {

}
