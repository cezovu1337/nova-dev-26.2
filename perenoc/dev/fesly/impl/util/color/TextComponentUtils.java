package dev.fesly.impl.util.color;

import net.minecraft.util.text.*;
import net.minecraft.world.BossInfo;

public class TextComponentUtils {

    public static String extractFormattedText(ITextComponent component) {
        StringBuilder builder = new StringBuilder();
        extractFormattedTextRecursive(component, builder, TextFormatting.WHITE);
        return builder.toString();
    }

    private static void extractFormattedTextRecursive(ITextComponent component, StringBuilder builder, TextFormatting parentFormat) {
        TextFormatting currentFormat = parentFormat;
        if (component.getStyle().getColor() != null) {
            currentFormat = TextFormatting.fromColorIndex( component.getStyle().getColor().getColor());
        }

        if (component instanceof StringTextComponent) {
            String text = ((StringTextComponent) component).getText();
            builder.append(currentFormat).append(text);
        } else if (component instanceof TranslationTextComponent) {
            TranslationTextComponent translatable = (TranslationTextComponent) component;
            for (Object arg : translatable.getFormatArgs()) {
                if (arg instanceof ITextComponent) {
                    extractFormattedTextRecursive((ITextComponent) arg, builder, currentFormat);
                }
            }
        } else if (component instanceof TextComponent) {
            for (ITextComponent child : ((TextComponent) component).getSiblings()) {
                extractFormattedTextRecursive(child, builder, currentFormat);
            }
        }
    }

    public static void main(String[] args) {
        ITextComponent component = new StringTextComponent("Hello ")
                .append(new StringTextComponent("Minecraft").setStyle(Style.EMPTY.setColor(Color.fromInt(java.awt.Color.BLUE.getRGB()))))
                .append(new StringTextComponent(" World!").setStyle(Style.EMPTY.setColor(Color.fromInt(java.awt.Color.RED.getRGB()))));

        String extractedText = extractFormattedText(component);
        System.out.println(component.getString());
    }
}