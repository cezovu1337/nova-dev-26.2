package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.optifine.Config;
import net.optifine.EmissiveTextures;

public abstract class Model<S> {
    protected final ModelPart root;
    protected final Function<Identifier, RenderType> renderType;
    private final List<ModelPart> allParts;
    public int textureWidth = 64;
    public int textureHeight = 32;
    public Identifier locationTextureCustom;

    public Model(ModelPart root, Function<Identifier, RenderType> renderType) {
        this.root = root;
        this.renderType = renderType;
        this.allParts = root.getAllParts();
    }

    public final Function<Identifier, RenderType> renderType() {
        return this.renderType;
    }

    public final RenderType renderType(Identifier texture) {
        if (this.locationTextureCustom != null) {
            texture = this.locationTextureCustom;
        }

        RenderType type = this.renderType().apply(texture);
        if (EmissiveTextures.isRenderEmissive() && type.isEntitySolid()) {
            type = RenderTypes.entityCutout(texture, type.affectsOutline());
        }

        if (Config.isShaders() && this instanceof PlayerModel && type.hasBlending()) {
            type = RenderTypes.entityCutout(texture, type.affectsOutline());
        }

        return type;
    }

    public final void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int lightCoords, int overlayCoords, int color) {
        this.root().render(poseStack, buffer, lightCoords, overlayCoords, color);
    }

    public final ModelPart root() {
        return this.root;
    }

    public final List<ModelPart> allParts() {
        return this.allParts;
    }

    public void setupAnim(S state) {
        this.resetPose();
    }

    public final void resetPose() {
        for (ModelPart part : this.allParts) {
            part.resetPose();
        }
    }

    public boolean isRenderRoot() {
        return true;
    }

    public static class Simple extends Model<Unit> {
        public Simple(ModelPart root, Function<Identifier, RenderType> renderType) {
            super(root, renderType);
        }

        public void setupAnim(Unit state) {
        }
    }
}
