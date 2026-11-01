package dev.fesly.impl.util.rotation;

import dev.fesly.impl.interfaces.game.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

public class MultiPoints implements IMinecraft {

    public static Vector3d getBestPoint(Entity e) {
        float x = (float) limit(e.getBoundingBox().minX, e.getBoundingBox().maxX - e.getBoundingBox().minX, mc.player.getPosX());
        float y = (float) limit(e.getBoundingBox().minY, e.getBoundingBox().maxY - e.getBoundingBox().minY, mc.player.getPosY() + mc.player.getEyeHeight());
        float z = (float) limit(e.getBoundingBox().minZ, e.getBoundingBox().maxZ - e.getBoundingBox().minZ, mc.player.getPosZ());

        return new Vector3d(
                e.getBoundingBox().minX + (e.getBoundingBox().maxX - e.getBoundingBox().minX) * x,
                e.getBoundingBox().minY + (e.getBoundingBox().maxY - e.getBoundingBox().minY) * y,
                e.getBoundingBox().minZ + (e.getBoundingBox().maxZ - e.getBoundingBox().minZ) * z
        );
    }

    private static double limit(double min, double max, double cur) {
        return Math.min(1, Math.max(0, (cur - min) / max));
    }
}
