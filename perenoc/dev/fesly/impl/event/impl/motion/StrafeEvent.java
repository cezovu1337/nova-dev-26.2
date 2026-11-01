package dev.fesly.impl.event.impl.motion;

import dev.fesly.impl.event.CancellableEvent;
import dev.fesly.impl.util.player.MoveUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

@Data
@AllArgsConstructor
public final class StrafeEvent extends CancellableEvent {

    private float friction;
    private Vector3d relative;
    private float yaw;

    public void setMotion(double speed, float strafeMotion) {
        float remainder = 1 - strafeMotion;
        ClientPlayerEntity player = Minecraft.getInstance().player;
        strafeMotion *= 0.90;
        if (player.isOnGround()) {
            MoveUtil.setSpeed(speed);
        } else {
            player.getMotion().x = player.getMotion().getX() * strafeMotion;
            player.getMotion().z = player.getMotion().getZ() * strafeMotion;
            friction = (float) speed * remainder;
        }
    }
}