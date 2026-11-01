package dev.fesly.impl.value.defaultValues.impl;

import dev.fesly.impl.value.defaultValues.Mode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubMode extends Mode<SubMode> {
    public SubMode(String name) {
        super(name, null);
    }
}