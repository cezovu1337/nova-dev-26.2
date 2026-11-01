package net.minecraft.client.renderer.chunk;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.model.data.ModelDataManager;
import net.optifine.override.ChunkCacheOF;

public class RenderSectionRegion implements BlockAndTintGetter {
    public static final int RADIUS = 1;
    public static final int SIZE = 3;
    private final int minSectionX;
    private final int minSectionY;
    private final int minSectionZ;
    private final SectionCopy[] sections;
    private final ClientLevel level;
    private final CardinalLighting cardinalLighting;
    private final LevelLightEngine lightEngine;
    private SectionPos sectionPos;
    private RenderRegionCache regionCache;

    public RenderSectionRegion(ClientLevel level, int minSectionX, int minSectionY, int minSectionZ, SectionCopy[] sections) {
        this(level, minSectionX, minSectionY, minSectionZ, sections, null, null);
    }

    public RenderSectionRegion(
        ClientLevel level, int minSectionX, int minSectionY, int minSectionZ, SectionCopy[] sections, SectionPos sectionPosIn, RenderRegionCache regionCacheIn
    ) {
        this.level = level;
        this.minSectionX = minSectionX;
        this.minSectionY = minSectionY;
        this.minSectionZ = minSectionZ;
        this.sections = sections;
        this.cardinalLighting = level.cardinalLighting();
        this.lightEngine = level.getLightEngine();
        this.sectionPos = sectionPosIn;
        this.regionCache = regionCacheIn;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.getSection(
                SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()), SectionPos.blockToSectionCoord(pos.getZ())
            )
            .getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getSection(
                SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()), SectionPos.blockToSectionCoord(pos.getZ())
            )
            .getBlockState(pos)
            .getFluidState();
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return this.cardinalLighting;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.getSection(
                SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()), SectionPos.blockToSectionCoord(pos.getZ())
            )
            .getBlockEntity(pos);
    }

    public SectionCopy getSection(int sectionX, int sectionY, int sectionZ) {
        return this.sections[index(this.minSectionX, this.minSectionY, this.minSectionZ, sectionX, sectionY, sectionZ)];
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return this.level.getBlockTint(pos, resolver);
    }

    @Override
    public int getMinY() {
        return this.level.getMinY();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    public static int index(int minSectionX, int minSectionY, int minSectionZ, int sectionX, int sectionY, int sectionZ) {
        return sectionX - minSectionX + (sectionY - minSectionY) * 3 + (sectionZ - minSectionZ) * 3 * 3;
    }

    public Biome getBiome(BlockPos pos) {
        return this.level.getBiome(pos).value();
    }

    public LevelChunk getLevelChunk(int cx, int cy, int cz) {
        return this.getSection(cx, cy, cz).getChunk();
    }

    public ChunkCacheOF makeChunkCacheOF() {
        return this.sectionPos == null ? null : new ChunkCacheOF(this, this.sectionPos);
    }

    public int getMinChunkX() {
        return this.minSectionX;
    }

    public int getMinChunkY() {
        return this.minSectionY;
    }

    public int getMinChunkZ() {
        return this.minSectionZ;
    }

    public RenderRegionCache getRegionCache() {
        return this.regionCache;
    }

    public ModelDataManager getModelDataManager() {
        if (this.level instanceof ClientLevel) {
            ClientLevel clientWorld = this.level;
            return clientWorld.getModelDataManager();
        } else {
            return null;
        }
    }
}
