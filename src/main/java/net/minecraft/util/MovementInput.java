package net.minecraft.util;

public class MovementInput {
    public float moveForward;
    public float moveStrafe;
    public boolean forwardKeyDown;
    public boolean backwardKeyDown;
    public boolean backKeyDown;
    public boolean leftKeyDown;
    public boolean rightKeyDown;
    public boolean jump;
    public boolean sneak;
    public boolean sneaking;

    public boolean isMovingForward() {
        return this.moveForward > 1.0E-5F || this.forwardKeyDown;
    }
}
