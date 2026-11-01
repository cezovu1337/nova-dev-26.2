package net.minecraft.util.math;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.phys.AABB;

public class AxisAlignedBB extends AABB {
    public AxisAlignedBB(double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
    }

    public RayTraceResult calculateIntercept(Vector3d src, Vector3d dest) {
        return null;
    }

    public AxisAlignedBB grow(double x, double y, double z) {
        return new AxisAlignedBB(this.minX - x, this.minY - y, this.minZ - z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public AxisAlignedBB expand(double x, double y, double z) {
        return new AxisAlignedBB(this.minX - x, this.minY - y, this.minZ - z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }
}
