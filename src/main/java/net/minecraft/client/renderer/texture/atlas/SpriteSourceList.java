package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import net.optifine.shaders.ShadersTextureType;
import net.optifine.texture.SpriteSourceCollector;
import net.optifine.util.StrUtils;
import org.slf4j.Logger;

public class SpriteSourceList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter ATLAS_INFO_CONVERTER = new FileToIdConverter("atlases", ".json");
    private final List<SpriteSource> sources;

    private SpriteSourceList(List<SpriteSource> sources) {
        this.sources = sources;
    }

    public List<SpriteSource.Loader> list(ResourceManager resourceManager) {
        final Map<Identifier, SpriteSource.DiscardableLoader> sprites = new HashMap<>();
        SpriteSource.Output output = new SpriteSource.Output() {
            @Override
            public void add(Identifier id, SpriteSource.DiscardableLoader sprite) {
                SpriteSource.DiscardableLoader previous = sprites.put(id, sprite);
                if (previous != null) {
                    previous.discard();
                }
            }

            @Override
            public void removeAll(Predicate<Identifier> predicate) {
                Iterator<Entry<Identifier, SpriteSource.DiscardableLoader>> it = sprites.entrySet().iterator();

                while (it.hasNext()) {
                    Entry<Identifier, SpriteSource.DiscardableLoader> entry = it.next();
                    if (predicate.test(entry.getKey())) {
                        entry.getValue().discard();
                        it.remove();
                    }
                }
            }
        };
        this.sources.forEach(s -> s.run(resourceManager, output));
        this.filterSpriteNames(sprites.keySet());
        Builder<SpriteSource.Loader> result = ImmutableList.builder();
        result.add(loader -> MissingTextureAtlasSprite.create());
        result.addAll(sprites.values());
        return result.build();
    }

    public static SpriteSourceList load(ResourceManager resourceManager, Identifier atlasId) {
        Identifier resourceId = ATLAS_INFO_CONVERTER.idToFile(atlasId);
        List<SpriteSource> loaders = new ArrayList<>();

        for (Resource entry : resourceManager.getResourceStack(resourceId)) {
            try (BufferedReader reader = entry.openAsReader()) {
                Dynamic<JsonElement> contents = new Dynamic<>(JsonOps.INSTANCE, StrictJsonParser.parse(reader));
                loaders.addAll(SpriteSources.FILE_CODEC.parse(contents).getOrThrow());
            } catch (Exception e) {
                LOGGER.error("Failed to parse atlas definition {} in pack {}", resourceId, entry.sourcePackId(), e);
            }
        }

        return new SpriteSourceList(loaders);
    }

    public void addSpriteSources(Collection<Identifier> spriteNames) {
        for (Identifier spriteName : spriteNames) {
            this.sources.add(new SingleFile(spriteName, Optional.empty()));
        }
    }

    public List<SpriteSource> getSpriteSources() {
        return this.sources;
    }

    public Set<Identifier> getSpriteNames(ResourceManager resourceManager) {
        Set<Identifier> spriteNames = new LinkedHashSet<>();

        for (SpriteSource source : this.sources) {
            SpriteSource.Output out = new SpriteSourceCollector(spriteNames);
            source.run(resourceManager, out);
        }

        return spriteNames;
    }

    public void filterSpriteNames(Set<Identifier> spriteNames) {
        String suffixNormal = ShadersTextureType.NORMAL.getSuffix();
        String suffixSpecular = ShadersTextureType.SPECULAR.getSuffix();
        String[] suffixesShaders = new String[]{suffixNormal, suffixSpecular};
        Iterator it = spriteNames.iterator();

        while (it.hasNext()) {
            Identifier loc = (Identifier)it.next();
            String path = loc.getPath();
            if (path.endsWith(suffixNormal) || path.endsWith(suffixSpecular)) {
                String pathBase = StrUtils.removeSuffix(path, suffixesShaders);
                Identifier locBase = new Identifier(loc.getNamespace(), pathBase);
                if (spriteNames.contains(locBase)) {
                    it.remove();
                }
            }
        }
    }
}
