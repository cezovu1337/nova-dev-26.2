package dev.fesly.impl.value.defaultValues;
import dev.fesly.Nova;
import dev.fesly.impl.interfaces.game.IMinecraft;
import dev.fesly.impl.interfaces.module.IToggleable;
import java.util.ArrayList;
import java.util.List;
public abstract class Mode<T> implements IMinecraft, IToggleable {
    private final String name;
    private final T parent;
    private final List<Value<?>> values = new ArrayList<>();
    public final void register() {
        Nova.getInstance().getEventBus().register(this);
        this.onEnable();
    }
    public final void unregister() {
        Nova.getInstance().getEventBus().unregister(this);
        this.onDisable();
    }
    @Override
    public void toggle() {
    }
    @Override
    public void onEnable() {
    }
    @Override
    public void onDisable() {
    }
    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }
    @java.lang.SuppressWarnings("all")
    public T getParent() {
        return this.parent;
    }
    @java.lang.SuppressWarnings("all")
    public List<Value<?>> getValues() {
        return this.values;
    }
    @java.lang.SuppressWarnings("all")
    public Mode(final String name, final T parent) {
        this.name = name;
        this.parent = parent;
    }
}
