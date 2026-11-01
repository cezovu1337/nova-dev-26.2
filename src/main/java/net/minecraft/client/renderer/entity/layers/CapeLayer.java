package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.optifine.util.PlayerUtils;

public class CapeLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final HumanoidModel<AvatarRenderState> model;
    private final EquipmentAssetManager equipmentAssets;

    public CapeLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, EntityModelSet modelSet, EquipmentAssetManager equipmentAssets) {
        super(renderer);
        this.model = new PlayerCapeModel(modelSet.bakeLayer(ModelLayers.PLAYER_CAPE));
        this.equipmentAssets = equipmentAssets;
    }

    private boolean hasLayer(ItemStack itemStack, EquipmentClientInfo.LayerType layerType) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && !equippable.assetId().isEmpty()) {
            EquipmentClientInfo equipmentClientInfo = this.equipmentAssets.get(equippable.assetId().get());
            return !equipmentClientInfo.getLayers(layerType).isEmpty();
        } else {
            return false;
        }
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (!state.isInvisible && state.showCape) {
            PlayerSkin skin = state.skin;
            AbstractClientPlayer player = state.entity instanceof AbstractClientPlayer ? (AbstractClientPlayer)state.entity : null;
            Identifier capeTexture = player != null ? player.getLocationCape() : PlayerUtils.getTexturePath(skin.cape());
            if (capeTexture != null && !this.hasLayer(state.chestEquipment, EquipmentClientInfo.LayerType.WINGS)) {
                poseStack.pushPose();
                if (this.hasLayer(state.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
                    poseStack.translate(0.0F, -0.053125F, 0.06875F);
                }

                submitNodeCollector.submitModel(
                    this.model, state, poseStack, RenderTypes.entitySolid(capeTexture), lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null
                );
                poseStack.popPose();
            }
        }
    }
}
