package dev.fesly.impl.event;

public interface Event {
    default boolean callOutsideOfGame() {
        return false;
    }
}
