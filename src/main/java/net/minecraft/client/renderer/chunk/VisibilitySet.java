package net.minecraft.client.renderer.chunk;

import java.util.Locale;
import java.util.Set;
import net.minecraft.core.Direction;

public class VisibilitySet {
    private static final int FACINGS = Direction.values().length;
    private long bits;

    public void add(Set<Direction> directions) {
        for (Direction direction1 : directions) {
            for (Direction direction2 : directions) {
                this.set(direction1, direction2, true);
            }
        }
    }

    public void set(Direction direction1, Direction direction2, boolean value) {
        this.setBit(direction1.ordinal() + direction2.ordinal() * FACINGS, value);
        this.setBit(direction2.ordinal() + direction1.ordinal() * FACINGS, value);
    }

    public void setAll(boolean visible) {
        if (visible) {
            this.bits = -1L;
        } else {
            this.bits = 0L;
        }
    }

    public boolean visibilityBetween(Direction direction1, Direction direction2) {
        return this.getBit(direction1.ordinal() + direction2.ordinal() * FACINGS);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(' ');

        for (Direction direction : Direction.values()) {
            builder.append(' ').append(direction.toString().toUpperCase(Locale.ROOT).charAt(0));
        }

        builder.append('\n');

        for (Direction direction1 : Direction.values()) {
            builder.append(direction1.toString().toUpperCase(Locale.ROOT).charAt(0));

            for (Direction direction2 : Direction.values()) {
                if (direction1 == direction2) {
                    builder.append("  ");
                } else {
                    boolean ok = this.visibilityBetween(direction1, direction2);
                    builder.append(' ').append((char)(ok ? 'Y' : 'n'));
                }
            }

            builder.append('\n');
        }

        return builder.toString();
    }

    private boolean getBit(int i) {
        return (this.bits & 1L << i) != 0L;
    }

    private void setBit(int i, boolean on) {
        if (on) {
            this.setBit(i);
        } else {
            this.clearBit(i);
        }
    }

    private void setBit(int i) {
        this.bits |= 1L << i;
    }

    private void clearBit(int i) {
        this.bits &= ~(1L << i);
    }
}
