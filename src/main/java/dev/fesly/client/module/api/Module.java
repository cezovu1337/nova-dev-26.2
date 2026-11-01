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
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    public final EaseBackIn animation = new EaseBackIn(200, 1, 1.3F, Direction.BACKWARDS);
    public final ElasticAnimation alpha = new ElasticAnimation(550, 1, 14, 1, false, Direction.BACKWARDS);
    public final Animation openAnimation = new SmoothStepAnimation(220, 1, Direction.BACKWARDS);
    public final Animation inScrollBar = new DecelerateAnimation(520, 0.5F);
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
            addSetting(name.toLowerCase(), new BooleanSetting("Visible", this, this::isVisible, this::setVisible));
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
        } else
        {
            superDisable();
        }
    }
    public final void superEnable() {
        Nova.getInstance().getEventBus().register(this);
        this.values.stream().filter(value -> value instanceof ModeValue).forEach(value -> ((ModeValue) value).getValue().register());
        this.values.stream().filter(value -> value instanceof BooleanValue).forEach(value -> {
            final BooleanValue booleanValue = (BooleanValue) value;
            if (booleanValue.getMode() != null && booleanValue.getValue()) {
                booleanValue.getMode().register();
            }
        });
        String text = " was enabled";
        if (name.equalsIgnoreCase("ClickGui")) text = " was opened!";
        ClientUtil.startPlaySound(new ResourceLocation("nova", "sound/enable.wav"), 0.7F);
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
    public int y;
    private int loc;
    private int heightLoc;
    public int height;
    private int xLoc;
    public int x;
    public int getY() {
        this.loc = (int) MathUtil.animation(this.loc, this.y,   6.0F);
        return loc;
    }
    public int getHeight() {
        this.heightLoc = (int) MathUtil.animation(this.heightLoc, height, 1.0F);
        return heightLoc;
    }
    public int getX() {
        this.xLoc = (int) MathUtil.animation(this.xLoc, x, 50.0F);
        return xLoc;
    }
    public final void superDisable() {
        Nova.getInstance().getEventBus().unregister(this);
        this.values.stream().filter(value -> value instanceof ModeValue).forEach(value -> ((ModeValue) value).getValue().unregister());
        this.values.stream().filter(value -> value instanceof BooleanValue).forEach(value -> {
            final BooleanValue booleanValue = (BooleanValue) value;
            if (booleanValue.getMode() != null) {
                booleanValue.getMode().unregister();
            }
        });
        if (!name.equalsIgnoreCase("ClickGui")) {
            ClientUtil.startPlaySound(new ResourceLocation("nova", "sound/disable.wav"), 0.7F);
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
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Module)) return false;
        final Module other = (Module) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.isHidden() != other.isHidden()) return false;
        if (this.isEnabled() != other.isEnabled()) return false;
        if (this.isOpened() != other.isOpened()) return false;
        if (this.isBinding() != other.isBinding()) return false;
        if (this.getKeyCode() != other.getKeyCode()) return false;
        if (this.isVisible() != other.isVisible()) return false;
        if (this.getY() != other.getY()) return false;
        if (this.getLoc() != other.getLoc()) return false;
        if (this.getHeightLoc() != other.getHeightLoc()) return false;
        if (this.getHeight() != other.getHeight()) return false;
        if (this.getXLoc() != other.getXLoc()) return false;
        if (this.getX() != other.getX()) return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$prefix = this.getPrefix();
        final java.lang.Object other$prefix = other.getPrefix();
        if (this$prefix == null ? other$prefix != null : !this$prefix.equals(other$prefix)) return false;
        final java.lang.Object this$values = this.getValues();
        final java.lang.Object other$values = other.getValues();
        if (this$values == null ? other$values != null : !this$values.equals(other$values)) return false;
        final java.lang.Object this$settings = this.getSettings();
        final java.lang.Object other$settings = other.getSettings();
        if (this$settings == null ? other$settings != null : !this$settings.equals(other$settings)) return false;
        final java.lang.Object this$moduleInfo = this.getModuleInfo();
        final java.lang.Object other$moduleInfo = other.getModuleInfo();
        if (this$moduleInfo == null ? other$moduleInfo != null : !this$moduleInfo.equals(other$moduleInfo)) return false;
        final java.lang.Object this$deanimation = this.getDeanimation();
        final java.lang.Object other$deanimation = other.getDeanimation();
        if (this$deanimation == null ? other$deanimation != null : !this$deanimation.equals(other$deanimation)) return false;
        final java.lang.Object this$animation = this.getAnimation();
        final java.lang.Object other$animation = other.getAnimation();
        if (this$animation == null ? other$animation != null : !this$animation.equals(other$animation)) return false;
        final java.lang.Object this$alpha = this.getAlpha();
        final java.lang.Object other$alpha = other.getAlpha();
        if (this$alpha == null ? other$alpha != null : !this$alpha.equals(other$alpha)) return false;
        final java.lang.Object this$openAnimation = this.getOpenAnimation();
        final java.lang.Object other$openAnimation = other.getOpenAnimation();
        if (this$openAnimation == null ? other$openAnimation != null : !this$openAnimation.equals(other$openAnimation)) return false;
        final java.lang.Object this$inScrollBar = this.getInScrollBar();
        final java.lang.Object other$inScrollBar = other.getInScrollBar();
        if (this$inScrollBar == null ? other$inScrollBar != null : !this$inScrollBar.equals(other$inScrollBar)) return false;
        final java.lang.Object this$selectionAnim = this.getSelectionAnim();
        final java.lang.Object other$selectionAnim = other.getSelectionAnim();
        if (this$selectionAnim == null ? other$selectionAnim != null : !this$selectionAnim.equals(other$selectionAnim)) return false;
        final java.lang.Object this$inListAnimation = this.getInListAnimation();
        final java.lang.Object other$inListAnimation = other.getInListAnimation();
        if (this$inListAnimation == null ? other$inListAnimation != null : !this$inListAnimation.equals(other$inListAnimation)) return false;
        final java.lang.Object this$keyAnimation = this.getKeyAnimation();
        final java.lang.Object other$keyAnimation = other.getKeyAnimation();
        if (this$keyAnimation == null ? other$keyAnimation != null : !this$keyAnimation.equals(other$keyAnimation)) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof Module;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isHidden() ? 79 : 97);
        result = result * PRIME + (this.isEnabled() ? 79 : 97);
        result = result * PRIME + (this.isOpened() ? 79 : 97);
        result = result * PRIME + (this.isBinding() ? 79 : 97);
        result = result * PRIME + this.getKeyCode();
        result = result * PRIME + (this.isVisible() ? 79 : 97);
        result = result * PRIME + this.getY();
        result = result * PRIME + this.getLoc();
        result = result * PRIME + this.getHeightLoc();
        result = result * PRIME + this.getHeight();
        result = result * PRIME + this.getXLoc();
        result = result * PRIME + this.getX();
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $prefix = this.getPrefix();
        result = result * PRIME + ($prefix == null ? 43 : $prefix.hashCode());
        final java.lang.Object $values = this.getValues();
        result = result * PRIME + ($values == null ? 43 : $values.hashCode());
        final java.lang.Object $settings = this.getSettings();
        result = result * PRIME + ($settings == null ? 43 : $settings.hashCode());
        final java.lang.Object $moduleInfo = this.getModuleInfo();
        result = result * PRIME + ($moduleInfo == null ? 43 : $moduleInfo.hashCode());
        final java.lang.Object $deanimation = this.getDeanimation();
        result = result * PRIME + ($deanimation == null ? 43 : $deanimation.hashCode());
        final java.lang.Object $animation = this.getAnimation();
        result = result * PRIME + ($animation == null ? 43 : $animation.hashCode());
        final java.lang.Object $alpha = this.getAlpha();
        result = result * PRIME + ($alpha == null ? 43 : $alpha.hashCode());
        final java.lang.Object $openAnimation = this.getOpenAnimation();
        result = result * PRIME + ($openAnimation == null ? 43 : $openAnimation.hashCode());
        final java.lang.Object $inScrollBar = this.getInScrollBar();
        result = result * PRIME + ($inScrollBar == null ? 43 : $inScrollBar.hashCode());
        final java.lang.Object $selectionAnim = this.getSelectionAnim();
        result = result * PRIME + ($selectionAnim == null ? 43 : $selectionAnim.hashCode());
        final java.lang.Object $inListAnimation = this.getInListAnimation();
        result = result * PRIME + ($inListAnimation == null ? 43 : $inListAnimation.hashCode());
        final java.lang.Object $keyAnimation = this.getKeyAnimation();
        result = result * PRIME + ($keyAnimation == null ? 43 : $keyAnimation.hashCode());
        return result;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "Module(name=" + this.getName() + ", prefix=" + this.getPrefix() + ", values=" + this.getValues() + ", settings=" + this.getSettings() + ", moduleInfo=" + this.getModuleInfo() + ", hidden=" + this.isHidden() + ", enabled=" + this.isEnabled() + ", opened=" + this.isOpened() + ", binding=" + this.isBinding() + ", deanimation=" + this.getDeanimation() + ", animation=" + this.getAnimation() + ", alpha=" + this.getAlpha() + ", openAnimation=" + this.getOpenAnimation() + ", inScrollBar=" + this.getInScrollBar() + ", selectionAnim=" + this.getSelectionAnim() + ", inListAnimation=" + this.getInListAnimation() + ", keyAnimation=" + this.getKeyAnimation() + ", keyCode=" + this.getKeyCode() + ", visible=" + this.isVisible() + ", y=" + this.getY() + ", loc=" + this.getLoc() + ", heightLoc=" + this.getHeightLoc() + ", height=" + this.getHeight() + ", xLoc=" + this.getXLoc() + ", x=" + this.getX() + ")";
    }
    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }
    @java.lang.SuppressWarnings("all")
    public String getPrefix() {
        return this.prefix;
    }
    @java.lang.SuppressWarnings("all")
    public List<Value<?>> getValues() {
        return this.values;
    }
    @java.lang.SuppressWarnings("all")
    public Map<String, Setting> getSettings() {
        return this.settings;
    }
    @java.lang.SuppressWarnings("all")
    public ModuleInfo getModuleInfo() {
        return this.moduleInfo;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isHidden() {
        return this.hidden;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isEnabled() {
        return this.enabled;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isOpened() {
        return this.opened;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isBinding() {
        return this.binding;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getDeanimation() {
        return this.deanimation;
    }
    @java.lang.SuppressWarnings("all")
    public EaseBackIn getAnimation() {
        return this.animation;
    }
    @java.lang.SuppressWarnings("all")
    public ElasticAnimation getAlpha() {
        return this.alpha;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getOpenAnimation() {
        return this.openAnimation;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getInScrollBar() {
        return this.inScrollBar;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getSelectionAnim() {
        return this.selectionAnim;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getInListAnimation() {
        return this.inListAnimation;
    }
    @java.lang.SuppressWarnings("all")
    public Animation getKeyAnimation() {
        return this.keyAnimation;
    }
    @java.lang.SuppressWarnings("all")
    public int getKeyCode() {
        return this.keyCode;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isVisible() {
        return this.visible;
    }
    @java.lang.SuppressWarnings("all")
    public int getLoc() {
        return this.loc;
    }
    @java.lang.SuppressWarnings("all")
    public int getHeightLoc() {
        return this.heightLoc;
    }
    @java.lang.SuppressWarnings("all")
    public int getXLoc() {
        return this.xLoc;
    }
    @java.lang.SuppressWarnings("all")
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }
    @java.lang.SuppressWarnings("all")
    public void setSettings(final Map<String, Setting> settings) {
        this.settings = settings;
    }
    @java.lang.SuppressWarnings("all")
    public void setModuleInfo(final ModuleInfo moduleInfo) {
        this.moduleInfo = moduleInfo;
    }
    @java.lang.SuppressWarnings("all")
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }
    @java.lang.SuppressWarnings("all")
    public void setOpened(final boolean opened) {
        this.opened = opened;
    }
    @java.lang.SuppressWarnings("all")
    public void setBinding(final boolean binding) {
        this.binding = binding;
    }
    @java.lang.SuppressWarnings("all")
    public void setKeyCode(final int keyCode) {
        this.keyCode = keyCode;
    }
    @java.lang.SuppressWarnings("all")
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
    @java.lang.SuppressWarnings("all")
    public void setLoc(final int loc) {
        this.loc = loc;
    }
    @java.lang.SuppressWarnings("all")
    public void setHeightLoc(final int heightLoc) {
        this.heightLoc = heightLoc;
    }
    @java.lang.SuppressWarnings("all")
    public void setXLoc(final int xLoc) {
        this.xLoc = xLoc;
    }
    @java.lang.SuppressWarnings("all")
    public void setY(final int y) {
        this.y = y;
    }
    @java.lang.SuppressWarnings("all")
    public void setHeight(final int height) {
        this.height = height;
    }
    @java.lang.SuppressWarnings("all")
    public void setX(final int x) {
        this.x = x;
    }
}
