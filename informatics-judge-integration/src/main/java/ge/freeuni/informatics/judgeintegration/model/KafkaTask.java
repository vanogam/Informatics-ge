package ge.freeuni.informatics.judgeintegration.model;

import ge.freeuni.informatics.common.model.CodeLanguage;

public record KafkaTask(
        String taskId,
        String contestId,
        String submissionId,
        CodeLanguage language,
        long timeLimitMillis,
        int memoryLimitKB,
        String testId,
        CheckerType checkerType,
        Stage stage
)   {

}
