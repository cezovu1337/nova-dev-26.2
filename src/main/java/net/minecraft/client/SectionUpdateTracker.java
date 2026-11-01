package net.minecraft.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.optifine.Config;

public class SectionUpdateTracker {
    private final RotatingSectionStorage<SectionUpdateTracker.SectionDirtyState> storage;

    public SectionUpdateTracker(LevelHeightAccessor levelHeightAccessor, int renderDistance) {
        this.storage = new RotatingSectionStorage<>(
            renderDistance,
            levelHeightAccessor.getMinSectionY(),
            levelHeightAccessor.getMaxSectionY(),
            (index, sectionNode) -> new SectionUpdateTracker.SectionDirtyState(true, false, sectionNode)
        );
    }

    public void setDirty(int sectionX, int sectionY, int sectionZ, boolean playerChanged) {
        SectionUpdateTracker.SectionDirtyState section = this.storage.getValue(sectionX, sectionY, sectionZ);
        if (section != null) {
            section.setDirty(playerChanged);
        }
    }

    public void repositionCamera(SectionPos cameraSectionPos) {
        this.storage.repositionCenter(cameraSectionPos);
    }

    public int size() {
        return this.storage.size();
    }

    public SectionUpdateTracker.SectionDirtyState getDirtyState(long sectionNode) {
        return this.storage.getValue(sectionNode);
    }

    public boolean hasAllNeighbors(ClientLevel level, long sectionNode) {
        return this.doesChunkExistAt(level, sectionNode);
    }

    private boolean doesChunkExistAt(ClientLevel level, long sectionNode) {
        ChunkAccess chunk = level.getChunk(SectionPos.x(sectionNode), SectionPos.z(sectionNode), ChunkStatus.FULL, false);
        return chunk != null && level.getLightEngine().lightOnInColumn(SectionPos.getZeroNode(sectionNode));
    }

    public static class SectionDirtyState implements RotatingSectionStorage.Value {
        private boolean isDirty;
        private boolean isDirtyFromPlayer;
        private long sectionNode;
        private boolean playerUpdate = false;

        private SectionDirtyState(boolean isDirty, boolean isDirtyFromPlayer, long sectionNode) {
            this.isDirty = isDirty;
            this.isDirtyFromPlayer = isDirtyFromPlayer;
        }

        public void setDirty(boolean fromPlayer) {
            boolean wasDirty = this.isDirty;
            this.isDirty = true;
            this.isDirtyFromPlayer = fromPlayer | (wasDirty && this.isDirtyFromPlayer);
            if (this.isWorldPlayerUpdate()) {
                this.playerUpdate = true;
            }
        }

        public void setNotDirty() {
            this.isDirty = false;
            this.isDirtyFromPlayer = false;
            this.playerUpdate = false;
        }

        @Override
        public void setSectionNode(long sectionNode) {
            if (this.sectionNode != sectionNode) {
                this.sectionNode = sectionNode;
                this.isDirty = true;
                this.isDirtyFromPlayer = false;
            }
        }

        @Override
        public long getSectionNode() {
            return this.sectionNode;
        }

        public boolean isDirty() {
            return this.isDirty;
        }

        public boolean isDirtyFromPlayer() {
            return this.isDirtyFromPlayer;
        }

        private boolean isWorldPlayerUpdate() {
            ClientLevel worldClient = Config.getMinecraft().level;
            return worldClient == null ? false : worldClient.isPlayerUpdate();
        }
    }
}
