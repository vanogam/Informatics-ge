package ge.freeuni.informatics.judgeintegration.model;

public record KafkaCallback (
        String submissionId,
        CallbackType messageType,
        String testcaseKey,
        String message
){

}
