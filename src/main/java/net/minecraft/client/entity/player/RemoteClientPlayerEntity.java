package net.minecraft.client.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;

public class RemoteClientPlayerEntity extends RemotePlayer {
    public float rotationYawHead;
    public Object inventory;

    public RemoteClientPlayerEntity(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
        this.setPos(x, y, z);
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    public void setInvisible(boolean invisible) {
        super.setInvisible(invisible);
    }

    public void setSneaking(boolean sneaking) {
        this.setShiftKeyDown(sneaking);
    }

    public int getEntityId() {
        return this.getId();
    }
}
