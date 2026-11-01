package dev.fesly.client.notification;

import dev.fesly.Nova;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.other.TickEvent;
import dev.fesly.impl.event.impl.render.DrawHudPreEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager implements IAccess {
    public static List<Notification> notifications = new ArrayList<>();

    public static void sendNotification(Notification notification) {
       notifications.add(notification);
    }

    @EventLink
    public Listener<DrawHudPreEvent> drawHud = event -> {
      drawMethod();
    };
    public void drawMethod() {
        if (notifications.isEmpty() || !nova.getClientUI().isNotifications()) return;
        notifications.forEach(notification ->
                notification.render(
                        (float) mc.mouseHelper.getMouseX(),
                        (float) mc.mouseHelper.getMouseY(),
                        mc.getRenderPartialTicks()));
    }

    @EventLink
    public Listener<TickEvent> tickEvent = event -> {
        notifications.removeIf(Notification::check);
    };

    public void register() {
        Nova.getInstance().getEventBus().register(this);
    }
}
