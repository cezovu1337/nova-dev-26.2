package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Mob;

@Deprecated
public abstract class AgeableMobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends MobRenderer<T, S, M> {
    private M adultModel;
    private M babyModel;

    public AgeableMobRenderer(EntityRendererProvider.Context context, M adultModel, M babyModel, float shadow) {
        super(context, adultModel, shadow);
        this.adultModel = adultModel;
        this.babyModel = babyModel;
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        this.model = state.isBaby ? this.babyModel : this.adultModel;
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    public M getAdultModel() {
        return this.adultModel;
    }

    public M getBabyModel() {
        return this.babyModel;
    }

    public void setAdultModel(M adultModel) {
        this.adultModel = adultModel;
        super.model = adultModel;
    }

    public void setBabyModel(M babyModel) {
        this.babyModel = babyModel;
    }
}
