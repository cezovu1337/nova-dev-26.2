package dev.fesly.impl.value.defaultValues.impl;
import dev.fesly.client.module.api.Module;
import dev.fesly.impl.value.defaultValues.Mode;
import dev.fesly.impl.value.defaultValues.Value;
import java.util.List;
import java.util.function.BooleanSupplier;
public class BooleanValue extends Value<Boolean> {
    private final Mode<?> mode;
    public BooleanValue(final String name, final Module parent, final Boolean defaultValue) {
        super(name, parent, defaultValue);
        this.mode = null;
    }
    public BooleanValue(final String name, final Mode<?> parent, final Boolean defaultValue) {
        super(name, parent, defaultValue);
        this.mode = null;
    }
    public BooleanValue(final String name, final Module parent, final Boolean defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
        this.mode = null;
    }
    public BooleanValue(final String name, final Mode<?> parent, final Boolean defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
        this.mode = null;
    }
    public BooleanValue(final String name, final Module parent, final Boolean defaultValue, final Mode<?> mode) {
        super(name, parent, defaultValue);
        this.mode = mode;
        getMode().getValues().forEach(value -> value.setHideIf(() -> !getValue()));
    }
    public BooleanValue(final String name, final Mode<?> parent, final Boolean defaultValue, final Mode<?> mode) {
        super(name, parent, defaultValue);
        this.mode = mode;
    }
    public BooleanValue(final String name, final Module parent, final Boolean defaultValue, final BooleanSupplier hideIf, final Mode<?> mode) {
        super(name, parent, defaultValue, hideIf);
        this.mode = mode;
    }
    public BooleanValue(final String name, final Mode<?> parent, final Boolean defaultValue, final BooleanSupplier hideIf, final Mode<?> mode) {
        super(name, parent, defaultValue, hideIf);
        this.mode = mode;
    }
    @Override
    public void setValue(final Boolean value) {
        super.setValue(value);
        if (this.mode != null && this.getParent() != null) {
            if (this.getParent().isEnabled()) {
                if (this.getValue()) {
                    this.mode.register();
                } else {
                    this.mode.unregister();
                }
            }
        }
    }
    @Override
    public List<Value<?>> getSubValues() {
        if (getMode() == null) return null;
         else return getMode().getValues();
    }
    @Override
    public void setValueAsObject(final Object value) {
        super.setValueAsObject(value);
        if (this.mode != null && this.getParent() != null) {
            if (this.getParent().isEnabled() && this.getValue()) {
                this.mode.onEnable();
            }
        }
    }
    @java.lang.SuppressWarnings("all")
    public Mode<?> getMode() {
        return this.mode;
    }
}
