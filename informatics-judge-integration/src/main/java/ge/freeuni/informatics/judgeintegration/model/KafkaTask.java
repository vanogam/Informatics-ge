package ge.freeuni.informatics.judgeintegration.model;

import ge.freeuni.informatics.common.model.CodeLanguage;

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
        Stage stage
)   {

}
