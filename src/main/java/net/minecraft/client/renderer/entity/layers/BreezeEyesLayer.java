package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.breeze.BreezeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class BreezeEyesLayer extends RenderLayer<BreezeRenderState, BreezeModel> {
    private RenderType BREEZE_EYES = RenderTypes.breezeEyes(Identifier.withDefaultNamespace("textures/entity/breeze/breeze_eyes.png"));
    private BreezeModel model;

    public BreezeEyesLayer(RenderLayerParent<BreezeRenderState, BreezeModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new BreezeModel(modelSet.bakeLayer(ModelLayers.BREEZE_EYES));
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, BreezeRenderState state, float yRot, float xRot) {
        submitNodeCollector.order(1)
            .submitModel(this.model, state, poseStack, this.BREEZE_EYES, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }

    public void setModel(BreezeModel model) {
        this.model = model;
    }

    public void setTextureLocation(Identifier textureLocation) {
        this.BREEZE_EYES = RenderTypes.breezeEyes(textureLocation);
    }
}
