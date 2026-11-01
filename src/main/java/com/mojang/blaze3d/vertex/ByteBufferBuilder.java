package com.mojang.blaze3d.vertex;

import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.util.Mth;
import net.optifine.render.BufferBuilderCache;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryUtil.MemoryAllocator;
import org.slf4j.Logger;

public class ByteBufferBuilder implements AutoCloseable {
    private static final MemoryPool MEMORY_POOL = TracyClient.createMemoryPool("ByteBufferBuilder");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);
    private static final long DEFAULT_MAX_CAPACITY = 4294967295L;
    private static final int MAX_GROWTH_SIZE = 2097152;
    private static final int BUFFER_FREED_GENERATION = -1;
    private long pointer;
    private long capacity;
    private final long maxCapacity;
    private long writeOffset;
    private long nextResultOffset;
    private int resultCount;
    private int generation;
    private BufferBuilderCache bufferBuilderCache;
    private ByteBuffer byteBuffer;
    private IntBuffer intBuffer;
    private FloatBuffer floatBuffer;

    public ByteBufferBuilder(int initialCapacity, long maxCapacity) {
        this.capacity = initialCapacity;
        this.maxCapacity = maxCapacity;
        this.pointer = ALLOCATOR.malloc(initialCapacity);
        MEMORY_POOL.malloc(this.pointer, initialCapacity);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + initialCapacity + " bytes");
        }

        this.byteBuffer = MemoryUtil.memByteBuffer(this.pointer, (int)this.capacity);
        this.intBuffer = this.byteBuffer.asIntBuffer();
        this.floatBuffer = this.byteBuffer.asFloatBuffer();
    }

    public ByteBufferBuilder(int initialCapacity) {
        this(initialCapacity, 4294967295L);
    }

    public static ByteBufferBuilder exactlySized(int capacity) {
        return new ByteBufferBuilder(capacity, capacity);
    }

    public long reserve(int size) {
        long offset = this.writeOffset;
        long nextOffset = Math.addExact(offset, size);
        this.ensureCapacity(nextOffset);
        this.writeOffset = nextOffset;
        return Math.addExact(this.pointer, offset);
    }

    private void ensureCapacity(long requiredCapacity) {
        if (requiredCapacity > this.capacity) {
            if (requiredCapacity > this.maxCapacity) {
                throw new IllegalArgumentException("Maximum capacity of ByteBufferBuilder (" + this.maxCapacity + ") exceeded, required " + requiredCapacity);
            }

            long preferredGrowth = Math.min(this.capacity, 2097152L);
            long newCapacity = Mth.clamp(this.capacity + preferredGrowth, requiredCapacity, this.maxCapacity);
            this.resize(newCapacity);
        }
    }

    private void resize(long newCapacity) {
        MEMORY_POOL.free(this.pointer);
        this.pointer = ALLOCATOR.realloc(this.pointer, newCapacity);
        MEMORY_POOL.malloc(this.pointer, (int)Math.min(newCapacity, 2147483647L));
        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", this.capacity, newCapacity);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + this.capacity + " bytes to " + newCapacity + " bytes");
        }

        this.capacity = newCapacity;
        this.byteBuffer = MemoryUtil.memByteBuffer(this.pointer, (int)this.capacity);
        this.intBuffer = this.byteBuffer.asIntBuffer();
        this.floatBuffer = this.byteBuffer.asFloatBuffer();
    }

    public ByteBufferBuilder.Result build() {
        this.checkOpen();
        long offset = this.nextResultOffset;
        long size = this.writeOffset - offset;
        if (size == 0L) {
            return null;
        }

        if (size > 2147483647L) {
            throw new IllegalStateException("Cannot build buffer larger than 2147483647 bytes (was " + size + ")");
        }

        this.nextResultOffset = this.writeOffset;
        this.resultCount++;
        return new ByteBufferBuilder.Result(offset, (int)size, this.generation);
    }

    public void clear() {
        if (this.resultCount > 0) {
            LOGGER.warn("Clearing BufferBuilder with unused batches");
        }

        this.discard();
    }

    public void discard() {
        this.checkOpen();
        if (this.resultCount > 0) {
            this.discardResults();
            this.resultCount = 0;
        }
    }

    private boolean isValid(int generation) {
        return generation == this.generation;
    }

    private void freeResult() {
        if (--this.resultCount <= 0) {
            this.discardResults();
        }
    }

    private void discardResults() {
        long currentSize = this.writeOffset - this.nextResultOffset;
        if (currentSize > 0L) {
            MemoryUtil.memCopy(this.pointer + this.nextResultOffset, this.pointer, currentSize);
        }

        this.writeOffset = currentSize;
        this.nextResultOffset = 0L;
        this.generation++;
    }

    @Override
    public void close() {
        if (this.pointer != 0L) {
            MEMORY_POOL.free(this.pointer);
            ALLOCATOR.free(this.pointer);
            this.pointer = 0L;
            this.generation = -1;
            this.byteBuffer = null;
            this.intBuffer = null;
            this.floatBuffer = null;
        }
    }

    private void checkOpen() {
        if (this.pointer == 0L) {
            throw new IllegalStateException("Buffer has been freed");
        }
    }

    public int getCapacity() {
        return (int)this.capacity;
    }

    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }

    public IntBuffer getIntBuffer() {
        return this.intBuffer;
    }

    public FloatBuffer getFloatBuffer() {
        return this.floatBuffer;
    }

    public int getNextResultOffset() {
        return (int)this.nextResultOffset;
    }

    public int getWriteOffset() {
        return (int)this.writeOffset;
    }

    public BufferBuilderCache getBufferBuilderCache() {
        if (this.bufferBuilderCache == null) {
            this.bufferBuilderCache = new BufferBuilderCache();
        }

        return this.bufferBuilderCache;
    }

    @Override
    public String toString() {
        return "resultOffset: " + this.nextResultOffset + ", writeOffset: " + this.writeOffset + ", capacity: " + this.capacity;
    }

    public class Result implements AutoCloseable {
        private final long offset;
        private final int size;
        private final int generation;
        private boolean closed;

        private Result(long offset, int size, int generation) {
            this.offset = offset;
            this.size = size;
            this.generation = generation;
        }

        public ByteBuffer byteBuffer() {
            if (!ByteBufferBuilder.this.isValid(this.generation)) {
                throw new IllegalStateException("Buffer is no longer valid");
            } else {
                return MemoryUtil.memByteBuffer(ByteBufferBuilder.this.pointer + this.offset, this.size);
            }
        }

        public int size() {
            return this.size;
        }

        @Override
        public void close() {
            if (!this.closed) {
                this.closed = true;
                if (ByteBufferBuilder.this.isValid(this.generation)) {
                    ByteBufferBuilder.this.freeResult();
                }
            }
        }

        @Override
        public String toString() {
            return "offset: " + this.offset + ", size: " + this.size + ", generation: " + this.generation + ", closed: " + this.closed;
        }
    }
}
