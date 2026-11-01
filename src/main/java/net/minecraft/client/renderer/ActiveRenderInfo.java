package net.minecraft.client.renderer;

import net.minecraft.client.Camera;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.phys.Vec3;

public class ActiveRenderInfo extends Camera {
    public float getPitch() {
        return this.xRot();
    }

    public float getYaw() {
        return this.yRot();
    }

    public Vector3d getProjectedView() {
        Vec3 pos = this.position();
        if (pos == null) {
            return new Vector3d(0, 0, 0);
        }
        return new Vector3d(pos.x, pos.y, pos.z);
    }

    public net.minecraft.core.BlockPos getBlockPos() {
        return this.blockPosition();
    }
}
