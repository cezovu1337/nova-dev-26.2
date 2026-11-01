package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.Event;
public class ChatReceiveEvent implements Event {
    public final String text;
    @java.lang.SuppressWarnings("all")
    public ChatReceiveEvent(final String text) {
        this.text = text;
    }
    @java.lang.SuppressWarnings("all")
    public String getText() {
        return this.text;
    }
}
