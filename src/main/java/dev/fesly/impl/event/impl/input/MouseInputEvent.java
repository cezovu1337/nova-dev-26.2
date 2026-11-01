package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.Event;
public class MouseInputEvent implements Event {
    public final int mouseButton;
    @java.lang.SuppressWarnings("all")
    public int getMouseButton() {
        return this.mouseButton;
    }
    @java.lang.SuppressWarnings("all")
    public MouseInputEvent(final int mouseButton) {
        this.mouseButton = mouseButton;
    }
}
