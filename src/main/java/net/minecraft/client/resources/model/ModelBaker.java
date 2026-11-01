package net.minecraft.client.resources.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.RenderTypeGroup;
import org.joml.Vector3fc;

public interface ModelBaker {
    ResolvedModel getModel(Identifier var1);

    BlockStateModelPart missingBlockModelPart();

    MaterialBaker materials();

    ModelBaker.Interner interner();

    <T> T compute(ModelBaker.SharedOperationKey<T> var1);

    default RenderTypeGroup renderType() {
        return null;
    }

    default RenderTypeGroup renderTypeFast() {
        return null;
    }

    interface Interner {
        Vector3fc vector(Vector3fc var1);

        BakedQuad.MaterialInfo materialInfo(BakedQuad.MaterialInfo var1);
    }

    @FunctionalInterface
    interface SharedOperationKey<T> {
        T compute(ModelBaker var1);
    }
}
