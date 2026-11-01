package net.minecraft.util;

public enum Direction {
    DOWN,
    UP,
    NORTH,
    SOUTH,
    WEST,
    EAST;

    public int getXOffset() {
        return switch (this) {
            case WEST -> -1;
            case EAST -> 1;
            default -> 0;
        };
    }

    public int getYOffset() {
        return switch (this) {
            case DOWN -> -1;
            case UP -> 1;
            default -> 0;
        };
    }

    public int getZOffset() {
        return switch (this) {
            case NORTH -> -1;
            case SOUTH -> 1;
            default -> 0;
        };
    }

    public enum Axis {
        X {
            @Override
            public double getCoordinate(double x, double y, double z) {
                return x;
            }
        },
        Y {
            @Override
            public double getCoordinate(double x, double y, double z) {
                return y;
            }
        },
        Z {
            @Override
            public double getCoordinate(double x, double y, double z) {
                return z;
            }
        };

        public abstract double getCoordinate(double x, double y, double z);

        public int getCoordinate(int x, int y, int z) {
            return (int) getCoordinate((double) x, (double) y, (double) z);
        }
    }

    public net.minecraft.core.Direction toCore() {
        return net.minecraft.core.Direction.byName(this.name().toLowerCase());
    }
}
