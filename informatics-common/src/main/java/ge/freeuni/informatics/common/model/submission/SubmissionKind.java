package ge.freeuni.informatics.common.model.submission;

/**
 * What the contestant actually submitted.
 *
 * <p>An {@link #OUTPUT} submission carries the answers themselves rather than a program that
 * produces them, so there is nothing to compile and nothing to run: judging copies each file
 * into place and hands it straight to the checker.
 *
 * <p>Persisted by ordinal like the other enums here, so new constants must be appended.
 */
public enum SubmissionKind {
    SOURCE,
    OUTPUT
}
