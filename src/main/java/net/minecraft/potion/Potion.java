package net.minecraft.potion;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class Potion {
    private final String name;

    public Potion(String name) {
        this.name = name;
    }

    public ITextComponent getDisplayName() {
        return new StringTextComponent(name);
    }
}
