package dev.fesly.impl.value.defaultValues;
import dev.fesly.client.module.api.Module;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
public abstract class Value<T> {
    private final String name;
    public BooleanSupplier hideIf;
    private T value;
    private boolean visible;
    private Module parent;
    private Consumer<T> valueChangeConsumer;
    private T defaultValue;
    public Value(final String name, final Module parent, final T defaultValue) {
        this.name = name;
        this.hideIf = null;
        this.parent = parent;
        this.defaultValue = defaultValue;
        this.setValue(defaultValue);
        parent.getValues().add(this);
    }
    public Value(final String name, final Mode<?> parent, final T defaultValue) {
        this.name = name;
        this.hideIf = null;
        this.defaultValue = defaultValue;
        this.setValue(defaultValue);
        parent.getValues().add(this);
    }
    public Value(final String name, final Module parent, final T defaultValue, final BooleanSupplier hideIf) {
        this.name = name;
        this.hideIf = hideIf;
        this.parent = parent;
        this.defaultValue = defaultValue;
        this.setValue(defaultValue);
        parent.getValues().add(this);
    }
    public Value(final String name, final Mode<?> parent, final T defaultValue, final BooleanSupplier hideIf) {
        this.name = name;
        this.hideIf = hideIf;
        this.defaultValue = defaultValue;
        this.setValue(defaultValue);
        parent.getValues().add(this);
    }
    public void setValueAsObject(final Object value) {
        if (this.valueChangeConsumer != null) this.valueChangeConsumer.accept((T) value);
        this.value = (T) value;
    }
    public void setValue(final T value) {
        if (this.valueChangeConsumer != null) this.valueChangeConsumer.accept(value);
        this.value = value;
    }
    public float getFloatValue() {
        return (float) value;
    }
    public abstract List<Value<?>> getSubValues();
    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }
    @java.lang.SuppressWarnings("all")
    public BooleanSupplier getHideIf() {
        return this.hideIf;
    }
    @java.lang.SuppressWarnings("all")
    public T getValue() {
        return this.value;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isVisible() {
        return this.visible;
    }
    @java.lang.SuppressWarnings("all")
    public Module getParent() {
        return this.parent;
    }
    @java.lang.SuppressWarnings("all")
    public Consumer<T> getValueChangeConsumer() {
        return this.valueChangeConsumer;
    }
    @java.lang.SuppressWarnings("all")
    public T getDefaultValue() {
        return this.defaultValue;
    }
    @java.lang.SuppressWarnings("all")
    public void setHideIf(final BooleanSupplier hideIf) {
        this.hideIf = hideIf;
    }
    @java.lang.SuppressWarnings("all")
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
    @java.lang.SuppressWarnings("all")
    public void setParent(final Module parent) {
        this.parent = parent;
    }
    @java.lang.SuppressWarnings("all")
    public void setValueChangeConsumer(final Consumer<T> valueChangeConsumer) {
        this.valueChangeConsumer = valueChangeConsumer;
    }
    @java.lang.SuppressWarnings("all")
    public void setDefaultValue(final T defaultValue) {
        this.defaultValue = defaultValue;
    }
}
