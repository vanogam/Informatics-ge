package ge.freeuni.informatics.judgeintegration.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ge.freeuni.informatics.common.model.submission.TestStatus;

public record KafkaCallback(
        @JsonProperty("submissionId") Long submissionId,
        @JsonProperty("messageType") CallbackType messageType,
        @JsonProperty("testcaseKey") String testcaseKey,
        @JsonProperty("message") String message,
        @JsonProperty("score") Long score,
        @JsonProperty("status") TestStatus status,
        @JsonProperty("exitCode") Integer exitCode,
        @JsonProperty("timeMillis") Long timeMillis,
        @JsonProperty("memoryKB") Long memoryKB
) {
    @JsonCreator
    public KafkaCallback {
    }
}