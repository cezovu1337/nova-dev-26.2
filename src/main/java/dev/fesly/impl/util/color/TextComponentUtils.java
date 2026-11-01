package dev.fesly.impl.util.color;

import net.minecraft.util.text.ITextComponent;

public class TextComponentUtils {
    public static String extractFormattedText(ITextComponent component) {
        return component != null ? component.getString() : "";
    }
}
