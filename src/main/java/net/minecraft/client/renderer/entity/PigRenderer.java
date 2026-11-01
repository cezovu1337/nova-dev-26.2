package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.animal.pig.BabyPigModel;
import net.minecraft.client.model.animal.pig.ColdPigModel;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.optifine.entity.model.ModelPairUtils;
import net.optifine.entity.model.PigType;

public class PigRenderer extends MobRenderer<Pig, PigRenderState, PigModel> {
    private final Map<PigVariant.ModelType, AdultAndBabyModelPair<PigModel>> models;
    private AdultAndBabyModelPair<PigModel> warmModels;
    private boolean customModels;

    public PigRenderer(EntityRendererProvider.Context context) {
        super(context, new PigModel(context.bakeLayer(ModelLayers.PIG)), 0.7F);
        this.models = bakeModels(context);
        this.addLayer(
            new SimpleEquipmentLayer<>(
                this,
                context.getEquipmentRenderer(),
                EquipmentClientInfo.LayerType.PIG_SADDLE,
                state -> state.saddle,
                new PigModel(context.bakeLayer(ModelLayers.PIG_SADDLE)),
                null
            )
        );
        this.warmModels = this.models.get(PigVariant.ModelType.NORMAL);
    }

    private static Map<PigVariant.ModelType, AdultAndBabyModelPair<PigModel>> bakeModels(EntityRendererProvider.Context context) {
        return Maps.newEnumMap(
            Map.of(
                PigVariant.ModelType.NORMAL,
                new AdultAndBabyModelPair<>(new PigModel(context.bakeLayer(ModelLayers.PIG)), new BabyPigModel(context.bakeLayer(ModelLayers.PIG_BABY))),
                PigVariant.ModelType.COLD,
                new AdultAndBabyModelPair<>(
                    new ColdPigModel(context.bakeLayer(ModelLayers.COLD_PIG)), new BabyPigModel(context.bakeLayer(ModelLayers.PIG_BABY))
                )
            )
        );
    }

    public void submit(PigRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.variant != null) {
            this.model = this.models.get(state.variant.modelAndTexture().model()).getModel(state.isBaby);
            if (this.customModels && state.variant.modelAndTexture().asset().id().getPath().endsWith("warm_pig")) {
                this.model = this.warmModels.getModel(state.isBaby);
            }

            super.submit(state, poseStack, submitNodeCollector, camera);
        }
    }

    public Identifier getTextureLocation(PigRenderState state) {
        if (dev.fesly.Nova.getInstance().getModuleManager().get(dev.fesly.client.module.impl.misc.Pigga.class).isEnabled()) {
            return new Identifier("nova", "textures/pig.png");
        }
        return state.variant == null
            ? MissingTextureAtlasSprite.getLocation()
            : (state.isBaby ? state.variant.babyTexture().texturePath() : state.variant.modelAndTexture().asset().texturePath());
    }

    public PigRenderState createRenderState() {
        return new PigRenderState();
    }

    public void extractRenderState(Pig entity, PigRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.saddle = entity.getItemBySlot(EquipmentSlot.SADDLE).copy();
        state.variant = entity.getVariant().value();
    }

    public void setModel(PigType typeIn, boolean babyIn, PigModel modelIn) {
        if (typeIn == PigType.NORMAL) {
            this.setModel(PigVariant.ModelType.NORMAL, babyIn, modelIn);
        } else if (typeIn == PigType.COLD) {
            this.setModel(PigVariant.ModelType.COLD, babyIn, modelIn);
        } else if (typeIn == PigType.WARM) {
            this.warmModels = ModelPairUtils.updateModel(this.warmModels, babyIn, modelIn);
        }

        this.customModels = true;
    }

    private void setModel(PigVariant.ModelType typeIn, boolean babyIn, PigModel modelIn) {
        AdultAndBabyModelPair<PigModel> pair = this.models.get(typeIn);
        AdultAndBabyModelPair<PigModel> pair2 = ModelPairUtils.updateModel(pair, babyIn, modelIn);
        this.models.put(typeIn, pair2);
    }
}
