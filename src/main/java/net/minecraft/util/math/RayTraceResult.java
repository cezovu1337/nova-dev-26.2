package net.minecraft.util.math;

import net.minecraft.util.math.vector.Vector3d;

public class RayTraceResult {
    public Vector3d hitVec;

    public RayTraceResult(Vector3d hitVec) {
        this.hitVec = hitVec;
    }
}
