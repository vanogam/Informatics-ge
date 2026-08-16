package ge.freeuni.informatics.common.model.task;

/**
 * Kinds of task-supplied source files that the judge needs.
 * New constants must be appended - the ordinal is what is persisted.
 */
public enum TaskFileKind {
    /**
     * Compiled together with the submission. Lives in the task's "graders" directory.
     */
    GRADER("graders"),
    /**
     * Compiled and run alongside the submission, connected over a FIFO pair.
     * Lives in the task's "manager" directory.
     */
    MANAGER("manager"),
    /**
     * Compiled and run after the submission to score its output, for tasks whose
     * {@link CheckerType} is CUSTOM. Lives in the task's "checker" directory.
     */
    CHECKER("checker");

    private final String directory;

    TaskFileKind(String directory) {
        this.directory = directory;
    }

    /**
     * Name of the directory holding files of this kind, relative to the task's file root.
     */
    public String getDirectory() {
        return directory;
    }
}