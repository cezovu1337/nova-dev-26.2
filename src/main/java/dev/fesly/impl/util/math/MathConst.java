package dev.fesly.impl.util.math;
import net.minecraft.util.math.MathHelper;
public class MathConst {
    public static final float PI = (float) Math.PI;
    public static final float TO_RADIANS = PI / 180.0F;
    public static final float TO_DEGREES = 180.0F / PI;
    public static final float[] COSINE = new float[361];
    public static final float[] SINE = new float[361];
    public static void calculate() {
        for (int i = 0; i <= 360; ++i) {
            COSINE[i] = MathHelper.cos(i * TO_RADIANS);
            SINE[i] = MathHelper.sin(i * TO_RADIANS);
        }
    }
    public static int toIntDegree(final float angle) {
        return (int) (angle % 360 + 360) % 360;
    }
}
