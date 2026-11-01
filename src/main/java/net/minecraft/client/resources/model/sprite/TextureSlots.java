package net.minecraft.client.resources.model.sprite;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public class TextureSlots {
    public static final TextureSlots EMPTY = new TextureSlots(Map.of());
    private static final char REFERENCE_CHAR = '#';
    private final Map<String, Material> resolvedValues;

    public TextureSlots(Map<String, Material> resolvedValues) {
        this.resolvedValues = resolvedValues;
    }

    public Material getMaterial(String reference) {
        if (isTextureReference(reference)) {
            reference = reference.substring(1);
        }

        return this.resolvedValues.get(reference);
    }

    private static boolean isTextureReference(String texture) {
        return texture.charAt(0) == '#';
    }

    public static TextureSlots.Data parseTextureMap(JsonObject texturesObject) {
        TextureSlots.Data.Builder builder = new TextureSlots.Data.Builder();

        for (Entry<String, JsonElement> entry : texturesObject.entrySet()) {
            parseEntry(entry.getKey(), entry.getValue(), builder);
        }

        return builder.build();
    }

    private static void parseEntry(String slot, JsonElement value, TextureSlots.Data.Builder output) {
        if (GsonHelper.isStringValue(value) && isTextureReference(value.getAsString())) {
            output.addReference(slot, value.getAsString().substring(1));
        } else {
            Material material = Material.CODEC.parse(JsonOps.INSTANCE, value).getOrThrow(JsonParseException::new);
            material = ModelManager.fixMaterial(material);
            output.addTexture(slot, material);
        }
    }

    public Map<String, Material> getResolvedValues() {
        return this.resolvedValues;
    }

    @Override
    public String toString() {
        return this.resolvedValues + "";
    }

    public record Data(Map<String, TextureSlots.SlotContents> values) {
        public static final TextureSlots.Data EMPTY = new TextureSlots.Data(Map.of());

        public static class Builder {
            private final Map<String, TextureSlots.SlotContents> textureMap = new HashMap<>();

            public TextureSlots.Data.Builder addReference(String slot, String reference) {
                this.textureMap.put(slot, new TextureSlots.Reference(reference));
                return this;
            }

            public TextureSlots.Data.Builder addTexture(String slot, Material material) {
                this.textureMap.put(slot, new TextureSlots.Value(material));
                return this;
            }

            public TextureSlots.Data build() {
                return this.textureMap.isEmpty() ? TextureSlots.Data.EMPTY : new TextureSlots.Data(Map.copyOf(this.textureMap));
            }
        }
    }

    private record Reference(String target) implements TextureSlots.SlotContents {
    }

    public static class Resolver {
        private static final Logger LOGGER = LogUtils.getLogger();
        private final List<TextureSlots.Data> entries = new ArrayList<>();

        public TextureSlots.Resolver addLast(TextureSlots.Data data) {
            this.entries.addLast(data);
            return this;
        }

        public TextureSlots.Resolver addFirst(TextureSlots.Data data) {
            this.entries.addFirst(data);
            return this;
        }

        public TextureSlots resolve(ModelDebugName debugNameProvider) {
            if (this.entries.isEmpty()) {
                return TextureSlots.EMPTY;
            }

            Object2ObjectMap<String, Material> resolved = new Object2ObjectArrayMap<>();
            Object2ObjectMap<String, TextureSlots.Reference> unresolved = new Object2ObjectArrayMap<>();

            for (TextureSlots.Data data : Lists.reverse(this.entries)) {
                data.values.forEach((slot, contents) -> {
                    switch (contents) {
                        case TextureSlots.Value value:
                            unresolved.remove(slot);
                            resolved.put(slot, value.material());
                            break;
                        case TextureSlots.Reference reference:
                            resolved.remove(slot);
                            unresolved.put(slot, reference);
                            break;
                        default:
                            throw new MatchException(null, null);
                    }
                });
            }

            if (unresolved.isEmpty()) {
                return new TextureSlots(resolved);
            }

            boolean hasChanges = true;

            while (hasChanges) {
                hasChanges = false;
                ObjectIterator<it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<String, TextureSlots.Reference>> iterator = Object2ObjectMaps.fastIterator(
                    unresolved
                );

                while (iterator.hasNext()) {
                    it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<String, TextureSlots.Reference> entry = iterator.next();
                    Material maybeResolved = resolved.get(entry.getValue().target);
                    if (maybeResolved != null) {
                        resolved.put(entry.getKey(), maybeResolved);
                        iterator.remove();
                        hasChanges = true;
                    }
                }
            }

            if (!unresolved.isEmpty()) {
                LOGGER.warn(
                    "Unresolved texture references in {}:\n{}",
                    debugNameProvider.debugName(),
                    unresolved.entrySet().stream().map(e -> "\t#" + e.getKey() + "-> #" + e.getValue().target + "\n").collect(Collectors.joining())
                );
            }

            return new TextureSlots(resolved);
        }
    }

    public sealed interface SlotContents permits TextureSlots.Value, TextureSlots.Reference {
    }

    private record Value(Material material) implements TextureSlots.SlotContents {
    }
}
