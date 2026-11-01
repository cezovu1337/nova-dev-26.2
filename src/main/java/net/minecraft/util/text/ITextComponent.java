package net.minecraft.util.text;

public interface ITextComponent {
    String getString();

    default ITextComponent deepCopy() {
        return this;
    }

    default ITextComponent mergeStyle(TextFormatting formatting) {
        return this;
    }

    default java.util.List<? extends ITextComponent> getSiblings() {
        return java.util.Collections.emptyList();
    }

    default net.minecraft.network.chat.Style getStyle() {
        return net.minecraft.network.chat.Style.EMPTY;
    }

    static ITextComponent getTextComponentOrEmpty(String text) {
        return new StringTextComponent(text != null ? text : "");
    }
}
