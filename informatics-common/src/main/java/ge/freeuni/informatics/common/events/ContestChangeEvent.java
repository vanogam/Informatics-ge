package ge.freeuni.informatics.common.events;

import org.springframework.context.ApplicationEvent;

public class ContestChangeEvent extends ApplicationEvent {

    public ContestChangeEvent(Object source) {
        super(source);
    }
}
