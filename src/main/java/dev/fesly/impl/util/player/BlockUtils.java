package dev.fesly.impl.util.player;
import dev.fesly.impl.interfaces.game.IMinecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.MathHelper;
public class BlockUtils implements IMinecraft {
    public static Block getBlock(BlockPos pos) {
        return getState(pos).getBlock();
    }
    public static Block getBlock(double x, double y, double z) {
        return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }
    public static Block getBlockAbovePlayer(Player inPlayer, double blocks) {
        blocks += inPlayer.getHeight();
        return getBlockAtPos(new BlockPos(inPlayer.getPosX(), inPlayer.getPosY() + blocks, inPlayer.getPosZ()));
    }
    public static Block getBlockAtPos(BlockPos inBlockPos) {
        BlockState s = mc.world.getBlockState(inBlockPos);
        return s.getBlock();
    }
    public static Block getBlockAtPosC(Player inPlayer, double x, double y, double z) {
        return getBlockAtPos(new BlockPos(inPlayer.getPosX() - x, inPlayer.getPosY() - y, inPlayer.getPosZ() - z));
    }
    public static float getBlockDistance(float xDiff, float yDiff, float zDiff) {
        return MathHelper.sqrt(((xDiff - 0.5F) * (xDiff - 0.5F)) + ((yDiff - 0.5F) * (yDiff - 0.5F))
                + ((zDiff - 0.5F) * (zDiff - 0.5F)));
    }
    public static float[] getRotations(final double posX, final double posY, final double posZ) {
        final LocalPlayer player = mc.player;
        final double x = posX - player.getPosX();
        final double y = posY - (player.getPosY() + (double) player.getEyeHeight());
        final double z = posZ - player.getPosZ();
        final double dist = MathHelper.sqrt(x * x + z * z);
        final float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        final float pitch = (float) (-(Math.atan2(y, dist) * 180.0D / Math.PI));
        return new float[]{yaw, pitch};
    }
    public static BlockPos getBlockPos(BlockPos inBlockPos) {
        return inBlockPos;
    }
    public static BlockPos getBlockPos(double x, double y, double z) {
        return getBlockPos(new BlockPos(x, y, z));
    }
    public static float getHorizontalPlayerBlockDistance(BlockPos blockPos) {
        float xDiff = (float) (mc.player.getPosX() - blockPos.getX());
        float zDiff = (float) (mc.player.getPosZ() - blockPos.getZ());
        return MathHelper.sqrt(((xDiff - 0.5F) * (xDiff - 0.5F)) + ((zDiff - 0.5F) * (zDiff - 0.5F)));
    }
    public static Minecraft getMinecraft() {
        return mc;
    }
    public static float getPlayerBlockDistance(BlockPos blockPos) {
        return getPlayerBlockDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
    public static float getPlayerBlockDistance(double posX, double posY, double posZ) {
        float xDiff = (float) (mc.player.getPosX() - posX);
        float yDiff = (float) (mc.player.getPosY() - posY);
        float zDiff = (float) (mc.player.getPosZ() - posZ);
        return getBlockDistance(xDiff, yDiff, zDiff);
    }
    public static BlockState getState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }
}
