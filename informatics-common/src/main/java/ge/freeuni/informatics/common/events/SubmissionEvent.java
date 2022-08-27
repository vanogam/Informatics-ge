package ge.freeuni.informatics.common.events;

import org.springframework.context.ApplicationEvent;

public class SubmissionEvent extends ApplicationEvent {

    public SubmissionEvent(Object source) {
        super(source);
    }

}
