package dev.fesly.impl.util.chat;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.color.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.*;
import java.util.Objects;
public final class ChatUtil {
    private static final Minecraft mc = Minecraft.getInstance();
    public static void addText(String text) {
        if (mc.player == null) return;
        java.awt.Color color1 = ClientUtil.getOneClientColor();
        java.awt.Color color2 = ClientUtil.getTwoClientColor();
        mc.ingameGUI.getChatGUI().printChatMessage(new StringTextComponent(TextFormatting.GRAY + "[" + TextFormatting.AQUA + TextFormatting.BOLD + "Nova" + TextFormatting.GRAY + "]" + TextFormatting.RESET + " " + replaceColorCodesInString(text)));
    }
    public static void addText(final Object message, final Object... objects) {
        if (mc.player == null) return;
        final String format = String.format(message.toString(), objects);
        addText(format);
    }
    public static StringTextComponent createGradientString(String text, java.awt.Color color1, java.awt.Color color2) {
        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        double colorStep = 1.0 / Math.max(1, (chars.length - 1));
        double currentStep = 0.0;
        for (char c : chars) {
            sb.append(c);
            currentStep += colorStep;
        }
        return new StringTextComponent(sb.toString());
    }
    public static TextFormatting blendColors(java.awt.Color color1, java.awt.Color color2, double ratio) {
        int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * ratio);
        int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * ratio);
        int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * ratio);
        int rgb = new java.awt.Color(r, g, b).getRGB();
        return TextFormatting.fromColorIndex(rgb);
    }
    public static void sendText(String text) {
        if (mc.player == null) return;
        mc.player.sendChatMessage(text.replace("§", "&"));
    }
    public static void sendText(final Object message, final Object... objects) {
        if (mc.player == null) return;
        final String format = String.format(message.toString(), objects);
        sendText(format);
    }
    public static String replaceColorCodesInString(String text) {
        if (text == null) return "";
        String str = text;
        str = str.replace("&4", String.valueOf(TextFormatting.DARK_RED)).replace("&c", String.valueOf(TextFormatting.RED)).replace("&6", String.valueOf(TextFormatting.GOLD)).replace("&e", String.valueOf(TextFormatting.YELLOW)).replace("&2", String.valueOf(TextFormatting.DARK_GREEN)).replace("&a", String.valueOf(TextFormatting.GREEN)).replace("&b", String.valueOf(TextFormatting.AQUA)).replace("&3", String.valueOf(TextFormatting.DARK_AQUA)).replace("&1", String.valueOf(TextFormatting.DARK_BLUE)).replace("&9", String.valueOf(TextFormatting.BLUE)).replace("&d", String.valueOf(TextFormatting.LIGHT_PURPLE)).replace("&5", String.valueOf(TextFormatting.DARK_PURPLE)).replace("&f", String.valueOf(TextFormatting.WHITE)).replace("&7", String.valueOf(TextFormatting.GRAY)).replace("&8", String.valueOf(TextFormatting.DARK_GRAY)).replace("&0", String.valueOf(TextFormatting.BLACK)).replace("&k", String.valueOf(TextFormatting.OBFUSCATED)).replace("&m", String.valueOf(TextFormatting.STRIKETHROUGH)).replace("&o", String.valueOf(TextFormatting.ITALIC)).replace("&l", String.valueOf(TextFormatting.BOLD)).replace("&n", String.valueOf(TextFormatting.UNDERLINE)).replace("&r", String.valueOf(TextFormatting.RESET));
        return str;
    }
    public static String removeColorCodes(String text) {
        String str = text;
        String[] colorcodes = new String[] {"4", "c", "6", "e", "2", "a", "b", "3", "1", "9", "d", "5", "f", "7", "8", "0", "k", "m", "o", "l", "n", "r"};
        for (String c : colorcodes) {
            str = str.replace("§" + c, "");
        }
        return str.trim();
    }
    @java.lang.SuppressWarnings("all")
    private ChatUtil() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
