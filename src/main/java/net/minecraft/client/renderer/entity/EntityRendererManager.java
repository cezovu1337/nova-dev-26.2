package net.minecraft.client.renderer.entity;

import net.minecraft.world.entity.Entity;

public interface EntityRendererManager {
    default void renderEntityStatic(Entity entity, double x, double y, double z, float yaw, float partialTicks, com.mojang.blaze3d.matrix.MatrixStack matrixStack, Object bufferSource, int packedLight) {
    }

    default int getPackedLight(Entity entity, float partialTicks) {
        return 15728880;
    }
}
