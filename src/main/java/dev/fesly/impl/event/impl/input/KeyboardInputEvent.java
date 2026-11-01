package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.Event;
import net.minecraft.client.gui.screens.Screen;

public final class KeyboardInputEvent implements Event {
    private final int keyCode;
    private final Screen screen;

    public int getKeyCode() {
        return this.keyCode;
    }

    public Screen getScreen() {
        return this.screen;
    }

    public KeyboardInputEvent(final int keyCode, final Screen screen) {
        this.keyCode = keyCode;
        this.screen = screen;
    }
}
