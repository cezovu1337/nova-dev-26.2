package dev.fesly.client.module.impl.misc;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.util.player.BlinkUtils;
import dev.fesly.impl.util.player.MoveUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import dev.fesly.impl.value.secondValues.StringSetting;
import net.minecraft.util.StringUtils;
@ModuleInfo(name = "AutoIntaveOsk", category = Category.MISC, description = "Автоматически называет интейва уебаном")
public class AutoIntaveOsk extends Module {
    private float delay = 3;
    private String field1 = "IntaveDev";
    private String field2 = "- уебок.";
    @Override
    public void init() {
        super.init();
        addSetting("field1", new StringSetting("Field1", this, this::getField1, this::setField1));
        addSetting("field2", new StringSetting("Field2", this, this::getField2, this::setField2));
        addSetting("delay", new NumberSetting("Ticks Delay", this, this::getDelay, this::setDelay, "%.1f", 1.0F, 69.0F));
    }
    @EventLink
    public final Listener<UpdateEvent> onCalcCoolDown = event -> {
        if (mc.player.ticksExisted % ((int) ((delay * 2) + (Math.random() * 5))) == 0) {
            if (Math.random() > 0.5) {
                mc.player.sendChatMessage((Math.random() * 63) + " " + field1 + " " + (Math.random() * 63) + "" + StringUtils.ticksToElapsedTime((int) (Math.random() * 5)));
            } else {
                mc.player.sendChatMessage((Math.random() * 63) + (Math.random() * 63) + " " + field2 + "" + StringUtils.ticksToElapsedTime((int) (Math.random() * 5)));
            }
        }
    };
    public static AutoIntaveOsk getInstance() {
        return nova.getModuleManager().get(AutoIntaveOsk.class);
    }
    @java.lang.SuppressWarnings("all")
    public float getDelay() {
        return this.delay;
    }
    @java.lang.SuppressWarnings("all")
    public void setDelay(final float delay) {
        this.delay = delay;
    }
    @java.lang.SuppressWarnings("all")
    public String getField1() {
        return this.field1;
    }
    @java.lang.SuppressWarnings("all")
    public void setField1(final String field1) {
        this.field1 = field1;
    }
    @java.lang.SuppressWarnings("all")
    public String getField2() {
        return this.field2;
    }
    @java.lang.SuppressWarnings("all")
    public void setField2(final String field2) {
        this.field2 = field2;
    }
}
