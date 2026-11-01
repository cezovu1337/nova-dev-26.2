package net.minecraft.client.model.geom;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class EntityModelSet {
    public static final EntityModelSet EMPTY = new EntityModelSet(Map.of());
    private final Map<ModelLayerLocation, LayerDefinition> roots;

    public EntityModelSet(Map<ModelLayerLocation, LayerDefinition> roots) {
        this.roots = roots;
    }

    public ModelPart bakeLayer(ModelLayerLocation id) {
        LayerDefinition result = this.roots.get(id);
        if (result == null) {
            throw new IllegalArgumentException("No model for layer " + id);
        }

        ModelPart part = result.bakeRoot();
        part.setModelLayerLocation(id);
        return part;
    }

    public static EntityModelSet vanilla() {
        return new EntityModelSet(ImmutableMap.copyOf(LayerDefinitions.createRoots()));
    }
}
