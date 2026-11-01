package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class MouseInputEvent implements Event {
    public final int mouseButton;
}
