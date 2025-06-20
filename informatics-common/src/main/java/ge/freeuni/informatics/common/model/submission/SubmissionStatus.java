package ge.freeuni.informatics.common.model.submission;

public enum SubmissionStatus {
    IN_QUEUE,
    RUNNING,
    COMPILING,
    FINISHED,
    COMPILATION_ERROR,
    TIME_LIMIT_EXCEEDED,
    MEMORY_LIMIT_EXCEEDED,
    RUNTIME_ERROR,
    WRONG_ANSWER,
    FAILED,
    PARTIAL,
    CORRECT
}
