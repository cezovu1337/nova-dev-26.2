package dev.fesly.impl.value.defaultValues.impl;
import dev.fesly.client.module.api.Module;
import dev.fesly.impl.value.defaultValues.Mode;
import dev.fesly.impl.value.defaultValues.Value;
import java.util.List;
import java.util.function.BooleanSupplier;
public class BoundsNumberValue extends Value<Number> {
    private final Number min;
    private final Number max;
    private final Number decimalPlaces;
    private Number secondValue;
    public BoundsNumberValue(final String name, final Module parent, final Number defaultValue, final Number defaultSecondValue, final Number min, final Number max, final Number step) {
        super(name, parent, defaultValue);
        this.decimalPlaces = step;
        this.min = min;
        this.max = max;
        this.secondValue = defaultSecondValue;
    }
    public BoundsNumberValue(final String name, final Mode<?> parent, final Number defaultValue, final Number defaultSecondValue, final Number min, final Number max, final Number step) {
        super(name, parent, defaultValue);
        this.decimalPlaces = step;
        this.min = min;
        this.max = max;
        this.secondValue = defaultSecondValue;
    }
    public BoundsNumberValue(final String name, final Module parent, final Number defaultValue, final Number defaultSecondValue, final Number min, final Number max, final Number step, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
        this.decimalPlaces = step;
        this.min = min;
        this.max = max;
        this.secondValue = defaultSecondValue;
    }
    public BoundsNumberValue(final String name, final Mode<?> parent, final Number defaultValue, final Number defaultSecondValue, final Number min, final Number max, final Number step, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
        this.decimalPlaces = step;
        this.min = min;
        this.max = max;
        this.secondValue = defaultSecondValue;
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
    @java.lang.SuppressWarnings("all")
    public Number getSecondValue() {
        return this.secondValue;
    }
    @java.lang.SuppressWarnings("all")
    public void setSecondValue(final Number secondValue) {
        this.secondValue = secondValue;
    }
}
