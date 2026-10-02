package ge.freeuni.informatics.common.model.plagiarism;

/**
 * Lifecycle of one JPlag invocation (a task/language pair within a {@link PlagiarismJob}). New
 * constants must be appended - the ordinal is what is persisted.
 */
public enum PlagiarismRunStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}
