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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.math.MathHelper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class DraggableManager implements IAccess {
    @java.lang.SuppressWarnings("all")
    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(DraggableManager.class);
    private final List<Draggable> DRAGGABLES = ImmutableList.of(new WaterMark(), new StaffList(), new PotionHud(), new TargetHud(), new KeyBinds(), new FeatureList());
    public final Map<Class<? extends Draggable>, Draggable> CLASS_TO_DRAGGABLE = ImmutableMap.copyOf(DRAGGABLES.stream().collect(Collectors.toMap(Draggable::getClass, v -> v)));
    public static Draggable dragged;
    public static TimerUtil draggedTimer = new TimerUtil();
    private int dragRelativeX;
    private int dragRelativeY;
    public static boolean isChatOpen() {
        Minecraft mc = Minecraft.getInstance();
        net.minecraft.client.gui.screens.Screen screen = mc.gui != null ? mc.gui.screen() : null;
        return screen instanceof ChatScreen || mc.currentScreen instanceof ChatScreen;
    }
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
        double mouseX = isChatOpen() ? mc.mouseHandler.getScaledXPos(mc.getWindow()) : Double.NaN;
        double mouseY = isChatOpen() ? mc.mouseHandler.getScaledYPos(mc.getWindow()) : Double.NaN;
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
        double mouseX = isChatOpen() ? mc.mouseHandler.getScaledXPos(mc.getWindow()) : Double.NaN;
        double mouseY = isChatOpen() ? mc.mouseHandler.getScaledYPos(mc.getWindow()) : Double.NaN;
        for (Draggable draggable : DRAGGABLES) {
            if (!DraggableUtil.isValid(draggable)) continue;
            draggable.setX((int) MathHelper.clamp(draggable.getX(), 0, mw.getScaledWidth() - draggable.getWidth()));
            draggable.setY((int) MathHelper.clamp(draggable.getY(), 0, mw.getScaledHeight() - draggable.getHeight()));
            draggable.draw(mouseX, mouseY, e.getPartialTicks());
        }
        if (dragged != null) {
            if (Double.isFinite(mouseX) && Double.isFinite(mouseY)) {
                dragged.setX((int) (mouseX - dragRelativeX));
                dragged.setY((int) (mouseY - dragRelativeY));
            } else {
                dragged = null;
            }
        } else {
            draggedTimer.reset();
        }
        assert dragged != null;
        if (!DraggableUtil.isValid(dragged)) dragged = null;
    };
    @EventLink
    public Listener<MouseClickedEvent> onMousePress = evt -> {
        if (!isChatOpen()) return;
        Minecraft mc = Minecraft.getInstance();
        float mouseX = (float) mc.mouseHandler.getScaledXPos(mc.getWindow());
        float mouseY = (float) mc.mouseHandler.getScaledYPos(mc.getWindow());
        for (Draggable draggable : DRAGGABLES) {
            if (!DraggableUtil.isValid(draggable)) continue;
            if (MathUtil.isHovered(draggable.getX(), draggable.getY(), draggable.getWidth(), draggable.getHeight(), mouseX, mouseY)) {
                dragged = draggable;
                dragRelativeX = (int) (mouseX - draggable.getX());
                dragRelativeY = (int) (mouseY - draggable.getY());
                break;
            }
        }
    };
    @EventLink
    public Listener<MouseReleaseEvent> onMouseReleased = evt -> {
        dragged = null;
    };
}
