package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.CancellableEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.ItemStack;

@RequiredArgsConstructor
public class FinishEatEvent extends CancellableEvent {
    @Getter
    public final ItemStack item;
}
