package dev.fesly.impl.event.impl.input;

import dev.fesly.impl.event.CancellableEvent;

public class ClientMovementInput extends CancellableEvent {

    public double moveForward, moveStrafe;

    public ClientMovementInput(double moveForward, double moveStrafe) {
        this.moveForward = moveForward;
        this.moveStrafe = moveStrafe;
    }

    public double getMoveForward() {
        return moveForward;
    }

    public void setMoveForward(double moveForward) {
        this.moveForward = moveForward;
    }

    public double getMoveStrafe() {
        return moveStrafe;
    }

    public void setMoveStrafe(double moveStrafe) {
        this.moveStrafe = moveStrafe;
    }
}
