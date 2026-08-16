package ge.freeuni.informatics.common.model.task;

/**
 * New constants must be appended - the ordinal is what is persisted, and existing
 * migrations (see V1.8) reference these values by ordinal.
 */
public enum CheckerType {
    TOKEN,
    YES_NO,
    LINES,
    DOUBLE_E6,
    DOUBLE_E9,
    CUSTOM,
    /**
     * The evaluator is the task's manager, run alongside the submission rather than after it.
     * Only meaningful for {@link TaskType#COMMUNICATION}.
     */
    MANAGER;

}