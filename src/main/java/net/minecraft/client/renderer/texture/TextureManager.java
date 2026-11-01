package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.screens.AddRealmPopupScreen;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.optifine.Config;
import net.optifine.EmissiveTextures;
import net.optifine.RandomEntities;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.ShadersTex;
import org.slf4j.Logger;

public class TextureManager implements PreparableReloadListener, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Identifier INTENTIONAL_MISSING_TEXTURE = Identifier.withDefaultNamespace("");
    private final Map<Identifier, AbstractTexture> byPath = new HashMap<>();
    private final Set<TickableTexture> tickableTextures = new HashSet<>();
    private final ResourceManager resourceManager;
    private AbstractTexture mojangLogoTexture;

    public TextureManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        NativeImage checkerboard = MissingTextureAtlasSprite.generateMissingImage();
        this.register(MissingTextureAtlasSprite.getLocation(), new DynamicTexture(() -> "(intentionally-)Missing Texture", checkerboard));
    }

    public void registerAndLoad(Identifier textureId, ReloadableTexture texture) {
        try {
            texture.apply(this.loadContentsSafe(textureId, texture));
        } catch (Throwable t) {
            CrashReport report = CrashReport.forThrowable(t, "Uploading texture");
            CrashReportCategory category = report.addCategory("Uploaded texture");
            category.setDetail("Resource location", texture.resourceId());
            category.setDetail("Texture id", textureId);
            throw new ReportedException(report);
        }

        this.register(textureId, texture);
    }

    private TextureContents loadContentsSafe(Identifier textureId, ReloadableTexture texture) {
        try {
            return loadContents(this.resourceManager, textureId, texture);
        } catch (Exception e) {
            LOGGER.error("Failed to load texture {} into slot {}", texture.resourceId(), textureId, e);
            return TextureContents.createMissing();
        }
    }

    public void registerForNextReload(Identifier location) {
        this.register(location, new SimpleTexture(location));
    }

    public void register(Identifier location, AbstractTexture texture) {
        if (Reflector.MinecraftForge.exists() && this.mojangLogoTexture == null && location.equals(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION)) {
            LOGGER.info("Keep logo texture for ForgeLoadingOverlay: " + texture);
            this.mojangLogoTexture = texture;
        }

        AbstractTexture prev = this.byPath.put(location, texture);
        if (prev != texture) {
            if (prev != null && texture != this.mojangLogoTexture) {
                this.safeClose(location, prev);
            }

            if (texture instanceof TickableTexture tickableTexture) {
                this.tickableTextures.add(tickableTexture);
            }
        }
    }

    private void safeClose(Identifier id, AbstractTexture texture) {
        this.tickableTextures.remove(texture);

        try {
            texture.close();
        } catch (Exception e) {
            LOGGER.warn("Failed to close texture {}", id, e);
        }
    }

    public AbstractTexture getTexture(Identifier location) {
        AbstractTexture textureObject = this.byPath.get(location);
        if (textureObject != null) {
            return textureObject;
        }

        SimpleTexture texture = new SimpleTexture(location);
        this.registerAndLoad(location, texture);
        return texture;
    }

    public AbstractTexture getTexture(net.minecraft.util.ResourceLocation location) {
        return this.getTexture(location.toIdentifier());
    }

    public void loadTexture(net.minecraft.util.ResourceLocation location, AbstractTexture texture) {
        this.register(location.toIdentifier(), texture);
    }

    public void tick() {
        RenderSystem.getGlDevice().debugLabels().pushDebugGroup(() -> "Animations");

        for (TickableTexture tickableTexture : this.tickableTextures) {
            tickableTexture.tick();
        }

        RenderSystem.getGlDevice().debugLabels().popDebugGroup();
    }

    public void release(Identifier location) {
        AbstractTexture texture = this.byPath.remove(location);
        if (texture != null) {
            this.safeClose(location, texture);
        }
    }

    @Override
    public void close() {
        this.byPath.forEach(this::safeClose);
        this.byPath.clear();
        this.tickableTextures.clear();
    }

    @Override
    public CompletableFuture<Void> reload(
        PreparableReloadListener.SharedState currentReload,
        Executor taskExecutor,
        PreparableReloadListener.PreparationBarrier preparationBarrier,
        Executor reloadExecutor
    ) {
        Config.dbg("*** Reloading textures ***");
        Iterator it = this.byPath.keySet().iterator();

        while (it.hasNext()) {
            Identifier loc = (Identifier)it.next();
            String path = loc.getPath();
            if (path.startsWith("optifine/") || EmissiveTextures.isEmissive(loc)) {
                AbstractTexture tex = this.byPath.get(loc);
                if (tex instanceof AbstractTexture at) {
                    at.deleteGlTexture();
                }

                it.remove();
            }
        }

        RandomEntities.update();
        EmissiveTextures.update();
        ResourceManager manager = currentReload.resourceManager();
        List<TextureManager.PendingReload> reloads = new ArrayList<>();
        Map<Identifier, AbstractTexture> mapTexturesCopy = new HashMap<>(this.byPath);
        mapTexturesCopy.forEach((id, texture) -> {
            if (Config.isShaders() && texture instanceof DynamicTexture dynTex && texture.hasGlTexture()) {
                ShadersTex.initDynamicTextureNS(dynTex);
            }

            if (texture instanceof ReloadableTexture reloadableTexture) {
                reloads.add(scheduleLoad(manager, id, reloadableTexture, taskExecutor));
            }
        });
        return CompletableFuture.allOf(reloads.stream().map(TextureManager.PendingReload::newContents).toArray(CompletableFuture[]::new))
            .thenCompose(preparationBarrier::wait)
            .thenAcceptAsync(unused -> {
                AddRealmPopupScreen.updateCarouselImages(this.resourceManager);

                for (TextureManager.PendingReload reload : reloads) {
                    reload.texture.apply(reload.newContents.join());
                }
            }, reloadExecutor);
    }

    public void dumpAllSheets(Path targetDir) {
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create directory {}", targetDir, e);
            return;
        }

        this.byPath.forEach((location, texture) -> {
            if (texture instanceof Dumpable dumpable) {
                try {
                    dumpable.dumpContents(location, targetDir);
                } catch (Exception e) {
                    LOGGER.error("Failed to dump texture {}", location, e);
                }
            }
        });
    }

    private static TextureContents loadContents(ResourceManager manager, Identifier location, ReloadableTexture texture) throws IOException {
        try {
            return texture.loadContents(manager);
        } catch (FileNotFoundException e) {
            if (location != INTENTIONAL_MISSING_TEXTURE) {
                LOGGER.warn("Missing resource {} referenced from {}", texture.resourceId(), location);
            }

            return TextureContents.createMissing();
        }
    }

    private static TextureManager.PendingReload scheduleLoad(ResourceManager manager, Identifier location, ReloadableTexture texture, Executor executor) {
        return new TextureManager.PendingReload(texture, CompletableFuture.supplyAsync(() -> {
            try {
                return loadContents(manager, location, texture);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, executor));
    }

    public boolean hasTexture(Identifier loc) {
        return this.byPath.containsKey(loc);
    }

    public Collection<AbstractTexture> getTextures() {
        return this.byPath.values();
    }

    public Collection<Identifier> getTextureLocations() {
        return this.byPath.keySet();
    }

    public void bindTexture(Identifier resource) {
        AbstractTexture abstracttexture = this.getTexture(resource);
        abstracttexture.bindTexture();
    }

    public void bindTexture(net.minecraft.util.ResourceLocation resource) {
        if (resource != null) {
            this.bindTexture(resource.toIdentifier());
        }
    }

    private record PendingReload(ReloadableTexture texture, CompletableFuture<TextureContents> newContents) {
    }
}
