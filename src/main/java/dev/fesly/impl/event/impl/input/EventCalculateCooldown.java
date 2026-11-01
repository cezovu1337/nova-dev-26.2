package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.Event;
import net.minecraft.world.item.Item;
public class EventCalculateCooldown implements Event {
    private final Item stack;
    private float cooldown;
    public EventCalculateCooldown(Item stack) {
        this.stack = stack;
    }
    public float getCooldown() {
        return this.cooldown;
    }
    public void setCooldown(float f) {
        this.cooldown = f;
    }
    public Item getStack() {
        return this.stack;
    }
}
