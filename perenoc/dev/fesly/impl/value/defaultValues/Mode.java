package dev.fesly.impl.value.defaultValues;

import dev.fesly.Nova;
import dev.fesly.impl.interfaces.game.IMinecraft;
import dev.fesly.impl.interfaces.module.IToggleable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
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
}