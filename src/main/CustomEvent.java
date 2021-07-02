package main;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

class CustomEvent extends Event {

    public CustomEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public CustomEvent(Object o, EventTarget eventTarget, EventType<? extends Event> eventType) {
        super(o, eventTarget, eventType);
    }

    public static final EventType<CustomEvent> STOP = new EventType<>("PAUSE");
    public static final EventType<CustomEvent> START = new EventType<>("START");

}
