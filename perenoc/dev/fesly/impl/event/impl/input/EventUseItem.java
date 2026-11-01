package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.CancellableEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Item;

@RequiredArgsConstructor
public class EventUseItem extends CancellableEvent {
    @Getter
    public final Item item;
}
