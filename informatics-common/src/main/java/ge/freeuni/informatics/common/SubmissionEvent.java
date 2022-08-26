package ge.freeuni.informatics.common;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public class SubmissionEvent extends ApplicationEvent {

    public SubmissionEvent(Object source) {
        super(source);
    }

    public SubmissionEvent(Object source, Clock clock) {
        super(source, clock);
    }
}
