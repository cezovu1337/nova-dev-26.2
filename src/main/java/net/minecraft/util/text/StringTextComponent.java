package net.minecraft.util.text;

public class StringTextComponent implements ITextComponent {
    public static final ITextComponent EMPTY = new StringTextComponent("");
    private final String text;

    public StringTextComponent(String text) {
        this.text = text;
    }

    public StringTextComponent setStyle(Style style) {
        return this;
    }

    public StringTextComponent append(ITextComponent component) {
        return this;
    }

    @Override
    public String getString() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
