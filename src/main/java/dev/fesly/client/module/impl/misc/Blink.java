package dev.fesly.client.module.impl.misc;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.event.impl.render.DrawHudPostEvent;
import dev.fesly.impl.util.player.BlinkUtils;
import dev.fesly.impl.util.player.MoveUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
@ModuleInfo(name = "Blink", category = Category.MISC, description = "Не отправляет вашу позицию серверу \n пока включена функция")
public class Blink extends Module {
    public static boolean spawnGhost = true;
    @Override
    public void init() {
        super.init();
        addSetting("spawnGhost", new BooleanSetting("Spawn Ghost", this, Blink::isSpawnGhost, Blink::setSpawnGhost));
    }
    private BlinkUtils blinkUtil = new BlinkUtils();
    @Override
    protected void onEnable() {
        super.onEnable();
        Nova.getInstance().getEventBus().register(blinkUtil);
        blinkUtil.startBlinking();
    }
    @Override
    protected void onDisable() {
        super.onDisable();
        blinkUtil.stopBlinking();
        MoveUtil.setSpeed(0.1F);
        Nova.getInstance().getEventBus().unregister(blinkUtil);
    }
    public static Blink getInstance() {
        return nova.getModuleManager().get(Blink.class);
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isSpawnGhost() {
        return Blink.spawnGhost;
    }
    @java.lang.SuppressWarnings("all")
    public static void setSpawnGhost(final boolean spawnGhost) {
        Blink.spawnGhost = spawnGhost;
    }
}
