package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.Event;

public class MouseClickedEvent implements Event {
    private int button;

    public MouseClickedEvent() {
    }

    public MouseClickedEvent(int button) {
        this.button = button;
    }

    public int getButton() {
        return this.button;
    }
}
