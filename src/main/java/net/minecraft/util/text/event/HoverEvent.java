package net.minecraft.util.text.event;

public class HoverEvent {
    public enum Action {
        SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY
    }

    private final Action action;
    private final Object value;

    public HoverEvent(Action action, Object value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return this.action;
    }

    public Object getValue() {
        return this.value;
    }
}
