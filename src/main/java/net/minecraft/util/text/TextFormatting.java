package net.minecraft.util.text;

public enum TextFormatting {
    BLACK("BLACK", '0', 0),
    DARK_BLUE("DARK_BLUE", '1', 1),
    DARK_GREEN("DARK_GREEN", '2', 2),
    DARK_AQUA("DARK_AQUA", '3', 3),
    DARK_RED("DARK_RED", '4', 4),
    DARK_PURPLE("DARK_PURPLE", '5', 5),
    GOLD("GOLD", '6', 6),
    GRAY("GRAY", '7', 7),
    DARK_GRAY("DARK_GRAY", '8', 8),
    BLUE("BLUE", '9', 9),
    GREEN("GREEN", 'a', 10),
    AQUA("AQUA", 'b', 11),
    RED("RED", 'c', 12),
    LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13),
    YELLOW("YELLOW", 'e', 14),
    WHITE("WHITE", 'f', 15),
    OBFUSCATED("OBFUSCATED", 'k', true),
    BOLD("BOLD", 'l', true),
    STRIKETHROUGH("STRIKETHROUGH", 'm', true),
    UNDERLINE("UNDERLINE", 'n', true),
    ITALIC("ITALIC", 'o', true),
    RESET("RESET", 'r', -1);

    private final String name;
    private final char code;
    private final int colorIndex;

    TextFormatting(String name, char code, int colorIndex) {
        this.name = name;
        this.code = code;
        this.colorIndex = colorIndex;
    }

    TextFormatting(String name, char code, boolean isFormat) {
        this.name = name;
        this.code = code;
        this.colorIndex = -1;
    }

    public static TextFormatting fromColorIndex(int colorIndex) {
        for (TextFormatting tf : values()) {
            if (tf.colorIndex == colorIndex) {
                return tf;
            }
        }
        return WHITE;
    }

    public static String getTextWithoutFormattingCodes(String text) {
        return text == null ? null : text.replaceAll("(?i)\u00a7[0-9A-FK-OR]", "");
    }

    @Override
    public String toString() {
        return "\u00a7" + this.code;
    }
}
