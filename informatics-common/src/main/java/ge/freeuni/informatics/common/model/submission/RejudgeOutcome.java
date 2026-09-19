package ge.freeuni.informatics.common.model.submission;

/**
 * Whether a single submission was taken up for re-judging.
 */
public enum RejudgeOutcome {
    /** The submission was re-queued, or - for a rescore - has already been rescored. */
    ACCEPTED,
    /** Nothing was done; the reason is carried alongside as a message code. */
    REFUSED
}
