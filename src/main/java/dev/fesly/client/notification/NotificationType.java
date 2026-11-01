package dev.fesly.client.notification;
import lombok.Setter;
import java.awt.*;
public enum NotificationType {
    INFO(Color.GREEN.darker()), WARNING(Color.YELLOW.darker()), ERROR(Color.RED.brighter());
    Color color;
    NotificationType(Color color) {
        this.color = color;
    }
    @java.lang.SuppressWarnings("all")
    public Color getColor() {
        return this.color;
    }
}
