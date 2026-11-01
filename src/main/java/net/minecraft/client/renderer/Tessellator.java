package net.minecraft.client.renderer;

public class Tessellator {
    private final BufferBuilder buffer;
    private static final Tessellator INSTANCE = new Tessellator();

    public static Tessellator getInstance() {
        return INSTANCE;
    }

    public Tessellator(int size) {
        this.buffer = new BufferBuilder(size);
    }

    public Tessellator() {
        this(2097152);
    }

    public BufferBuilder getBuffer() {
        return this.buffer;
    }

    public void draw() {
        this.buffer.finishDrawing();
    }
}
