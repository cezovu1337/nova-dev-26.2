package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.IdentityHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.optifine.entity.model.IEntityRenderer;
import net.optifine.util.Either;

public interface BlockEntityRenderer<T extends BlockEntity, S extends BlockEntityRenderState> extends IEntityRenderer {
    IdentityHashMap<BlockEntityRenderer, BlockEntityType> CACHED_TYPES = new IdentityHashMap<>();

    S createRenderState();

    default void extractRenderState(T blockEntity, S state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
    }

    void submit(S var1, PoseStack var2, SubmitNodeCollector var3, CameraRenderState var4);

    default boolean shouldRenderOffScreen() {
        return false;
    }

    default int getViewDistance() {
        return 64;
    }

    default boolean shouldRender(T blockEntity, Vec3 cameraPosition) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPosition, this.getViewDistance());
    }

    @Override
    default Either<EntityType, BlockEntityType> getType() {
        BlockEntityType type = CACHED_TYPES.get(this);
        return type == null ? null : Either.makeRight(type);
    }

    @Override
    default void setType(Either<EntityType, BlockEntityType> type) {
        CACHED_TYPES.put(this, type.getRight().get());
    }

    @Override
    default void setShadowSize(float shadowSize) {
    }

    @Override
    default Identifier getLocationTextureCustom() {
        return null;
    }

    @Override
    default void setLocationTextureCustom(Identifier locationTextureCustom) {
    }
}
