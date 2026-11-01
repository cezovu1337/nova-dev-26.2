package dev.fesly.impl.util.player;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.Nova;
import dev.fesly.client.module.impl.combat.BackTracks;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
public class BackEntityTracker {
    public Entity entity;
    public List<Point> positions;
    Minecraft mc;
    public BackEntityTracker(final Entity entityCoord) {
        this.positions = new ArrayList<>();
        this.mc = Minecraft.getInstance();
        this.entity = entityCoord;
    }
    public void tick() {
        if (this.entity instanceof LocalPlayer) {
            return;
        }
        if (Math.abs(this.entity.getPosX() - this.entity.prevPosX) > 0.0 || Math.abs(this.entity.getPosZ() - this.entity.prevPosZ) > 0.0
                || Math.abs(this.entity.getPosY() - this.entity.prevPosY) > 0.0) {
            final Point p = new Point(this.entity.getPositionVec().add(-0.5, 0.0, -0.5));
            p.yaw = (int)this.entity.rotationYaw;
            this.positions.add(p);
        }
        if (this.positions.size() > (Nova.getInstance().getModuleManager().get(BackTracks.class)).getSaveTicks()) {
            this.positions.remove(0);
        }
        this.positions.removeIf(point -> point.ticks > (Nova.getInstance().getModuleManager().get(BackTracks.class)).getSaveTicks());
    }
    public void click() {
        for (final Point point2 : this.positions) {
            final Point point = point2;
            ++point2.ticks;
            if (this.entity.getDistance(this.positions.get(0).vec3d.x, this.positions.get(0).vec3d.y, this.positions.get(0).vec3d.z) == 0.7071067690849304) {
                continue;
            }
            if (this.positions.size() <= 0) {
                continue;
            }
            final Vector3d src = new Vector3d(mc.player.getEyePosition(this.mc.getRenderPartialTicks()));
            final Vector3d vectorForRotation = mc.player.getLookVec();
            final Vector3d dest = src.add(vectorForRotation.x * 3.1, vectorForRotation.y * 3.1, vectorForRotation.z * 3.1);
            final RayTraceResult rayTraceResult = new AxisAlignedBB(point.vec3d.x + 0.8, point.vec3d.y, point.vec3d.z + 0.2, point.vec3d.x + 0.2, point.vec3d.y + 1.8, point.vec3d.z + 0.8).calculateIntercept(src, dest);
            if (rayTraceResult != null) {
                final PlayerController playerController = this.mc.playerController;
                final Minecraft mc2 = this.mc;
                playerController.attackEntity(mc.player, this.entity);
                break;
            }
        }
    }
    public void click(final float yaw, final float pitch, final float dist) {
        for (final Point point2 : this.positions) {
            final Point point = point2;
            ++point2.ticks;
            if (this.entity.getDistance(this.positions.get(0).vec3d.x, this.positions.get(0).vec3d.y, this.positions.get(0).vec3d.z) == 0.7071067690849304) {
                continue;
            }
            if (this.positions.size() <= 0) {
                continue;
            }
            final Vector3d src = new Vector3d(mc.player.getEyePosition(this.mc.getRenderPartialTicks()));
            final Vector3d vectorForRotation = Entity.getVectorForRotation(pitch, yaw);
            final Vector3d dest = src.add(vectorForRotation.x * dist, vectorForRotation.y * dist, vectorForRotation.z * dist);
            final RayTraceResult rayTraceResult = new AxisAlignedBB(point.vec3d.x + 0.8, point.vec3d.y, point.vec3d.z + 0.2, point.vec3d.x + 0.2, point.vec3d.y + 1.8, point.vec3d.z + 0.8).calculateIntercept(src, dest);
            if (rayTraceResult != null) {
                final PlayerController playerController = this.mc.playerController;
                final Minecraft mc2 = this.mc;
                playerController.attackEntity(mc.player, this.entity);
                break;
            }
        }
    }
    public boolean checkClick(final float yaw, final float pitch, final float dist) {
        for (final Point point : this.positions) {
            if (this.entity.getDistance(this.positions.get(0).vec3d.x, this.positions.get(0).vec3d.y, this.positions.get(0).vec3d.z) == 0.7071067690849304) {
                continue;
            }
            if (this.positions.size() <= 0) {
                continue;
            }
            final Vector3d src = new Vector3d(mc.player.getEyePosition(this.mc.getRenderPartialTicks()));
            final Vector3d vectorForRotation = Entity.getVectorForRotation(pitch, yaw);
            final Vector3d dest = src.add(vectorForRotation.x * dist, vectorForRotation.y * dist, vectorForRotation.z * dist);
            final RayTraceResult rayTraceResult = new AxisAlignedBB(point.vec3d.x + 0.8, point.vec3d.y, point.vec3d.z + 0.2, point.vec3d.x + 0.2, point.vec3d.y + 1.8, point.vec3d.z + 0.8).calculateIntercept(src, dest);
            if (rayTraceResult != null) {
                return true;
            }
        }
        return false;
    }
    public static class Point
    {
        public Vector3d vec3d;
        public int ticks;
        public int yaw;
        public Point(final Vector3d vec3d) {
            this.vec3d = vec3d;
        }
        public Vector3d getVec3d() {
            return this.vec3d;
        }
        public void update() {
            ++this.ticks;
        }
    }
}
