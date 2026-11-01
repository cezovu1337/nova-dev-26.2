package dev.fesly.impl.util.player;
import dev.fesly.impl.event.impl.input.MoveInputEvent;
import dev.fesly.impl.event.impl.motion.StrafeEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.math.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
public final class MoveUtil implements IAccess {
    public static boolean isBlockAboveHead() {
        AxisAlignedBB bb = new AxisAlignedBB(mc.player.getPosX() - 0.3, mc.player.getPosY() + (double) mc.player.getEyeHeight(), mc.player.getPosZ() + 0.3, mc.player.getPosX() + 0.3, mc.player.getPosY() + 2.5, mc.player.getPosZ() - 0.3);
        assert mc.world != null;
        return mc.world.getCollisionShapes(mc.player, bb).findAny().isPresent();
    }
    public static boolean isBlockAbove() {
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(mc.player.getPosX() - 0.3, mc.player.getPosY() + mc.player.getEyeHeight(), mc.player.getPosZ() + 0.3, mc.player.getPosX() + 0.3, mc.player.getPosY() + (!mc.player.isOnGround() ? 1.5 : 2.5), mc.player.getPosZ() - 0.3);
        return mc.world.getCollisionShapes(mc.player, axisAlignedBB).findAny().isPresent();
    }
    public static boolean hasBlocks(double y) {
        Minecraft mc = Minecraft.getInstance();
        return !mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().expand(0, y, 0)).toList().isEmpty();
    }
    public static float getDirection() {
        float rotationYaw = mc.player.rotationYaw;
        float factor = 0.0F;
        if (mc.player.movementInput.moveForward > 0) factor = 1;
        if (mc.player.movementInput.moveForward < 0) factor = -1;
        if (factor == 0) {
            if (mc.player.movementInput.moveStrafe > 0) rotationYaw -= 90;
            if (mc.player.movementInput.moveStrafe < 0) rotationYaw += 90;
        } else {
            if (mc.player.movementInput.moveStrafe > 0) rotationYaw -= 45 * factor;
            if (mc.player.movementInput.moveStrafe < 0) rotationYaw += 45 * factor;
        }
        if (factor < 0) rotationYaw -= 180;
        return (float) Math.toRadians(rotationYaw);
    }
    public static boolean isMoving() {
        if (mc.player == null) return false;
        if (mc.player.movementInput.moveForward != 0.0F) return true;
        return mc.player.movementInput.moveStrafe != 0.0F;
    }
    public static void strafe(float speed) {
        if (!MoveUtil.isMoving()) {
            return;
        }
        double yaw = MoveUtil.getDirection();
        mc.player.motion.x = -Math.sin(yaw) * (double) speed;
        mc.player.motion.z = Math.cos(yaw) * (double) speed;
    }
    public static void strafe() {
        if (mc.gameSettings.keyBindBack.isKeyDown()) {
            return;
        }
        MoveUtil.strafe(MoveUtil.getSpeed());
    }
    public static float getSpeed() {
        return (float) Math.sqrt(mc.player.motion.x * mc.player.motion.x + mc.player.motion.z * mc.player.motion.z);
    }
    public static void setSpeed(double speed) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            mc.player.motion.x = 0.0;
            mc.player.motion.y = 0.0;
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float) (forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float) (forward > 0.0 ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            mc.player.motion.x = forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F));
            mc.player.motion.z = forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F));
        }
    }
    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0.0F) rotationYaw += 180.0F;
        float forward = 1.0F;
        if (moveForward < 0.0F) forward = -0.5F;
         else if (moveForward > 0.0F) forward = 0.5F;
        if (moveStrafing > 0.0F) rotationYaw -= 90.0F * forward;
        if (moveStrafing < 0.0F) rotationYaw += 90.0F * forward;
        return Math.toRadians(rotationYaw);
    }
    public static void stop() {
        Minecraft.getInstance().player.setMotion(0, Minecraft.getInstance().player.getMotion().y, 0);
    }
    public static void fixMovement(final MoveInputEvent event, final float yaw) {
        final float forward = event.getForward();
        final float strafe = event.getStrafe();
        final double angle = MathHelper.wrapDegrees(Math.toDegrees(MoveUtil.direction(mc.player.rotationYaw, forward, strafe)));
        if (forward == 0 && strafe == 0) {
            return;
        }
        float closestForward = 0;
        float closestStrafe = 0;
        float closestDifference = Float.MAX_VALUE;
        for (float predictedForward = -1.0F; predictedForward <= 1.0F; predictedForward += 1.0F) {
            for (float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; predictedStrafe += 1.0F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;
                final double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(MoveUtil.direction(yaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);
                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }
        event.setForward(closestForward);
        event.setStrafe(closestStrafe);
    }
    public static void fixMovement(float yaw) {
        LocalPlayer player = mc.player;
        float yawRad = (float) Math.toRadians(yaw);
        double currentMotionX = player.motion.x;
        double currentMotionZ = player.motion.z;
        double modifiedMotionX = currentMotionX * Math.cos(yawRad) - currentMotionZ * Math.sin(yawRad);
        double modifiedMotionZ = currentMotionX * Math.sin(yawRad) + currentMotionZ * Math.cos(yawRad);
        player.motion.x = modifiedMotionX;
        player.motion.z = modifiedMotionZ;
    }
    public static void silentMove(StrafeEvent event, float yaw) {
        float dist;
        Vector3d relative = event.getRelative();
        double d0 = event.getRelative().lengthSquared();
        Vector3d vector3d = (d0 > 1.0 ? relative.normalize() : relative).scale(event.getFriction());
        float strafe = (float) vector3d.x;
        float forward = (float) vector3d.z;
        float friction = event.getFriction();
        int difference = (int) ((MathHelper.wrapDegrees(mc.player.rotationYaw - yaw - 23.5F - 135.0F) + 180.0F) / 45.0F);
        float calcForward = 0.0F;
        float calcStrafe = 0.0F;
        switch (difference) {
        case 0:
            {
                calcForward = forward;
                calcStrafe = strafe;
                break;
            }
        case 1:
            {
                calcForward += forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe += strafe;
                break;
            }
        case 2:
            {
                calcForward = strafe;
                calcStrafe = -forward;
                break;
            }
        case 3:
            {
                calcForward -= forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe -= strafe;
                break;
            }
        case 4:
            {
                calcForward = -forward;
                calcStrafe = -strafe;
                break;
            }
        case 5:
            {
                calcForward -= forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe -= strafe;
                break;
            }
        case 6:
            {
                calcForward = -strafe;
                calcStrafe = forward;
                break;
            }
        case 7:
            {
                calcForward += forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe += strafe;
            }
        }
        if (calcForward > 1.0F || calcForward < 0.9F && calcForward > 0.3F || calcForward < -1.0F || calcForward > -0.9F && calcForward < -0.3F) {
            calcForward *= 0.5F;
        }
        if (calcStrafe > 1.0F || calcStrafe < 0.9F && calcStrafe > 0.3F || calcStrafe < -1.0F || calcStrafe > -0.9F && calcStrafe < -0.3F) {
            calcStrafe *= 0.5F;
        }
        if ((dist = calcStrafe * calcStrafe + calcForward * calcForward) >= 1.0E-4F) {
            dist = (float) ((double) friction / Math.max(1.0, Math.sqrt(dist)));
            float yawSin = MathHelper.sin((float) ((double) yaw * Math.PI / 180.0));
            float yawCos = MathHelper.cos((float) ((double) yaw * Math.PI / 180.0));
            mc.player.motion.x += (calcStrafe *= dist) * yawCos - (calcForward *= dist) * yawSin;
            mc.player.motion.z += calcForward * yawCos + calcStrafe * yawSin;
        }
    }
    public static void applyStrafeToPlayer(MoveInputEvent event) {
        final var player = Minecraft.getInstance().player;
        float dif = ((MathUtil.wrapDegrees(player.rotationYaw - player.rotationYaw - 23.5F - 135) + 180) / 45);
        final var yaw = player.rotationYaw;
        final var strafe = event.getStrafe();
        final var forward = event.getForward();
        var calcForward = 0.0F;
        var calcStrafe = 0.0F;
        if (dif == 0) {
            calcForward = forward;
            calcStrafe = strafe;
        } else if (dif == 1) {
            calcForward += forward;
            calcStrafe -= forward;
            calcForward += strafe;
            calcStrafe += strafe;
        } else if (dif == 2) {
            calcForward = strafe;
            calcStrafe = -forward;
        } else if (dif == 3) {
            calcForward -= forward;
            calcStrafe -= forward;
            calcForward += strafe;
            calcStrafe -= strafe;
        } else if (dif == 4) {
            calcForward = -forward;
            calcStrafe = -strafe;
        } else if (dif == 5) {
            calcForward -= forward;
            calcStrafe += forward;
            calcForward -= strafe;
            calcStrafe -= strafe;
        } else if (dif == 6) {
            calcForward = -strafe;
            calcStrafe = forward;
        } else if (dif == 7) {
            calcForward += forward;
            calcStrafe += forward;
            calcForward -= strafe;
            calcStrafe += strafe;
        }
        if (calcForward > 1.0F || calcForward < 0.9F && calcForward > 0.3F || calcForward < -1.0F || calcForward > -0.9F && calcForward < -0.3F) {
            calcForward *= 0.5F;
        }
        if (calcStrafe > 1.0F || calcStrafe < 0.9F && calcStrafe > 0.3F || calcStrafe < -1.0F || calcStrafe > -0.9F && calcStrafe < -0.3F) {
            calcStrafe *= 0.5F;
        }
        var d = calcStrafe * calcStrafe + calcForward * calcForward;
        {
            d = MathHelper.sqrt(d);
            if (d < 1.0F) d = 1.0F;
            float rq = 5.5754306E12F;
            d = (float) Math.hypot(d, rq);
            calcStrafe *= d;
            calcForward *= d;
            final var yawSin = MathHelper.sin((float) (yaw * Math.PI / 180.0F));
            final var yawCos = MathHelper.cos((float) (yaw * Math.PI / 180.0F));
            player.motion.x += (calcStrafe * yawCos - calcForward * yawSin);
            player.motion.y += (calcForward * yawCos + calcStrafe * yawSin) ;
        }
    }
    @java.lang.SuppressWarnings("all")
    private MoveUtil() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
