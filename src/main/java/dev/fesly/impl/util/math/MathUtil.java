package dev.fesly.impl.util.math;
import dev.fesly.impl.util.shader.ShaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
public final class MathUtil {
    public static double getRandom(double min, double max) {
        if (min == max) {
            return min;
        } else if (min > max) {
            final double d = min;
            min = max;
            max = d;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
    public static float wrapDegrees(float value) {
        value = value % 360.0F;
        if (value >= 180.0F) {
            value -= 360.0F;
        }
        if (value < -180.0F) {
            value += 360.0F;
        }
        return value;
    }
    public static float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }
    public static double round(double target, int decimal) {
        double p = Math.pow(10, decimal);
        return Math.round(target * p) / p;
    }
    public static String format(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = ((millis % 360000) % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    public static float normalize(float value, float min, float max) {
        return (value - min) / (max - min);
    }
    public static float denormalize(float value, float min, float max) {
        return min + (max - min) * value;
    }
    public static double round(final double value, final int scale, final double inc) {
        final double halfOfInc = inc / 2.0;
        final double floored = Math.floor(value / inc) * inc;
        if (value >= floored + halfOfInc) {
            return new BigDecimal(Math.ceil(value / inc) * inc).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        } else {
            return new BigDecimal(floored).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        }
    }
    public static double roundWithSteps(final double value, final double steps) {
        double a = ((Math.round(value / steps)) * steps);
        a *= 1000;
        a = (int) a;
        a /= 1000;
        return a;
    }
    public static boolean isHovered(double x, double y, double width, double height, double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
    public static double lerp(final double a, final double b, final double c) {
        return a + c * (b - a);
    }
    public static float lerp(final float oldValue, final float newValue, final float lerpValue) {
        return oldValue + lerpValue * (newValue - oldValue);
    }
    public static int lerp(final int oldValue, final float newValue, final float lerpValue) {
        return (int) (oldValue + lerpValue * (newValue - oldValue));
    }
    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }
    public static float animation(float animation, float target, float speed) {
        float dif = (target - animation) / Math.max((float) Minecraft.getInstance().getFrameTimer().getFrames().length, 5.0F) * 15.0F;
        if (dif > 0.0F) {
            dif = Math.max(speed, dif);
            dif = Math.min(target - animation, dif);
        } else if (dif < 0.0F) {
            dif = Math.min(-speed, dif);
            dif = Math.max(target - animation, dif);
        }
        return animation + dif;
    }
    public static double getDistance(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        final double d0 = x2 - x1;
        final double d1 = y2 - y1;
        final double d2 = z2 - z1;
        return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }
    public static double clamp(double min, double max, double n) {
        return Math.max(min, Math.min(max, n));
    }
    public static float randomizeFloat(float min, float max) {
        return (float) (min + Math.random() * (max - min));
    }
    public static float Rotate(float from, float to, float minstep, float maxstep) {
        float f = (float) (wrapDegrees(to - from) * clamp(0.6F, 0, 1));
        if (f < 0) {
            f = (float) clamp(f, -maxstep, -minstep);
        } else {
            f = (float) clamp(f, minstep, maxstep);
        }
        if (Math.abs(f) > Math.abs(wrapDegrees(to - from))) return to;
        return from + f;
    }
    @java.lang.SuppressWarnings("all")
    private MathUtil() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
