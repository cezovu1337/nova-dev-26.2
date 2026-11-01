package net.minecraft.util.math;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class BlockRayTraceResult extends RayTraceResult {
    private Direction face;
    private BlockPos pos;
    private boolean isInside;

    public BlockRayTraceResult(Vector3d hitVec, Direction face, BlockPos pos, boolean isInside) {
        super(hitVec);
        this.face = face;
        this.pos = pos;
        this.isInside = isInside;
    }

    public Direction getFace() {
        return face;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean isInside() {
        return isInside;
    }
}
