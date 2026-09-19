package ge.freeuni.informatics.common.events;

import org.springframework.context.ApplicationEvent;

public class SubmissionEvent extends ApplicationEvent {

    private final boolean rejudged;

    public SubmissionEvent(Object source) {
        this(source, false);
    }

    /**
     * @param rejudged true when this submission was scored again by an admin re-judge rather than
     *                 submitted by a contestant. The standings fold a new submission in
     *                 incrementally, which can only ever raise a score; a re-judge may equally
     *                 have lowered one, so it rebuilds the contestant's result for that task
     *                 instead. See {@code ContestService#addSubmission}.
     */
    public SubmissionEvent(Object source, boolean rejudged) {
        super(source);
        this.rejudged = rejudged;
    }

    public boolean isRejudged() {
        return rejudged;
    }
}
