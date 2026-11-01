package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.datafixers.DataFixUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debugchart.BandwidthDebugChart;
import net.minecraft.client.gui.components.debugchart.FpsDebugChart;
import net.minecraft.client.gui.components.debugchart.PingDebugChart;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;
import net.minecraft.client.gui.components.debugchart.TpsDebugChart;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.optifine.Lagometer;
import net.optifine.QuickInfo;
import net.optifine.render.GuiRenderCache;
import net.optifine.util.Mutable;

public class DebugScreenOverlay {
    private static final int MARGIN_RIGHT = 2;
    private static final int MARGIN_LEFT = 2;
    private static final int MARGIN_TOP = 2;
    private final Minecraft minecraft;
    private final Font font;
    private ChunkPos lastPos;
    private LevelChunk clientChunk;
    private CompletableFuture<LevelChunk> serverChunk;
    private boolean renderProfilerChart;
    private boolean renderFpsCharts;
    private boolean renderNetworkCharts;
    private boolean renderLightmapTexture;
    private final LocalSampleLogger frameTimeLogger = new LocalSampleLogger(1);
    private final LocalSampleLogger tickTimeLogger = new LocalSampleLogger(TpsDebugDimensions.values().length);
    private final LocalSampleLogger pingLogger = new LocalSampleLogger(1);
    private final LocalSampleLogger bandwidthLogger = new LocalSampleLogger(1);
    private final Map<RemoteDebugSampleType, LocalSampleLogger> remoteSupportingLoggers = Map.of(RemoteDebugSampleType.TICK_TIME, this.tickTimeLogger);
    private final FpsDebugChart fpsChart;
    private final TpsDebugChart tpsChart;
    private final PingDebugChart pingChart;
    private final BandwidthDebugChart bandwidthChart;
    private final ProfilerPieChart profilerPieChart;
    private GuiRenderCache renderCache;

