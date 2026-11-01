package dev.fesly.impl.util.rotation;

import dev.fesly.impl.interfaces.game.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.security.SecureRandom;
import java.util.Random;


@UtilityClass
public class RotationUtil implements IMinecraft {

    public static float yaw;
    public static float pitch;
    public static boolean rotationInUse;

    public float[] getDefault(ClientPlayerEntity player, LivingEntity target, boolean isRandom) {
        float yawRandom = (float) getRandom(new SecureRandom(), -0.05, 0.05);
        float pitchRandom = (float) getRandom(new SecureRandom(), -0.05, 0.05);

        if (target != null) {
            double posX = isRandom ? target.getPosX() - player.getPosX() + (double) yawRandom : target.getPosX() - player.getPosX();
            double posY = isRandom ? target.getPosY() + (double) target.getEyeHeight() - (player.getPosY() + (double) player.getEyeHeight() - (double) pitchRandom) : target.getPosY() + (double) target.getEyeHeight() - (player.getPosY() + (double) player.getEyeHeight());
            double posZ = isRandom ? target.getPosZ() - player.getPosZ() - (double) yawRandom : target.getPosZ() - player.getPosZ();
            double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
            float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
            float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
            double sens = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            double pow = sens * sens * sens * 1.2F;
            yaw -= yaw % pow;
            pitch -= pitch % (pow * sens);
            return new float[]{(yaw), (pitch)};
        }
        return new float[]{(yaw), (pitch)};
    }

    public static float[] getVectorDefault(ClientPlayerEntity player, Vector3d target, double height, boolean isRandom) {
        float yawRandom = (float) getRandom(new SecureRandom(), -0.01, 0.01);
        float pitchRandom = (float) getRandom(new SecureRandom(), -0.01, 0.01);

        if (target != null) {
            double posX = isRandom ? target.getX() - player.getPosX() + (double) yawRandom : target.getX() - player.getPosX();
            double posY = isRandom ? target.getY() + height - (player.getPosY() + (double) player.getEyeHeight() - (double) pitchRandom) : target.getY() + height - (player.getPosY() + (double) player.getEyeHeight());
            double posZ = isRandom ? target.getZ() - player.getPosZ() - (double) yawRandom : target.getZ() - player.getPosZ();
            double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
            float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
            float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
            double sens = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            double pow = sens * sens * sens * 1.2F;
            yaw -= yaw % pow;
            pitch -= pitch % (pow * sens);
            return new float[]{yaw, pitch};
        }
        return new float[]{yaw, pitch};
    }

    public static float[] getMultiPoint(ClientPlayerEntity player, LivingEntity target, boolean isRandom) {
        float yawRandom = (float) getRandom(new SecureRandom(), -0.35, 0.32);
        float pitchRandom = (float) getRandom(new SecureRandom(), -0.35, 0.332);


        if (target != null) {
            Vector3d vec = MultiPoints.getBestPoint(target);
            double posX = isRandom ? vec.x - player.getPosX() : vec.x - player.getPosX() + yawRandom;
            double posY = isRandom ? vec.y - (player.getPosY() + (double) player.getEyeHeight() / 0.9f) : vec.y - (player.getPosY() + (double) player.getEyeHeight() / 0.9f) + pitchRandom;
            double posZ = isRandom ? vec.z - player.getPosZ() : vec.z - player.getPosZ() + yawRandom;
            double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
            float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f + yawRandom;
            float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
            double sens = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            double pow = sens * sens * sens * 1.2F;
            yaw -= yaw % pow;
            pitch -= pitch % (pow * sens);
            return new float[]{yaw, pitch};
        }
        return new float[]{yaw, pitch};
    }

    public static float[] getSmooth(ClientPlayerEntity player, LivingEntity target, float yaw, float pitch, float smoothness, boolean multipoint) {
        float[] currentRotation = multipoint ? getMultiPoint(player, target, true) : getDefault(player, target, false);
        float deltaYaw = MathHelper.wrapDegrees(currentRotation[0] - yaw);
        float deltaPitch = MathHelper.wrapDegrees(currentRotation[1] - pitch);
        float yawStep = Math.signum(deltaYaw) * Math.min(Math.abs(deltaYaw), smoothness);
        float pitchStep = Math.signum(deltaPitch) * Math.min(Math.abs(deltaPitch), smoothness);
        return new float[]{yaw + yawStep, pitch + pitchStep};
    }

    public static double getDistance(Vector3d first, Vector3d second) {
        double x = first.x - second.x;
        double y = first.y - second.y;
        double z = first.z - second.z;
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static void setRotation(float y, float p) {
        pitch = Math.max(-90.0f, Math.min(90.0f, p));
        yaw = normalizeAngle(y);
        rotationInUse = true;
    }

    public static void setYaw(float y, float speed) {
        setRotation(yaw, pitch);
        float taryaw = normalizeAngle(y);
        float angleDifference = getAngleDifference(yaw, taryaw);

        if (angleDifference <= speed || speed >= 360.0f) {
            yaw = taryaw;
        } else {
            yaw = normalizeAngle(yaw + (getRotationDirection(yaw, taryaw) * speed));
        }
    }

    public static void setPitch(float p, float speed) {
        float targetPitch = Math.max(-90.0f, Math.min(90.0f, p));
        float angleDifference = Math.abs(pitch - targetPitch);

        if (angleDifference <= speed || speed >= 360.0f) {
            pitch = targetPitch;
        } else {
            pitch += (targetPitch < pitch ? -speed : speed);
        }
    }

    private static float normalizeAngle(float angle) {
        return (angle % 360 + 360) % 360;
    }

    private static float getAngleDifference(float angle1, float angle2) {
        float difference = Math.abs(angle1 - angle2) % 360;
        return difference > 180 ? 360 - difference : difference;
    }

    private static int getRotationDirection(float angle1, float angle2) {
        float difference = normalizeAngle(angle2 - angle1);
        return difference > 180 ? -1 : 1;
    }

    public static boolean isInRange(float angle1, float angle2, float max) {
        return getAngleDifference(angle1, angle2) <= max;
    }

    private static double getRandom(Random random, double min, double max) {
        return min >= max ? min : random.nextDouble() * (max - min) + min;
    }

    public static float calculateCorrectYawOffset(float yaw) {
        // Инициализация переменных
        double xDiff = mc.player.getPosX() - mc.player.prevPosX;
        double zDiff = mc.player.getPosZ() - mc.player.prevPosZ;
        float distSquared = (float) (xDiff * xDiff + zDiff * zDiff);
        float renderYawOffset = mc.player.renderYawOffset;
        float offset = renderYawOffset;
        float yawOffsetDiff;

        // Вычисление смещения, если расстояние больше порогового значения
        if (distSquared > 0.0025000002f) {
            offset = (float) MathHelper.atan2(zDiff, xDiff) * 180.0f / (float) Math.PI - 90.0f;
        }

        // Установка смещения равным углу поворота, если игрок машет рукой
        if (mc.player != null && mc.player.swingProgress > 0.0f) {
            offset = yaw;
        }

        // Ограничение разницы смещений
        yawOffsetDiff = MathHelper.wrapDegrees(yaw - (renderYawOffset + MathHelper.wrapDegrees(offset - renderYawOffset) * 0.3f));
        yawOffsetDiff = MathHelper.clamp(yawOffsetDiff, -75.0f, 75.0f);

        // Вычисление итогового смещения
        renderYawOffset = yaw - yawOffsetDiff;
        if (yawOffsetDiff * yawOffsetDiff > 2500.0f) {
            renderYawOffset += yawOffsetDiff * 0.2f;
        }

        return renderYawOffset;
    }
}