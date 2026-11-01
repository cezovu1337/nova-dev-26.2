package dev.fesly.client.notification;


import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public enum NotificationType {
    INFO(Color.GREEN.darker()),
    WARNING(Color.YELLOW.darker()),
    ERROR(Color.RED.brighter());

    @Getter
    Color color;

    NotificationType(Color color) {
        this.color = color;
    }
}
