package ge.informatics.sandbox.model;

/**
 * Mirrors ge.freeuni.informatics.common.model.task.TaskType on the core side.
 * New constants must be appended - the two enums are matched by name over Kafka.
 */
public enum TaskType {
    BATCH,
    /**
     * The submission is linked against the task's grader sources and judged by the task's
     * manager, which it talks to over a FIFO pair rather than through files.
     */
    COMMUNICATION
}