    public DebugScreenOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.font = minecraft.font;
        this.fpsChart = new FpsDebugChart(this.font, this.frameTimeLogger);
        this.tpsChart = new TpsDebugChart(
            this.font, this.tickTimeLogger, () -> minecraft.level == null ? 0.0F : minecraft.level.tickRateManager().millisecondsPerTick()
        );
        this.pingChart = new PingDebugChart(this.font, this.pingLogger);
        this.bandwidthChart = new BandwidthDebugChart(this.font, this.bandwidthLogger);
        this.profilerPieChart = new ProfilerPieChart(this.font);
        this.renderCache = new GuiRenderCache(minecraft.gameRenderer.getGuiRenderer(), 100L);
    }

    public void clearChunkCache() {
        this.serverChunk = null;
        this.clientChunk = null;
    }

    public void extractRenderState(GuiGraphicsExtractor graphics) {
        Options options = this.minecraft.options;
        if (this.minecraft.isGameLoadFinished() && (!this.minecraft.gui.hud.isHidden() || this.minecraft.gui.screen() != null)) {
            Collection<Identifier> visibleEntries = this.minecraft.debugEntries.getCurrentlyEnabled();
            boolean debugLinesRendered = false;
            if (!visibleEntries.isEmpty()) {
                graphics.nextStratum();
                ProfilerFiller profiler = Profiler.get();
                profiler.push("debug");
                ChunkPos chunkPos;
                if (this.minecraft.getCameraEntity() != null && this.minecraft.level != null) {
                    BlockPos feetPos = this.minecraft.getCameraEntity().blockPosition();
                    chunkPos = ChunkPos.containing(feetPos);
                } else {
                    chunkPos = null;
                }

                if (!Objects.equals(this.lastPos, chunkPos)) {
                    this.lastPos = chunkPos;
                    this.clearChunkCache();
                }

                List<String> leftLines = new ArrayList<>();
                List<String> rightLines = new ArrayList<>();
                final List<String> linesLeft = leftLines;
                final List<String> linesRight = rightLines;
                final Mutable<Identifier> debugLocation = new Mutable<>();
                DebugScreenDisplayer displayer = new DebugScreenDisplayer() {
                    private int indexFps = -1;

                    @Override
                    public void addPriorityLine(String line) {
                        this.addLine(line);
                    }

                    @Override
                    public void addLine(String line) {
                        if (DebugScreenEntries.isFpsLine(debugLocation.get())) {
                            if (this.indexFps >= 0) {
                                String lineFps = linesLeft.get(this.indexFps);
                                lineFps = lineFps + " " + line;
                                linesLeft.set(this.indexFps, lineFps);
                                return;
                            }

                            this.indexFps = linesLeft.size();
                        }

                        List<String> lines = DebugScreenEntries.isLineLeft(debugLocation.get()) ? linesLeft : linesRight;
                        lines.add(line);
                    }

                    @Override
                    public void addToGroup(Identifier group, Collection<String> lines) {
                        List<String> lines2 = DebugScreenEntries.isLineLeft(debugLocation.get()) ? linesLeft : linesRight;
                        if (DebugScreenEntries.isNewLine(debugLocation.get())) {
                            lines2.add("");
                        }

                        lines2.addAll(lines);
                    }

                    @Override
                    public void addToGroup(Identifier group, String lines) {
                        List<String> lines2 = DebugScreenEntries.isLineLeft(debugLocation.get()) ? linesLeft : linesRight;
                        if (DebugScreenEntries.isNewLine(debugLocation.get())) {
                            lines2.add("");
                        }

                        lines2.add(lines);
                    }
                };
                Level level = this.getLevel();

                for (Identifier id : visibleEntries) {
                    debugLocation.set(id);
                    DebugScreenEntry entry = DebugScreenEntries.getEntry(id);
                    if (entry != null) {
                        entry.display(displayer, level, this.getClientChunk(), this.getServerChunk());
                    }
                }

                if (this.minecraft.debugEntries.isOverlayVisible()) {
                    leftLines.add("");
                    boolean hasServer = this.minecraft.getSingleplayerServer() != null;
                    KeyMapping keyDebugModifier = options.keyDebugModifier;
                    leftLines.add(
                        "Debug charts: "
                            + formatChart(keyDebugModifier, options.keyDebugPofilingChart, "Profiler", this.renderProfilerChart)
                            + "; "
                            + formatChart(keyDebugModifier, options.keyDebugFpsCharts, hasServer ? "FPS + TPS" : "FPS", this.renderFpsCharts)
                            + ";"
                    );
                    leftLines.add(
                        formatChart(
                                keyDebugModifier,
                                options.keyDebugNetworkCharts,
                                !this.minecraft.isLocalServer() ? "Bandwidth + Ping" : "Ping",
                                this.renderNetworkCharts
                            )
                            + "; "
                            + formatChart(keyDebugModifier, options.keyDebugLightmapTexture, "Lightmap", this.renderLightmapTexture)
                    );
                    leftLines.add("To edit: press " + formatKeybind(keyDebugModifier, options.keyDebugDebugOptions));
                }

                this.extractLines(graphics, leftLines, true);
                this.extractLines(graphics, rightLines, false);
                debugLinesRendered = !leftLines.isEmpty() || !rightLines.isEmpty();
                graphics.nextStratum();
                this.profilerPieChart.setBottomOffset(10);
                if (this.showFpsCharts()) {
                    int scaledWidth = graphics.guiWidth();
                    int maxWidth = scaledWidth / 2;
                    if (Boolean.TRUE) {
                        Lagometer.renderLagometer(graphics, this.minecraft.getWindow().getGuiScale());
                    } else {
                        this.fpsChart.extractRenderState(graphics, 0, this.fpsChart.getWidth(maxWidth));
                    }

                    if (this.tickTimeLogger.size() > 0) {
                        int width = this.tpsChart.getWidth(maxWidth);
                        this.tpsChart.extractRenderState(graphics, scaledWidth - width, width);
                    }

                    this.profilerPieChart.setBottomOffset(this.tpsChart.getFullHeight());
                }

                if (this.showNetworkCharts() && this.minecraft.getConnection() != null) {
                    int scaledWidth = graphics.guiWidth();
                    int maxWidth = scaledWidth / 2;
                    if (!this.minecraft.isLocalServer()) {
                        this.bandwidthChart.extractRenderState(graphics, 0, this.bandwidthChart.getWidth(maxWidth));
                    }

                    int width = this.pingChart.getWidth(maxWidth);
                    this.pingChart.extractRenderState(graphics, scaledWidth - width, width);
                    this.profilerPieChart.setBottomOffset(this.pingChart.getFullHeight());
                }

                if (this.showLightmapTexture()) {
                    GpuTextureView lightmapTextureView = this.minecraft.gameRenderer.levelLightmap();
                    int displaySize = 64;
                    int x = graphics.guiWidth() - 64 - 2;
                    int y = graphics.guiHeight() - 64 - 2;
                    graphics.fill(x - 1, y - 1, x + 64 + 1, y + 64 + 1, -16777216);
                    graphics.blit(
                        lightmapTextureView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST), x, y, x + 64, y + 64, 0.0F, 1.0F, 1.0F, 0.0F
                    );
                }

                if (this.minecraft.debugEntries.isCurrentlyEnabled(DebugScreenEntries.VISUALIZE_CHUNKS_ON_SERVER)) {
                    IntegratedServer singleplayerServer = this.minecraft.getSingleplayerServer();
                    if (singleplayerServer != null && this.minecraft.player != null) {
                        ChunkLoadStatusView statusView = singleplayerServer.createChunkLoadStatusView(16 + ChunkLevel.RADIUS_AROUND_FULL_CHUNK);
                        statusView.moveTo(this.minecraft.player.level().dimension(), this.minecraft.player.chunkPosition());
                        LevelLoadingScreen.extractChunksForRendering(graphics, graphics.guiWidth() / 2, graphics.guiHeight() / 2, 4, 1, statusView);
                    }
                }

                try (Zone ignored = profiler.zone("profilerPie")) {
                    this.profilerPieChart.extractRenderState(graphics);
                }

                profiler.pop();
            }

            if (!debugLinesRendered && this.minecraft.options.ofQuickInfo) {
                graphics.nextStratum();
                QuickInfo.render(graphics);
            }
        }
    }

    private static String formatChart(KeyMapping keyDebugModifier, KeyMapping keybind, String name, boolean status) {
        return formatKeybind(keyDebugModifier, keybind) + " " + name + " " + (status ? "visible" : "hidden");
    }

    private static String formatKeybind(KeyMapping keyDebugModifier, KeyMapping keybind) {
        return "["
            + (keyDebugModifier.isUnbound() ? "" : keyDebugModifier.getTranslatedKeyMessage().getString() + "+")
            + keybind.getTranslatedKeyMessage().getString()
            + "]";
    }

    private void extractLines(GuiGraphicsExtractor graphics, List<String> lines, boolean alignLeft) {
        int height = 9;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!Strings.isNullOrEmpty(line)) {
                int width = this.font.width(line);
                int left = alignLeft ? 2 : graphics.guiWidth() - 2 - width;
                int top = 2 + height * i;
                graphics.fill(left - 1, top - 1, left + width + 1, top + height - 1, -1873784752);
            }
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!Strings.isNullOrEmpty(line)) {
                int width = this.font.width(line);
                int left = alignLeft ? 2 : graphics.guiWidth() - 2 - width;
                int top = 2 + height * i;
                graphics.text(this.font, line, left, top, -2039584, false);
            }
        }
    }

    private ServerLevel getServerLevel() {
        if (this.minecraft.level == null) {
            return null;
        }

        IntegratedServer server = this.minecraft.getSingleplayerServer();
        return server != null ? server.getLevel(this.minecraft.level.dimension()) : null;
    }

    private Level getLevel() {
        return this.minecraft.level == null
            ? null
            : DataFixUtils.orElse(
                Optional.ofNullable(this.minecraft.getSingleplayerServer()).flatMap(s -> Optional.ofNullable(s.getLevel(this.minecraft.level.dimension()))),
                this.minecraft.level
            );
    }

    private LevelChunk getServerChunk() {
        if (this.minecraft.level != null && this.lastPos != null) {
            if (this.serverChunk == null) {
                ServerLevel level = this.getServerLevel();
                if (level == null) {
                    return null;
                }

                this.serverChunk = level.getChunkSource()
                    .getChunkFuture(this.lastPos.x(), this.lastPos.z(), ChunkStatus.FULL, false)
                    .thenApply(chunkResult -> (LevelChunk)chunkResult.orElse(null));
            }

            return this.serverChunk.getNow(null);
        } else {
            return null;
        }
    }

    private LevelChunk getClientChunk() {
        if (this.minecraft.level != null && this.lastPos != null) {
            if (this.clientChunk == null) {
                this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x(), this.lastPos.z());
            }

            return this.clientChunk;
        } else {
            return null;
        }
    }

    public boolean showDebugScreen() {
        DebugScreenEntryList entries = this.minecraft.debugEntries;
        return (entries.isOverlayVisible() || !entries.getCurrentlyEnabled().isEmpty())
            && (!this.minecraft.gui.hud.isHidden() || this.minecraft.gui.screen() != null);
    }

    public boolean showProfilerChart() {
        return this.minecraft.debugEntries.isOverlayVisible() && this.renderProfilerChart;
    }

    public boolean showNetworkCharts() {
        return this.minecraft.debugEntries.isOverlayVisible() && this.renderNetworkCharts;
    }

    public boolean showFpsCharts() {
        return this.minecraft.debugEntries.isOverlayVisible() && this.renderFpsCharts;
    }

    public boolean showLightmapTexture() {
        return this.minecraft.debugEntries.isOverlayVisible() && this.renderLightmapTexture;
    }

    public void toggleNetworkCharts() {
        this.renderNetworkCharts = !this.minecraft.debugEntries.isOverlayVisible() || !this.renderNetworkCharts;
        if (this.renderNetworkCharts) {
            this.minecraft.debugEntries.setOverlayVisible(true);
            this.renderFpsCharts = false;
            this.renderLightmapTexture = false;
        }
    }

    public void toggleFpsCharts() {
        this.renderFpsCharts = !this.minecraft.debugEntries.isOverlayVisible() || !this.renderFpsCharts;
        if (this.renderFpsCharts) {
            this.minecraft.debugEntries.setOverlayVisible(true);
            this.renderNetworkCharts = false;
            this.renderLightmapTexture = false;
        }
    }

    public void toggleLightmapTexture() {
        this.renderLightmapTexture = !this.minecraft.debugEntries.isOverlayVisible() || !this.renderLightmapTexture;
        if (this.renderLightmapTexture) {
            this.minecraft.debugEntries.setOverlayVisible(true);
            this.renderFpsCharts = false;
            this.renderNetworkCharts = false;
        }
    }

    public void toggleProfilerChart() {
        this.renderProfilerChart = !this.minecraft.debugEntries.isOverlayVisible() || !this.renderProfilerChart;
        if (this.renderProfilerChart) {
            this.minecraft.debugEntries.setOverlayVisible(true);
        }
    }

    public void logFrameDuration(long frameDuration) {
        this.frameTimeLogger.logSample(frameDuration);
    }

    public LocalSampleLogger getTickTimeLogger() {
        return this.tickTimeLogger;
    }

    public LocalSampleLogger getPingLogger() {
        return this.pingLogger;
    }

    public LocalSampleLogger getBandwidthLogger() {
        return this.bandwidthLogger;
    }

    public ProfilerPieChart getProfilerPieChart() {
        return this.profilerPieChart;
    }

    public void logRemoteSample(long[] sample, RemoteDebugSampleType type) {
        LocalSampleLogger logger = this.remoteSupportingLoggers.get(type);
        if (logger != null) {
            logger.logFullSample(sample);
        }
    }

    public void reset() {
        this.tickTimeLogger.reset();
        this.pingLogger.reset();
        this.bandwidthLogger.reset();
    }

    public LocalSampleLogger getFrameTimeLogger() {
        return this.frameTimeLogger;
    }

    public boolean isRenderFpsCharts() {
        return this.renderFpsCharts;
    }

    public boolean isRenderProfilerChart() {
        return this.renderProfilerChart;
    }
}
