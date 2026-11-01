package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.CancellableEvent;
import net.minecraft.world.item.Item;
public class EventUseItem extends CancellableEvent {
    public final Item item;
    @java.lang.SuppressWarnings("all")
    public EventUseItem(final Item item) {
        this.item = item;
    }
    @java.lang.SuppressWarnings("all")
    public Item getItem() {
        return this.item;
    }
}
