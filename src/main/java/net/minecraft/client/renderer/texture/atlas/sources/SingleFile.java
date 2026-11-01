package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.optifine.util.StrUtils;
import org.slf4j.Logger;

public record SingleFile(Identifier resourceId, Optional<Identifier> spriteId) implements SpriteSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SingleFile> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
                Identifier.CODEC.fieldOf("resource").forGetter(SingleFile::resourceId),
                Identifier.CODEC.optionalFieldOf("sprite").forGetter(SingleFile::spriteId)
            )
            .apply(i, SingleFile::new)
    );
    public static final String PREFIX_VIRTUAL = "_virtual_/";

    public SingleFile(Identifier resourceId) {
        this(resourceId, Optional.empty());
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        Identifier fullResourceId = TEXTURE_ID_CONVERTER.idToFile(this.resourceId);
        if (TextureAtlas.isAbsoluteLocation(this.resourceId)) {
            fullResourceId = new Identifier(this.resourceId.getNamespace(), this.resourceId.getPath() + ".png");
        }

        if (this.resourceId.getPath().startsWith("_virtual_/")) {
            fullResourceId = new Identifier(
                this.resourceId.getNamespace(), "textures/" + StrUtils.removePrefix(this.resourceId.getPath(), "_virtual_/") + ".png"
            );
        }

        Optional<Resource> resource = resourceManager.getResource(fullResourceId);
        if (resource.isPresent()) {
            output.add(this.spriteId.orElse(this.resourceId), resource.get());
        } else {
            LOGGER.warn("Missing sprite: {}", fullResourceId);
        }
    }

    @Override
    public MapCodec<SingleFile> codec() {
        return MAP_CODEC;
    }
}
