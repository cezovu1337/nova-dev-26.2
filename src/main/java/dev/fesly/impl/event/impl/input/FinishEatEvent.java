package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.CancellableEvent;
import net.minecraft.world.item.ItemStack;
public class FinishEatEvent extends CancellableEvent {
    public final ItemStack item;
    @java.lang.SuppressWarnings("all")
    public FinishEatEvent(final ItemStack item) {
        this.item = item;
    }
    @java.lang.SuppressWarnings("all")
    public ItemStack getItem() {
        return this.item;
    }
}
