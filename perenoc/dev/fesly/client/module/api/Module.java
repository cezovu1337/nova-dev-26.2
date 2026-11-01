package dev.fesly.client.module.api;

import com.google.gson.JsonObject;
import dev.fesly.Nova;
import dev.fesly.client.notification.Notification;
import dev.fesly.client.notification.NotificationManager;
import dev.fesly.client.notification.NotificationType;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.*;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.value.defaultValues.Value;
import dev.fesly.impl.value.defaultValues.impl.BooleanValue;
import dev.fesly.impl.value.defaultValues.impl.ModeValue;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.Setting;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Data
@Getter
@Setter
public abstract class Module implements IAccess {
    private final String name;
    public String prefix;
    private final List<Value<?>> values = new ArrayList<>();
    private Map<String, Setting> settings = new LinkedHashMap<>();
    private ModuleInfo moduleInfo;
    private boolean hidden;
    public boolean enabled;

    private boolean opened;
    private boolean binding;

    public final Animation deanimation = new DecelerateAnimation(120, 1);

    public final EaseBackIn animation = new EaseBackIn(200, 1,1.3f, Direction.BACKWARDS);
    public final ElasticAnimation alpha = new ElasticAnimation(550, 1, 14, 1, false, Direction.BACKWARDS);


    public final Animation openAnimation = new SmoothStepAnimation(220, 1, Direction.BACKWARDS);

    public final Animation inScrollBar = new DecelerateAnimation(520, 0.5f);

    public final Animation selectionAnim = new DecelerateAnimation(320, 1);

    public final Animation inListAnimation = new ElasticAnimation(320, 1, 7, 1, false, Direction.BACKWARDS);

    public final Animation keyAnimation = new DecelerateAnimation(120, 1);

    private int keyCode;

    public Module() {
        if (this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            this.moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);

            this.name = this.moduleInfo.name().replace(" ", "");
            this.keyCode = this.moduleInfo.keyBind();
            this.hidden = moduleInfo.hidden();

            addSetting(name.toLowerCase(), new BooleanSetting("Visible",this, this::isVisible, this::setVisible));
            init();
        } else {
            throw new RuntimeException("@ModuleInfo annotation not found on " + this.getClass().getSimpleName());
        }
    }
    public void init() {
    }

    private boolean visible = true;

    public Module(final ModuleInfo info) {
        this.moduleInfo = info;

        this.name = String.join(" ", StringUtils.splitByCharacterTypeCamelCase(this.moduleInfo.name()));

        this.keyCode = this.moduleInfo.keyBind();
    }

    public final void addSetting(String id, Setting setting) {
        settings.put(id, setting);
    }

    public void toggle() {
        this.setEnabled(!enabled);
        CompletableFuture.runAsync(() -> Nova.getInstance().getConfigManager().set("default"));
    }

    public void setEnabled(final boolean enabled) {
        if (this.enabled == enabled || (!this.moduleInfo.allowDisable() && !enabled)) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            superEnable();
//            if (mc.player != null) SoundUtil.playCustomSound("enable.wav", 0.1F);
        } else {
            superDisable();
//            if (mc.player != null) SoundUtil.playCustomSound("disable.wav", 0.1F);
        }
    }

    /**
     * Called when a module gets enabled
     * -> important: whenever you override this method in a subclass
     * keep the super.onEnable()
     */
    public final void superEnable() {
        Nova.getInstance().getEventBus().register(this);
        this.values.stream()
                .filter(value -> value instanceof ModeValue)
                .forEach(value -> ((ModeValue) value).getValue().register());

        this.values.stream()
                .filter(value -> value instanceof BooleanValue)
                .forEach(value -> {
                    final BooleanValue booleanValue = (BooleanValue) value;
                    if (booleanValue.getMode() != null && booleanValue.getValue()) {
                        booleanValue.getMode().register();
                    }
                });
        String text = " was enabled";
        if (name.equalsIgnoreCase("ClickGui" )) text = " was opened!";
        ClientUtil.startPlaySound(new ResourceLocation("nova","sound/enable.wav"),0.7f);
        NotificationManager.sendNotification(new Notification(name + text, NotificationType.INFO));
        if (mc.player != null) this.onEnable();
    }

    public final void load(JsonObject json) {
        setKeyCode(json.get("key").getAsInt());
        setEnabled(json.get("enabled").getAsBoolean());
        for (Map.Entry<String, Setting> en : settings.entrySet()) {
            String id = en.getKey();
            if (!json.has(id)) continue;
            en.getValue().load(json.get(id));
        }
    }

    public final JsonObject save() {
        JsonObject json = new JsonObject();
        json.addProperty("key", getKeyCode());
        json.addProperty("enabled", isEnabled());
        for (Map.Entry<String, Setting> en : settings.entrySet()) {
            json.add(en.getKey(), en.getValue().save());
        }
        return json;
    }

    @Setter
    public int y;
    private int loc;
    private int heightLoc;
    @Setter
    public int height;
    private int xLoc;
    @Setter
    public int x;

    public int getY() {
        this.loc = (int) MathUtil.animation(this.loc, this.y, /*Nova.clickgui.dragged != null ?  1f :.3f*/ 6f);
        return loc;
    }

    public int getHeight() {
        this.heightLoc = (int) MathUtil.animation(this.heightLoc, height, 1f);
        return heightLoc;
    }

    public int getX() {
        this.xLoc = (int) MathUtil.animation(this.xLoc, x, 50f);
        return xLoc;
    }

    /**
     * Called when a module gets disabled
     * -> important: whenever you override this method in a subclass
     * keep the super.onDisable()
     */
    public final void superDisable() {
        Nova.getInstance().getEventBus().unregister(this);
        this.values.stream()
                .filter(value -> value instanceof ModeValue)
                .forEach(value -> ((ModeValue) value).getValue().unregister());

        this.values.stream()
                .filter(value -> value instanceof BooleanValue)
                .forEach(value -> {
                    final BooleanValue booleanValue = (BooleanValue) value;
                    if (booleanValue.getMode() != null) {
                        booleanValue.getMode().unregister();
                    }
                });
        if (!name.equalsIgnoreCase("ClickGui")) {
            ClientUtil.startPlaySound(new ResourceLocation("nova","sound/disable.wav"),0.7f);
            NotificationManager.sendNotification(new Notification(name + " was disabled", NotificationType.INFO));
        }
        if (mc.player != null) this.onDisable();
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public List<Value<?>> getAllValues() {
        ArrayList<Value<?>> allValues = new ArrayList<>();

        values.forEach(value -> {
            List<Value<?>> subValues = value.getSubValues();

            allValues.add(value);

            if (subValues != null) {
                allValues.addAll(subValues);
            }
        });

        return allValues;
    }

}