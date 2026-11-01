package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

public class CreeperPowerLayer extends EnergySwirlLayer<CreeperRenderState, CreeperModel> {
    private static final Identifier POWER_LOCATION = Identifier.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
    public CreeperModel model;
    public Identifier customTextureLocation;

    public CreeperPowerLayer(RenderLayerParent<CreeperRenderState, CreeperModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new CreeperModel(modelSet.bakeLayer(ModelLayers.CREEPER_ARMOR));
    }

    protected boolean isPowered(CreeperRenderState state) {
        return state.isPowered;
    }

    @Override
    protected float xOffset(float t) {
        return t * 0.01F;
    }

    @Override
    protected Identifier getTextureLocation() {
        return this.customTextureLocation != null ? this.customTextureLocation : POWER_LOCATION;
    }

    protected CreeperModel model() {
        return this.model;
    }
}
