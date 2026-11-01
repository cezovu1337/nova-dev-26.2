package net.minecraft.util.math;

import net.minecraft.util.Mth;

public class MathHelper {
    public static float sin(float value) {
        return Mth.sin(value);
    }

    public static float cos(float value) {
        return Mth.cos(value);
    }

    public static float sqrt(float value) {
        return Mth.sqrt(value);
    }

    public static double sqrt(double value) {
        return Math.sqrt(value);
    }

    public static float fastInvSqrt(float number) {
        return (float) (1.0 / Math.sqrt(number));
    }

    public static double fastInvSqrt(double number) {
        return 1.0 / Math.sqrt(number);
    }

    public static int floor(float value) {
        return Mth.floor(value);
    }

    public static int floor(double value) {
        return Mth.floor(value);
    }

    public static long lfloor(double value) {
        return Mth.lfloor(value);
    }

    public static float abs(float value) {
        return Mth.abs(value);
    }

    public static int abs(int value) {
        return Mth.abs(value);
    }

    public static int ceil(float value) {
        return Mth.ceil(value);
    }

    public static int ceil(double value) {
        return Mth.ceil(value);
    }

    public static int clamp(int num, int min, int max) {
        return Mth.clamp(num, min, max);
    }

    public static float clamp(float num, float min, float max) {
        return Mth.clamp(num, min, max);
    }

    public static double clamp(double num, double min, double max) {
        return Mth.clamp(num, min, max);
    }

    public static double clampedLerp(double lowerBnd, double upperBnd, double slide) {
        return Mth.clampedLerp(lowerBnd, upperBnd, slide);
    }

    public static float clampedLerp(float lowerBnd, float upperBnd, float slide) {
        return Mth.clampedLerp(lowerBnd, upperBnd, slide);
    }

    public static double lerp(double delta, double start, double end) {
        return Mth.lerp(delta, start, end);
    }

    public static float lerp(float delta, float start, float end) {
        return Mth.lerp(delta, start, end);
    }

    public static float wrapDegrees(float value) {
        return Mth.wrapDegrees(value);
    }

    public static double wrapDegrees(double value) {
        return Mth.wrapDegrees(value);
    }

    public static int wrapDegrees(int angle) {
        return Mth.wrapDegrees(angle);
    }

    public static double atan2(double y, double x) {
        return Math.atan2(y, x);
    }
}
