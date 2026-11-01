package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ChatReceiveEvent implements Event {
    public final String text;
}
