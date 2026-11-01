package net.minecraft.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.optifine.Config;
import net.optifine.render.RegionLayerRenderer;
import net.optifine.render.VboRegion;
import net.optifine.util.MemberIterator;

public class RotatingSectionStorage<T extends RotatingSectionStorage.Value> implements Iterable<T> {
    private final RotatingSectionStorage.Node<T>[] nodes;
    private final int radius;
    private final int minY;
    private final int maxY;
    private final int sectionGridSizeY;
    private final int sectionGridSizeXZ;
    private SectionPos centerSectionPos = SectionPos.of(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    private Map<ChunkPos, VboRegion[]> mapVboRegions = new HashMap<>();

    public RotatingSectionStorage(int radius, int minY, int maxY, RotatingSectionStorage.ValueCreator<T> valueCreator) {
        this.radius = radius;
        this.minY = minY;
        this.maxY = maxY;
        this.sectionGridSizeY = maxY - minY + 1;
        this.sectionGridSizeXZ = radius * 2 + 1;
        int totalSections = this.sectionGridSizeXZ * this.sectionGridSizeXZ * this.sectionGridSizeY;
        this.nodes = new RotatingSectionStorage.Node[totalSections];

        for (int x = 0; x < this.sectionGridSizeXZ; x++) {
            for (int y = 0; y < this.sectionGridSizeY; y++) {
                for (int z = 0; z < this.sectionGridSizeXZ; z++) {
                    int index = this.getSectionIndex(x, y, z);
                    long sectionNode = SectionPos.asLong(x, y + minY, z);
                    this.nodes[index] = new RotatingSectionStorage.Node<>(valueCreator.createValue(index, sectionNode));
                    if (Config.isRenderRegions()) {
                        SectionRenderDispatcher.RenderSection rs = this.nodes[index].getRenderSection();
                        if (rs != null) {
                            this.updateVboRegion(rs);
                        }
                    }
                }
            }
        }
    }

    public boolean repositionCenter(SectionPos newCenterSectionPos) {
        if (newCenterSectionPos.equals(this.centerSectionPos)) {
            return false;
        }

        int lowestX = newCenterSectionPos.x() - this.radius;
        int lowestZ = newCenterSectionPos.z() - this.radius;

        for (int gridX = 0; gridX < this.sectionGridSizeXZ; gridX++) {
            int newSectionX = lowestX + Math.floorMod(gridX - lowestX, this.sectionGridSizeXZ);

            for (int gridZ = 0; gridZ < this.sectionGridSizeXZ; gridZ++) {
                int newSectionZ = lowestZ + Math.floorMod(gridZ - lowestZ, this.sectionGridSizeXZ);

                for (int gridY = 0; gridY < this.sectionGridSizeY; gridY++) {
                    int newSectionY = this.minY + gridY;
                    T value = this.nodes[this.getSectionIndex(gridX, gridY, gridZ)].value;
                    long sectionNode = value.getSectionNode();
                    if (sectionNode != SectionPos.asLong(newSectionX, newSectionY, newSectionZ)) {
                        value.setSectionNode(SectionPos.asLong(newSectionX, newSectionY, newSectionZ));
                    }
                }
            }
        }

        this.centerSectionPos = newCenterSectionPos;
        return true;
    }

    public int radius() {
        return this.radius;
    }

    public int minY() {
        return this.minY;
    }

    public int maxY() {
        return this.maxY;
    }

    public int height() {
        return this.sectionGridSizeY;
    }

    public SectionPos centerSectionPos() {
        return this.centerSectionPos;
    }

    public T getValueAt(BlockPos pos) {
        return this.getValue(SectionPos.asLong(pos));
    }

    public T getValue(long sectionNode) {
        int sectionX = SectionPos.x(sectionNode);
        int sectionY = SectionPos.y(sectionNode);
        int sectionZ = SectionPos.z(sectionNode);
        return this.getValue(sectionX, sectionY, sectionZ);
    }

    public T getValue(int sectionX, int sectionY, int sectionZ) {
        if (!this.containsSection(sectionX, sectionY, sectionZ)) {
            return null;
        }

        int y = sectionY - this.minY;
        int x = Math.floorMod(sectionX, this.sectionGridSizeXZ);
        int z = Math.floorMod(sectionZ, this.sectionGridSizeXZ);
        return this.nodes[this.getSectionIndex(x, y, z)].value;
    }

    private boolean containsSection(int sectionX, int sectionY, int sectionZ) {
        if (sectionY >= this.minY && sectionY <= this.maxY) {
            return sectionX >= this.centerSectionPos.x() - this.radius && sectionX <= this.centerSectionPos.x() + this.radius
                ? sectionZ >= this.centerSectionPos.z() - this.radius && sectionZ <= this.centerSectionPos.z() + this.radius
                : false;
        } else {
            return false;
        }
    }

    private int getSectionIndex(int x, int y, int z) {
        return (z * this.sectionGridSizeY + y) * this.sectionGridSizeXZ + x;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int i;

            @Override
            public boolean hasNext() {
                return this.i < RotatingSectionStorage.this.nodes.length - 1;
            }

            public T next() {
                if (this.i >= RotatingSectionStorage.this.nodes.length) {
                    throw new NoSuchElementException();
                } else {
                    return RotatingSectionStorage.this.nodes[this.i++].value;
                }
            }
        };
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (RotatingSectionStorage.Node<T> node : this.nodes) {
            action.accept(node.value);
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(this.iterator(), this.nodes.length, 0);
    }

    public int size() {
        return this.nodes.length;
    }

    private void updateVboRegion(SectionRenderDispatcher.RenderSection renderChunk) {
        BlockPos pos = renderChunk.getPosition();
        int rx = pos.getX() >> 8 << 8;
        int rz = pos.getZ() >> 8 << 8;
        ChunkPos cp = new ChunkPos(rx, rz);
        ChunkSectionLayer[] layers = ChunkSectionLayer.VALUES;
        VboRegion[] regions = this.mapVboRegions.get(cp);
        if (regions == null) {
            regions = new VboRegion[layers.length];

            for (int ix = 0; ix < layers.length; ix++) {
                if (RegionLayerRenderer.canRenderLayer(layers[ix])) {
                    regions[ix] = new VboRegion(cp, layers[ix]);
                }
            }

            this.mapVboRegions.put(cp, regions);
        }

        renderChunk.setVboRegions(regions);
    }

    public void deleteVboRegions() {
        for (ChunkPos cp : this.mapVboRegions.keySet()) {
            VboRegion[] vboRegions = this.mapVboRegions.get(cp);

            for (int i = 0; i < vboRegions.length; i++) {
                VboRegion vboRegion = vboRegions[i];
                if (vboRegion != null) {
                    vboRegion.deleteGlBuffers();
                }

                vboRegions[i] = null;
            }
        }

        this.mapVboRegions.clear();
    }

    public int getHighestUsedChunkIndex(int chunkX, int minChunkIndex, int chunkZ) {
        chunkX = Mth.positiveModulo(chunkX, this.sectionGridSizeXZ);
        minChunkIndex = Mth.clamp(minChunkIndex, 0, this.sectionGridSizeY);
        chunkZ = Mth.positiveModulo(chunkZ, this.sectionGridSizeXZ);

        for (int chunkY = this.sectionGridSizeY - 1; chunkY >= minChunkIndex; chunkY--) {
            SectionRenderDispatcher.RenderSection rc = this.nodes[this.getSectionIndex(chunkX, chunkY, chunkZ)].getRenderSection();
            if (!rc.getSectionMesh().isEmpty()) {
                return chunkY;
            }
        }

        return -1;
    }

    public Iterator<SectionRenderDispatcher.RenderSection> getRenderSectionIterator() {
        return new MemberIterator<>(this.nodes, RotatingSectionStorage.Node::getRenderSection);
    }

    private record Node<T>(T value) {
        public SectionRenderDispatcher.RenderSection getRenderSection() {
            return this.value instanceof SectionRenderDispatcher.RenderSection ? (SectionRenderDispatcher.RenderSection)this.value : null;
        }
    }

    public interface Value {
        void setSectionNode(long var1);

        long getSectionNode();
    }

    public interface ValueCreator<T extends RotatingSectionStorage.Value> {
        T createValue(int var1, long var2);
    }
}
