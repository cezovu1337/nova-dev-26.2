package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;


@Getter
@AllArgsConstructor
public final class KeyboardInputEvent implements Event {
    private final int keyCode;
    private final Screen screen;
}
