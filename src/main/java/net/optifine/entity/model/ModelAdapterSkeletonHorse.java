package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterSkeletonHorse extends ModelAdapterHorse {
    public ModelAdapterSkeletonHorse() {
        this(EntityTypes.SKELETON_HORSE, "skeleton_horse", ModelLayers.SKELETON_HORSE);
    }

    protected ModelAdapterSkeletonHorse(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterSkeletonHorse ma = new ModelAdapterSkeletonHorse(this.getEntityType(), "skeleton_horse_baby", ModelLayers.SKELETON_HORSE_BABY);
        ma.setBaby(true);
        return ma;
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(EntityRendererProvider.Context context) {
        return new UndeadHorseRenderer(
            context,
            EquipmentClientInfo.LayerType.SKELETON_HORSE_SADDLE,
            ModelLayers.SKELETON_HORSE_SADDLE,
            UndeadHorseRenderer.Type.SKELETON,
            UndeadHorseRenderer.Type.SKELETON_BABY
        );
    }
}
