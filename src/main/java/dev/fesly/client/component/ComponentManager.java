package dev.fesly.client.component;
import dev.fesly.Nova;
import java.util.ArrayList;
public final class ComponentManager extends ArrayList<Component> {
    public void init() {
        this.registerToEventBus();
    }
    public void registerToEventBus() {
        for (final Component component : this) {
            Nova.getInstance().getEventBus().register(component);
        }
    }
}
