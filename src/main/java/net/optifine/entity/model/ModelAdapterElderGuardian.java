package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterElderGuardian extends ModelAdapterGuardian {
    public ModelAdapterElderGuardian() {
        super(EntityTypes.ELDER_GUARDIAN, "elder_guardian", ModelLayers.ELDER_GUARDIAN);
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(EntityRendererProvider.Context context) {
        return new ElderGuardianRenderer(context);
    }
}
