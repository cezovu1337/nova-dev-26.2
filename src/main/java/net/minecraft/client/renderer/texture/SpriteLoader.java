package net.minecraft.client.renderer.texture;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.util.TextureUtils;
import org.slf4j.Logger;

public class SpriteLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Identifier location;
    private final int maxSupportedTextureSize;
    private TextureAtlas atlas;

    public SpriteLoader(Identifier location, int maxSupportedTextureSize) {
        this.location = location;
        this.maxSupportedTextureSize = maxSupportedTextureSize;
    }

    public static SpriteLoader create(TextureAtlas atlas) {
        SpriteLoader loader = new SpriteLoader(atlas.location(), atlas.maxSupportedTextureSize());
        loader.atlas = atlas;
        return loader;
    }

    private SpriteLoader.Preparations stitch(List<SpriteContents> sprites, int maxMipmapLevels, Executor executor) {
        int mipmapLevels = this.atlas.mipmapLevel;
        int minSpriteSize = this.atlas.getIconGridSize();

        try (Zone ignored = Profiler.get().zone(() -> "stitch " + this.location)) {
            int maxTextureSize = this.maxSupportedTextureSize;
            int minTexelSize = Integer.MAX_VALUE;
            int lowestOneBit = 1 << maxMipmapLevels;

            for (SpriteContents spriteInfo : sprites) {
                int ws = spriteInfo.getSpriteWidth();
                int hs = spriteInfo.getSpriteHeight();
                if (ws >= 1 && hs >= 1) {
                    if (ws < minSpriteSize || mipmapLevels > 0) {
                        int ws2 = mipmapLevels > 0 ? TextureUtils.scaleToGrid(ws, minSpriteSize) : TextureUtils.scaleToMin(ws, minSpriteSize);
                        if (ws2 != ws) {
                            if (!TextureUtils.isPowerOfTwo(ws)) {
                                Config.log("Scaled non power of 2: " + spriteInfo.getSpriteLocation() + ", " + ws + " -> " + ws2);
                            } else {
                                Config.log("Scaled too small texture: " + spriteInfo.getSpriteLocation() + ", " + ws + " -> " + ws2);
                            }

                            int hs2 = hs * ws2 / ws;
                            double scaleFactor = ws2 * 1.0 / ws;
                            spriteInfo.setSpriteWidth(ws2);
                            spriteInfo.setSpriteHeight(hs2);
                            spriteInfo.setScaleFactor(scaleFactor);
                            spriteInfo.rescale();
                        }
                    }

                    minTexelSize = Math.min(minTexelSize, Math.min(spriteInfo.width(), spriteInfo.height()));
                    int lowestTextureBit = Math.min(Integer.lowestOneBit(spriteInfo.width()), Integer.lowestOneBit(spriteInfo.height()));
                    if (lowestTextureBit < lowestOneBit) {
                        LOGGER.warn(
                            "Texture {} with size {}x{} limits mip level from {} to {}",
                            spriteInfo.name(),
                            spriteInfo.width(),
                            spriteInfo.height(),
                            Mth.log2(lowestOneBit),
                            Mth.log2(lowestTextureBit)
                        );
                        lowestOneBit = lowestTextureBit;
                    }
                } else {
                    Config.warn("Invalid sprite size: " + spriteInfo.getSpriteLocation());
                }
            }

            int minSize = Math.min(minTexelSize, lowestOneBit);
            int minPowerOfTwo = Mth.log2(minSize);
            if (minPowerOfTwo < 0) {
                minPowerOfTwo = 0;
            }

            int mipLevel;
            if (minPowerOfTwo < maxMipmapLevels) {
                LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, maxMipmapLevels, minPowerOfTwo, minSize);
                mipLevel = minPowerOfTwo;
            } else {
                mipLevel = maxMipmapLevels;
            }

            Options options = Minecraft.getInstance().options;
            int anisotropyBit = options.textureFiltering().get() != TextureFilteringMethod.ANISOTROPIC ? 0 : options.maxAnisotropyBit().get();
            Stitcher<SpriteContents> stitcher = new Stitcher<>(maxTextureSize, maxTextureSize, mipLevel, anisotropyBit);

            for (SpriteContents spriteInfo : sprites) {
                stitcher.registerSprite(spriteInfo);
            }

            try {
                stitcher.stitch();
            } catch (StitcherException e) {
                CrashReport report = CrashReport.forThrowable(e, "Stitching");
                CrashReportCategory category = report.addCategory("Stitcher");
                category.setDetail(
                    "Sprites",
                    e.getAllSprites()
                        .stream()
                        .map(s -> String.format(Locale.ROOT, "%s[%dx%d]", s.name(), s.width(), s.height()))
                        .collect(Collectors.joining(","))
                );
                category.setDetail("Max Texture Size", maxTextureSize);
                throw new ReportedException(report);
            }

            int width = stitcher.getWidth();
            int height = stitcher.getHeight();
            Map<Identifier, TextureAtlasSprite> result = this.getStitchedSprites(stitcher, width, height);
            TextureAtlasSprite missingSprite = result.get(MissingTextureAtlasSprite.getLocation());
            CompletableFuture<Void> readyForUpload = CompletableFuture.runAsync(() -> result.values().forEach(s -> {
                s.setTextureAtlas(this.atlas);
                s.increaseMipLevel(mipLevel);
            }), executor);
            return new SpriteLoader.Preparations(width, height, mipLevel, missingSprite, result, readyForUpload);
        }
    }

    private static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(
        SpriteResourceLoader resourceLoader, List<SpriteSource.Loader> sprites, Executor executor
    ) {
        List<CompletableFuture<SpriteContents>> spriteFutures = sprites.stream()
            .map(supplier -> CompletableFuture.supplyAsync(() -> supplier.get(resourceLoader), executor))
            .toList();
        return Util.sequence(spriteFutures).thenApply(l -> l.stream().filter(Objects::nonNull).toList());
    }

    public CompletableFuture<SpriteLoader.Preparations> loadAndStitch(
        ResourceManager manager, Identifier atlasInfoLocation, int maxMipmapLevels, Executor taskExecutor, Set<MetadataSectionType<?>> additionalMetadata
    ) {
        if (Reflector.ForgeHooksClient_getAtlastMetadataSections.exists()) {
            additionalMetadata = (Set<MetadataSectionType<?>>)Reflector.ForgeHooksClient_getAtlastMetadataSections.call(atlasInfoLocation, additionalMetadata);
        }

        SpriteResourceLoader spriteResourceLoader = SpriteResourceLoader.create(additionalMetadata);
        return CompletableFuture.<List<SpriteSource.Loader>>supplyAsync(() -> {
                SpriteSourceList srl = SpriteSourceList.load(manager, atlasInfoLocation);
                Set<Identifier> spriteNames = srl.getSpriteNames(manager);
                srl.filterSpriteNames(spriteNames);
                Set<Identifier> spriteNamesNew = new LinkedHashSet<>(spriteNames);
                this.atlas.preStitch(spriteNamesNew, manager, maxMipmapLevels);
                spriteNamesNew.removeAll(spriteNames);
                srl.addSpriteSources(spriteNamesNew);
                return srl.list(manager);
            }, taskExecutor)
            .thenCompose(sprites -> runSpriteSuppliers(spriteResourceLoader, (List<SpriteSource.Loader>)sprites, taskExecutor))
            .thenApply(resources -> this.stitch((List<SpriteContents>)resources, maxMipmapLevels, taskExecutor));
    }

    private Map<Identifier, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> stitcher, int atlasWidth, int atlasHeight) {
        Map<Identifier, TextureAtlasSprite> result = new HashMap<>();
        stitcher.gatherSprites(
            (contents, x, y, padding) -> {
                if (Reflector.ForgeHooksClient_loadTextureAtlasSprite.exists()) {
                    TextureAtlasSprite sprite = (TextureAtlasSprite)Reflector.ForgeHooksClient_loadTextureAtlasSprite
                        .call(this.location, contents, atlasWidth, atlasHeight, x, y, padding, contents.byMipLevel.length - 1);
                    if (sprite != null) {
                        result.put(contents.name(), sprite);
                        return;
                    }
                }

                TextureAtlasSprite sprite = this.atlas.getRegisteredSprite(contents.name());
                if (sprite != null) {
                    sprite.init(this.location, contents, atlasWidth, atlasHeight, x, y, padding);
                } else {
                    sprite = new TextureAtlasSprite(this.location, contents, atlasWidth, atlasHeight, x, y, padding, this.atlas, null);
                }

                sprite.update(Config.getResourceManager());
                result.put(contents.name(), sprite);
            }
        );
        return result;
    }

    public record Preparations(
        int width, int height, int mipLevel, TextureAtlasSprite missing, Map<Identifier, TextureAtlasSprite> regions, CompletableFuture<Void> readyForUpload
    ) {
        public TextureAtlasSprite getSprite(Identifier id) {
            return this.regions.get(id);
        }
    }
}
