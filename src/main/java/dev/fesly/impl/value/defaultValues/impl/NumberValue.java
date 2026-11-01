package dev.fesly.impl.value.defaultValues.impl;
import dev.fesly.client.module.api.Module;
import dev.fesly.impl.value.defaultValues.Mode;
import dev.fesly.impl.value.defaultValues.Value;
import java.util.List;
import java.util.function.BooleanSupplier;
public class NumberValue extends Value<Number> {
    private final Number min;
    private final Number max;
    private final Number decimalPlaces;
    public NumberValue(final String name, final Module parent, final Number defaultValue, final Number min, final Number max, final Number decimalPlaces) {
        super(name, parent, defaultValue);
        this.decimalPlaces = decimalPlaces;
        this.min = min;
        this.max = max;
    }
    public NumberValue(final String name, final Mode<?> parent, final Number defaultValue, final Number min, final Number max, final Number decimalPlaces) {
        super(name, parent, defaultValue);
        this.decimalPlaces = decimalPlaces;
        this.min = min;
        this.max = max;
    }
    public NumberValue(final String name, final Module parent, final Number defaultValue, final Number min, final Number max, final Number decimalPlaces, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
        this.decimalPlaces = decimalPlaces;
        this.min = min;
        this.max = max;
    }
    public NumberValue(final String name, final Mode<?> parent, final Number defaultValue, final Number min, final Number max, final Number decimalPlaces, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
        this.decimalPlaces = decimalPlaces;
        this.min = min;
        this.max = max;
    }
    @Override
    public List<Value<?>> getSubValues() {
        return null;
    }
    @java.lang.SuppressWarnings("all")
    public Number getMin() {
        return this.min;
    }
    @java.lang.SuppressWarnings("all")
    public Number getMax() {
        return this.max;
    }
    @java.lang.SuppressWarnings("all")
    public Number getDecimalPlaces() {
        return this.decimalPlaces;
    }
}
