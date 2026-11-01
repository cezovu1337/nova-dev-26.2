package dev.fesly.client.component.draggable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import dev.fesly.Nova;
import dev.fesly.client.component.draggable.impl.*;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.MouseClickedEvent;
import dev.fesly.impl.event.impl.input.MouseReleaseEvent;
import dev.fesly.impl.event.impl.render.DrawHudPostEvent;
import dev.fesly.impl.event.impl.render.DrawHudPreEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.time.TimerUtil;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class DraggableManager implements IAccess {
    private final List<Draggable> DRAGGABLES = ImmutableList.of(new WaterMark(),new StaffList(),new PotionHud(), new TargetHud(),new KeyBinds(),new FeatureList());
    public final Map<Class<? extends Draggable>, Draggable> CLASS_TO_DRAGGABLE = ImmutableMap.copyOf(DRAGGABLES.stream().collect(Collectors.toMap(Draggable::getClass, v -> v)));
    public static Draggable dragged;

    public static TimerUtil draggedTimer = new TimerUtil();
    private int dragRelativeX;
    private int dragRelativeY;

    public <T extends Draggable> T getDraggable(Class<? extends T> clazz) {
        return (T) CLASS_TO_DRAGGABLE.get(clazz);
    }

    public void register() {
        Nova.getInstance().getEventBus().register(this);
        for (Draggable draggable : DRAGGABLES) Nova.getInstance().getEventBus().register(draggable);
    }

    public void load(JsonObject json) {
        for (Draggable draggable : DRAGGABLES) {
            try {
                if (!json.has(draggable.getId())) continue;
                JsonObject entry = json.getAsJsonObject(draggable.getId());
                draggable.setX(entry.get("x").getAsInt());
                draggable.setY(entry.get("y").getAsInt());
            } catch (Exception ex) {
                log.error("Unable to load position of draggable " + draggable, ex);
            }
        }
    }

    public JsonObject save() {
        JsonObject json = new JsonObject();
        for (Draggable draggable : DRAGGABLES) {
            try {
                JsonObject entry = new JsonObject();
                entry.addProperty("x", draggable.getX());
                entry.addProperty("y", draggable.getY());
                json.add(draggable.getId(), entry);
            } catch (Exception ex) {
                log.error("Unable to save position of draggable " + draggable, ex);
            }
        }
        return json;
    }

    @EventLink
    public Listener<DrawHudPreEvent> onDrawHudPre = e -> {
        Minecraft mc = Minecraft.getInstance();
        double mouseX = mc.currentScreen instanceof ChatScreen ? mc.mouseHelper.getMouseX() / (double) mw.getScaleFactor() : Double.NaN;
        double mouseY = mc.currentScreen instanceof ChatScreen ? mc.mouseHelper.getMouseY() / (double) mw.getScaleFactor() : Double.NaN;
        for (Draggable draggable : DRAGGABLES) {
            if (!DraggableUtil.isValid(draggable)) continue;
            draggable.setX((int) MathHelper.clamp(draggable.getX(), 0, mw.getScaledWidth() - draggable.getWidth()));
            draggable.setY((int) MathHelper.clamp(draggable.getY(), 0, mw.getScaledHeight() - draggable.getHeight()));
            draggable.drawPre(mouseX, mouseY, e.getPartialTicks());
        }
    };
    @EventLink
    public Listener<DrawHudPostEvent> onDrawHudPost = e -> {
        Minecraft mc = Minecraft.getInstance();
        double mouseX = mc.currentScreen instanceof ChatScreen ? mc.mouseHelper.getMouseX() / (double) mw.getScaleFactor() : Double.NaN;
        double mouseY = mc.currentScreen instanceof ChatScreen ? mc.mouseHelper.getMouseY() / (double) mw.getScaleFactor() : Double.NaN;
        for (Draggable draggable : DRAGGABLES) {
            if (!DraggableUtil.isValid(draggable)) continue;
            draggable.setX((int) MathHelper.clamp(draggable.getX(), 0, mw.getScaledWidth() - draggable.getWidth()));
            draggable.setY((int) MathHelper.clamp(draggable.getY(), 0, mw.getScaledHeight() - draggable.getHeight()));
            draggable.draw(mouseX, mouseY, e.getPartialTicks());
        }


        if (dragged != null) {

            if (Double.isFinite(mouseX) && Double.isFinite(mouseY)) {
                dragged.setX((int) (mouseX - dragRelativeX));
                dragged.setY((int) (mouseY - dragRelativeY ));
            } else{
                dragged = null;
            }
        }else {
            draggedTimer.reset();
        }


        assert dragged != null;
        if (!DraggableUtil.isValid(dragged)) dragged = null;
    };

    @EventLink
    public Listener<MouseClickedEvent> onMousePress = evt -> {
        float mouseX = (float) (mc.mouseHelper.getMouseX() / mw.getScaleFactor());
        float mouseY = (float) (mc.mouseHelper.getMouseY() / mw.getScaleFactor());

        float mY2 = (float) ((mw.getHeight() - mc.mouseHelper.getMouseY()) / mw.getScaleFactor());


        for (Draggable draggable : DRAGGABLES) {
            if (!DraggableUtil.isValid(draggable)) continue;
            if (Minecraft.getInstance().currentScreen instanceof ChatScreen && MathUtil.isHovered(draggable.getX(),
                    draggable.getY(),
                    draggable.getWidth(),
                    draggable.getHeight(), mouseX,mouseY)) {
                dragged = draggable;
                dragRelativeX = (int) (mouseX - draggable.getX());
                dragRelativeY = (int) ((mouseY ) - draggable.getY());
                break;
            }
        }
    };

    @EventLink
    public Listener<MouseReleaseEvent> onMouseReleased = evt -> {
        dragged = null;
    };
}
