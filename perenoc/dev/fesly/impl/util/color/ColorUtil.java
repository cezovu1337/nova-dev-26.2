package dev.fesly.impl.util.color;

import dev.fesly.impl.util.chat.ChatUtil;
import dev.fesly.impl.util.math.MathUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    /**
     * Method which colors using a hex code
     *
     * @param hex used hex code
     */
    public static void glColor(final int hex) {
        final float a = (hex >> 24 & 0xFF) / 255.0F;
        final float r = (hex >> 16 & 0xFF) / 255.0F;
        final float g = (hex >> 8 & 0xFF) / 255.0F;
        final float b = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(r, g, b, a);
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static String replaceColorCodesInString(String text) {
        String pattern = "&#([0-9a-fA-F]{6});";
        Pattern colorCodePattern = Pattern.compile(pattern);
        Matcher matcher = colorCodePattern.matcher(text);

        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            String colorCode = matcher.group(1);
            String replacement = getByColorCode(colorCode);
            matcher.appendReplacement(stringBuffer, replacement);
        }
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    public static String getByColorCode(String colorCode) {
        Color color = hexToColor(colorCode);

        if (color != null) {
            int red = color.getRed();
            int green = color.getGreen();
            int blue = color.getBlue();

            return ChatUtil.replaceColorCodesInString( getColorFormatting(red, green, blue));
        }

        return "";
    }

    private static Color hexToColor(String hexCode) {
        try {
            int rgb = Integer.parseInt(hexCode, 16);
            return new Color(rgb);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String getColorFormatting(int red, int green, int blue) {
        int index = getColorIndex(red, green, blue);
        if (index != -1) {
            return "§" + index;
        }
        return "";
    }

    private static int getColorIndex(int red, int green, int blue) {
        int[] colorValues = {0, 170, 435, 600, 765, 930, 1095, 1260, 1425, 1590, 1755, 1920, 2085, 2250, 2415};
        int colorIndex = -1;

        for (int i = 0; i < colorValues.length; i++) {
            if (red == colorValues[i] && green == colorValues[i] && blue == colorValues[i]) {
                colorIndex = i;
                break;
            }
        }

        return colorIndex;
    }

    /**
     * Method which colors using a color
     *
     * @param color used color
     */
    public static void glColor(final Color color) {
        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }

    public static Color darker(final Color color, final float factor) {
        return new Color(Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0),
                color.getAlpha());
    }

    public static Color brighter(final Color color, final float factor) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        final int alpha = color.getAlpha();

        final int i = (int) (1 / (1 - factor));
        if (red == 0 && green == 0 && blue == 0) {
            return new Color(i, i, i, alpha);
        }

        if (red > 0 && red < i) red = i;
        if (green > 0 && green < i) green = i;
        if (blue > 0 && blue < i) blue = i;

        return new Color(Math.min((int) (red / factor), 255),
                Math.min((int) (green / factor), 255),
                Math.min((int) (blue / factor), 255),
                alpha);
    }

    public static Color withRed(final Color color, final int red) {
        return new Color(red, color.getGreen(), color.getBlue());
    }

    public static Color withGreen(final Color color, final int green) {
        return new Color(color.getRed(), green, color.getBlue());
    }

    public static Color withBlue(final Color color, final int blue) {
        return new Color(color.getRed(), color.getGreen(), blue);
    }

    public static Color withAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) MathUtil.clamp(0, 255, alpha));
    }

    public static int fade(Color color, int delay) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs(((float) (System.currentTimeMillis() % 2000L + delay) / 1000.0F) % 2F - 1.0F);
        brightness = 0.5F + 0.5F * brightness;
        hsb[2] = brightness % 2.0F;
        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

    public static Color fade(int speed, int index, Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;

        Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360f));

        return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, color.getAlpha())));
    }

    public static Color getRandomColor() {
        return new Color(Color.HSBtoRGB((float) Math.random(), (float) (.5 + Math.random() / 2), (float) (.5 + Math.random() / 2f)));
    }

    public static Color hslToRGB(float[] hsl) {
        float red, green, blue;

        if (hsl[1] == 0) {
            red = green = blue = 1;
        } else {
            float q = hsl[2] < .5 ? hsl[2] * (1 + hsl[1]) : hsl[2] + hsl[1] - hsl[2] * hsl[1];
            float p = 2 * hsl[2] - q;

            red = hueToRGB(p, q, hsl[0] + 1 / 3f);
            green = hueToRGB(p, q, hsl[0]);
            blue = hueToRGB(p, q, hsl[0] - 1 / 3f);
        }

        red *= 255;
        green *= 255;
        blue *= 255;

        return new Color((int) red, (int) green, (int) blue);
    }

    public static float hueToRGB(float p, float q, float t) {
        float newT = t;
        if (newT < 0) newT += 1;
        if (newT > 1) newT -= 1;
        if (newT < 1 / 6f) return p + (q - p) * 6 * newT;
        if (newT < .5f) return q;
        if (newT < 2 / 3f) return p + (q - p) * (2 / 3f - newT) * 6;
        return p;
    }

    public static float[] rgbToHSL(Color rgb) {
        float red = rgb.getRed() / 255f;
        float green = rgb.getGreen() / 255f;
        float blue = rgb.getBlue() / 255f;

        float max = Math.max(Math.max(red, green), blue);
        float min = Math.min(Math.min(red, green), blue);
        float c = (max + min) / 2f;
        float[] hsl = new float[]{c, c, c};

        if (max == min) {
            hsl[0] = hsl[1] = 0;
        } else {
            float d = max - min;
            hsl[1] = hsl[2] > .5 ? d / (2 - max - min) : d / (max + min);

            if (max == red) {
                hsl[0] = (green - blue) / d + (green < blue ? 6 : 0);
            } else if (max == blue) {
                hsl[0] = (blue - red) / d + 2;
            } else if (max == green) {
                hsl[0] = (red - green) / d + 4;
            }
            hsl[0] /= 6;
        }
        return hsl;
    }

    public static Color mixColors(final Color color1, final Color color2, final double percent) {
        final double inverse_percent = 1.0 - percent;
        int red = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        int green = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        int blue = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));
        return new Color(red, green, blue);
    }

    public static Color interpolateTwoColors(int speed, int index, Color start, Color end) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return ColorUtil.interpolateColor(start, end, angle / 360f);
    }

    public static Color interpolateColor(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(MathUtil.interpolate(color1.getRed(), color2.getRed(), amount).intValue(),
                MathUtil.interpolate(color1.getGreen(), color2.getGreen(), amount).intValue(),
                MathUtil.interpolate(color1.getBlue(), color2.getBlue(), amount).intValue(),
                MathUtil.interpolate(color1.getAlpha(), color2.getAlpha(), amount).intValue());
    }

    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                Math.max(0, Math.min(255, (int) (opacity * 255))));
    }
    public static int rainbow(int delay, double speed) {
        double rainbow = Math.ceil((System.currentTimeMillis() + delay) / speed);
        rainbow %= 360.0D;
        return Color.getHSBColor((float) -((rainbow / 360.0F)), 0.9F, 1.0F).getRGB();
    }

    public static Color skyRainbow(int speed, int index) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        return Color.getHSBColor((double) ((float) ((angle %= 360.0) / 360.0)) < 0.5 ? -((float) (angle / 360.0))
                : (float) (angle / 360.0), 0.5F, 1.0F);
    }

    public static int getColor(int red, int green, int blue) {
        return ColorUtil.getColor(red, green, blue, 255);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        return color |= blue;
    }

    public static int r(int color) {
        return color >> 16 & 0xFF;
    }

    public static int g(int color) {
        return color >> 8 & 0xFF;
    }

    public static int b(int color) {
        return color & 0xFF;
    }

    public static int a(int color) {
        return color >> 24 & 0xFF;
    }

    public static int getRedFromColor(int color) {
        return color >> 16 & 0xFF;
    }

    public static int getGreenFromColor(int color) {
        return color >> 8 & 0xFF;
    }

    public static int getBlueFromColor(int color) {
        return color & 0xFF;
    }

    public static int getAlphaFromColor(int color) {
        return color >> 24 & 0xFF;
    }

    public static int getGLRedFromColor(int color) {
        return (color >> 16 & 0xFF) / 255;
    }

    public static float getGLGreenFromColor(int color) {
        return (color >> 8 & 0xFF) / 255;
    }

    public static float getGLBlueFromColor(int color) {
        return (color & 0xFF) / 255;
    }

    public static float getGLAlphaFromColor(int color) {
        return (color >> 24 & 0xFF) / 255;
    }

    public static int getHue(int red, int green, int blue) {
        float max;
        float min = Math.min(Math.min(red, green), blue);
        if (min == (max = (float)Math.max(Math.max(red, green), blue))) {
            return 0;
        }
        float hue = 0.0f;
        hue = max == (float)red ? (float)(green - blue) / (max - min) : (max == (float)green ? 2.0f + (float)(blue - red) / (max - min) : 4.0f + (float)(red - green) / (max - min));
        if ((hue *= 60.0f) < 0.0f) {
            hue += 360.0f;
        }
        return Math.round(hue);
    }

    public static int getHueFromColor(int color) {
        return ColorUtil.getHue(ColorUtil.getRedFromColor(color), ColorUtil.getGreenFromColor(color), ColorUtil.getBlueFromColor(color));
    }

    public static float getFullyBrightnessFromColor(int color) {
        float max = 765.0f;
        float main = (float)ColorUtil.getRedFromColor(color) * 1.0f + (float)ColorUtil.getGreenFromColor(color) * 1.0f + (float)ColorUtil.getBlueFromColor(color) * 1.0f;
        return main / max;
    }

    public static float getSaturateFromColor(int color) {
        float[] athsb = Color.RGBtoHSB(ColorUtil.getRedFromColor(color), ColorUtil.getGreenFromColor(color), ColorUtil.getBlueFromColor(color), null);
        return athsb[1];
    }

    public static float getBrightnessFromColor(int color) {
        float[] athsb = Color.RGBtoHSB(ColorUtil.getRedFromColor(color), ColorUtil.getGreenFromColor(color), ColorUtil.getBlueFromColor(color), null);
        return athsb[2];
    }

    public static int getOverallColorFrom(int color1, int color2) {
        int red1 = ColorUtil.getRedFromColor(color1);
        int green1 = ColorUtil.getGreenFromColor(color1);
        int blue1 = ColorUtil.getBlueFromColor(color1);
        int alpha1 = ColorUtil.getAlphaFromColor(color1);
        int red2 = ColorUtil.getRedFromColor(color2);
        int green2 = ColorUtil.getGreenFromColor(color2);
        int blue2 = ColorUtil.getBlueFromColor(color2);
        int alpha2 = ColorUtil.getAlphaFromColor(color2);
        int finalRed = (red1 + red2) / 2;
        int finalGreen = (green1 + green2) / 2;
        int finalBlue = (blue1 + blue2) / 2;
        int finalAlpha = (alpha1 + alpha2) / 2;
        return ColorUtil.getColor(finalRed, finalGreen, finalBlue, finalAlpha);
    }

    public static int getOverallColorFrom(int color1, int color2, float percentTo2) {
        int red1 = ColorUtil.getRedFromColor(color1);
        int green1 = ColorUtil.getGreenFromColor(color1);
        int blue1 = ColorUtil.getBlueFromColor(color1);
        int alpha1 = ColorUtil.getAlphaFromColor(color1);
        int red2 = ColorUtil.getRedFromColor(color2);
        int green2 = ColorUtil.getGreenFromColor(color2);
        int blue2 = ColorUtil.getBlueFromColor(color2);
        int alpha2 = ColorUtil.getAlphaFromColor(color2);
        int finalRed = (int)((float)red1 * (1.0f - percentTo2) + (float)red2 * percentTo2);
        int finalGreen = (int)((float)green1 * (1.0f - percentTo2) + (float)green2 * percentTo2);
        int finalBlue = (int)((float)blue1 * (1.0f - percentTo2) + (float)blue2 * percentTo2);
        int finalAlpha = (int)((float)alpha1 * (1.0f - percentTo2) + (float)alpha2 * percentTo2);
        return ColorUtil.getColor(finalRed, finalGreen, finalBlue, finalAlpha);
    }

    public static int TwoColoreffect(int color1, int color2, double speed) {
        Color c1 = new Color(color1);
        Color c2 = new Color(color2);
        int cl1r = c1.getRed();
        int cl2r = c2.getRed();
        int cl1g = c1.getGreen();
        int cl2g = c2.getGreen();
        int cl1b = c1.getBlue();
        int cl2b = c2.getBlue();
        int cl1a = c1.getAlpha();
        int cl2a = c2.getAlpha();
        double thing = speed / 4.0 % 1.0;
        float val = (float) MathUtil.clamp((float)Math.sin(Math.PI * 6 * thing) / 2.0f + 0.5f, 0.0f, 1.0f);
        return ColorUtil.getColor((int)MathUtil.lerp(cl1r, cl2r, val), (int)MathUtil.lerp(cl1g, cl2g, val), (int)MathUtil.lerp(cl1b, cl2b, val), (int)MathUtil.lerp(cl1a, cl2a, val));
    }

    public static int astolfoColorsCool(int yOffset, int yTotal) {
        float f = 0;
        float hue;
        float speed = 1450.0f;
        for (hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + (float)((yTotal - yOffset) * 9); hue > speed; hue -= speed) {
        }
        hue /= speed;
        if ((double)f > 0.5) {
            hue = 0.5f - (hue - 0.5f);
        }
        return Color.HSBtoRGB(hue += 0.5f, 0.67f, 1.0f);
    }

    public static int swapAlpha(int color, float alpha) {
        int f = color >> 16 & 0xFF;
        int f1 = color >> 8 & 0xFF;
        int f2 = color & 0xFF;
        return getColor(f, f1, f2, (int)alpha);
    }

    public static int getColor(int brightness, int alpha) {
        return getColor(brightness, brightness, brightness, alpha);
    }

}