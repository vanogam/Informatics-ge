package ge.freeuni.informatics.common.model.contest;

public enum ScoringType {
    BEST_SUBMISSION,
    LAST_SUBMISSION,
    /**
     * IOI-style: a task's score is the sum of the contestant's best result on each subtask,
     * taken across all of their submissions - so a contestant may solve one subtask per
     * submission and still end on full marks. Appended deliberately: the enum is persisted by
     * ordinal, and the contest table's check constraint is widened to match in V1.15.
     */
    SUBTASK_MAX;
}
