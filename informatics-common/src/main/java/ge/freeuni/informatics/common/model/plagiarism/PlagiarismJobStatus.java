package ge.freeuni.informatics.common.model.plagiarism;

/**
 * Lifecycle of one admin-triggered plagiarism check. New constants must be appended - the
 * ordinal is what is persisted.
 */
public enum PlagiarismJobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}
