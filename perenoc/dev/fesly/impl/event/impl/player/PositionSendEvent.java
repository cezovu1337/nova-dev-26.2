package dev.fesly.impl.event.impl.player;

import dev.fesly.impl.event.CancellableEvent;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PositionSendEvent extends CancellableEvent {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;
    private boolean canceled;

    public PositionSendEvent(double x, double y, double z, float yaw, float pitch, Boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }
}
