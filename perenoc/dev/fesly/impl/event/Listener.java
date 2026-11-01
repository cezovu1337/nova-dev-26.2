package dev.fesly.impl.event;

@FunctionalInterface
public interface Listener<Event> {
    void call(Event event);
}