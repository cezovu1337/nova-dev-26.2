package net.minecraft.client.renderer;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.slf4j.Logger;

public class SectionBufferBuilderPool implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Queue<SectionBufferBuilderPack> freeBuffers = Queues.newConcurrentLinkedQueue();

    private SectionBufferBuilderPool(List<SectionBufferBuilderPack> buffers) {
        this.freeBuffers.addAll(buffers);
    }

    public static SectionBufferBuilderPool allocate(int maxWorkers) {
        int maxBuffers = Math.max(1, (int)(Runtime.getRuntime().maxMemory() * 0.3) / SectionBufferBuilderPack.TOTAL_BUFFERS_SIZE);
        int targetBufferCount = Math.max(1, Math.min(maxWorkers, maxBuffers));
        List<SectionBufferBuilderPack> buffers = new ArrayList<>(targetBufferCount);

        try {
            for (int i = 0; i < targetBufferCount; i++) {
                buffers.add(new SectionBufferBuilderPack());
            }
        } catch (OutOfMemoryError e) {
            LOGGER.warn("Allocated only {}/{} buffers", buffers.size(), targetBufferCount);
            int buffersToDrop = Math.min(buffers.size() * 2 / 3, buffers.size() - 1);

            for (int i = 0; i < buffersToDrop; i++) {
                buffers.remove(buffers.size() - 1).close();
            }
        }

        return new SectionBufferBuilderPool(buffers);
    }

    public SectionBufferBuilderPack acquire() {
        return this.freeBuffers.poll();
    }

    public void release(SectionBufferBuilderPack buffer) {
        this.freeBuffers.offer(buffer);
    }

    public boolean isEmpty() {
        return this.freeBuffers.isEmpty();
    }

    public int getFreeBufferCount() {
        return this.freeBuffers.size();
    }

    @Override
    public void close() {
        this.freeBuffers.forEach(SectionBufferBuilderPack::close);
        this.freeBuffers.clear();
    }
}
