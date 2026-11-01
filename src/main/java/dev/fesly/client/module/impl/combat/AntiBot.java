package dev.fesly.client.module.impl.combat;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.util.time.TimerUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.ArrayList;
import java.util.List;
@ModuleInfo(name = "AntiBot", description = "", category = Category.COMBAT)
public class AntiBot extends Module {
    public static List<Entity> isBot = new ArrayList<>();
    private TimerUtil timer = new TimerUtil();
    @Override
    public void init() {
        super.init();
    }
    @EventLink
    public final Listener<UpdateEvent> onCalcCoolDown = event -> {
        for (Player entity : mc.world.getPlayers()) {
            if (!entity.getUniqueID().equals(Player.getOfflineUUID(entity.getName().getString()))) {
                if (!isBot.contains(entity)) {
                    isBot.add(entity);
                }
            }
        }
    };
    @Override
    public void onDisable() {
        super.onDisable();
        isBot.clear();
    }
    public static boolean checkBot(LivingEntity entity) {
        if (entity instanceof Player) {
            return isBot.contains(entity);
        }
        return false;
    }
    public static AntiBot get() {
        return Nova.getInstance().getModuleManager().get(AntiBot.class);
    }
    @java.lang.SuppressWarnings("all")
    public TimerUtil getTimer() {
        return this.timer;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<UpdateEvent> getOnCalcCoolDown() {
        return this.onCalcCoolDown;
    }
    @java.lang.SuppressWarnings("all")
    public void setTimer(final TimerUtil timer) {
        this.timer = timer;
    }
}
