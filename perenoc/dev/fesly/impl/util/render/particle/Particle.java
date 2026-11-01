package dev.fesly.impl.util.render.particle;

import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.player.BlockUtils;
import dev.fesly.impl.util.time.TimerUtil;
import lombok.Getter;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BushBlock;
import net.minecraft.util.math.vector.Vector3d;

@Getter
public class Particle implements IAccess {

    private final TimerUtil timerUtil = new TimerUtil();

    public final Vector3d position;
    private final Vector3d delta;

    public Particle(final Vector3d position) {
        this.position = position;
        this.delta = new Vector3d((Math.random() * 0.5 - 0.25) * 0.01, (Math.random() * 0.25) * 0.01, (Math.random() * 0.5 - 0.25) * 0.01);
        this.timerUtil.reset();
    }

    public Particle(final Vector3d position, final Vector3d velocity) {
        this.position = position;
        this.delta = new Vector3d(velocity.x * 0.01, velocity.y * 0.01, velocity.z * 0.01);
        this.timerUtil.reset();
    }

    public void update() {
        final Block block1 = BlockUtils.getBlock(this.position.x, this.position.y, this.position.z + this.delta.z);
        if (!(block1 instanceof AirBlock || block1 instanceof BushBlock))
            this.delta.z *= -0.8;

        final Block block2 = BlockUtils.getBlock(this.position.x, this.position.y + this.delta.y, this.position.z);
        if (!(block2 instanceof AirBlock || block2 instanceof BushBlock)) {
            this.delta.x *= 0.999F;
            this.delta.z *= 0.999F;

            this.delta.y *= -0.6;
        }

        final Block block3 = BlockUtils.getBlock(this.position.x + this.delta.x, this.position.y, this.position.z);
        if (!(block3 instanceof AirBlock || block3 instanceof BushBlock))
            this.delta.x *= -0.8;

        this.updateWithoutPhysics();
    }

    public void updateWithoutPhysics() {
        this.position.x += this.delta.x;
        this.position.y += this.delta.y;
        this.position.z += this.delta.z;
        this.delta.x /= 0.99999F;
        this.delta.y -= 0.000005F;
        this.delta.z /= 0.99999F;
    }
}