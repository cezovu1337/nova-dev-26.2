package dev.fesly.impl.value.defaultValues.impl;

import dev.fesly.client.module.api.Module;
import dev.fesly.impl.value.defaultValues.Mode;
import dev.fesly.impl.value.defaultValues.Value;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.BooleanSupplier;

@Getter
@Setter
public class StringValue extends Value<String> {

    public StringValue(final String name, final Module parent, final String defaultValue) {
        super(name, parent, defaultValue);
    }

    public StringValue(final String name, final Mode<?> parent, final String defaultValue) {
        super(name, parent, defaultValue);
    }

    public StringValue(final String name, final Module parent, final String defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
    }

    public StringValue(final String name, final Mode<?> parent, final String defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
    }

    @Override
    public List<Value<?>> getSubValues() {
        return null;
    }
}