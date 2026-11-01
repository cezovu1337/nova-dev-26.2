package dev.fesly.client.module.impl.combat;

import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.EventCalculateCooldown;
import dev.fesly.impl.event.impl.input.EventUseItem;
import dev.fesly.impl.event.impl.input.FinishEatEvent;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.util.time.TimerUtil;
import dev.fesly.impl.value.secondValues.NumberSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Food;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;

@ModuleInfo(
        name = "GAppleTimer",
        description = "",
        category = Category.COMBAT
)
@Getter
@Setter
public class GAppleTimer extends Module {

    public float coolDownTime = 3.5f;
    public long lastGappleTime;

    private TimerUtil timer = new TimerUtil();

    @Override
    public void init() {
        super.init();
        addSetting("coolDownTime", new NumberSetting("CoolDown", this, this::getCoolDownTime, this::setCoolDownTime, "%.2f", 3.5F, 6F));
    }

    @EventLink()
    public final Listener<EventUseItem> onUse = event -> {
        if (isEnabled() && mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null) {

            long time = System.currentTimeMillis() - lastGappleTime;
            if (time < coolDownTime * 1000) {
                event.setCancelled(true);
            }
        }
    };

    @EventLink()
    public final Listener<EventCalculateCooldown> onCalcCoolDown = event -> {
        {
            if (event.getStack() == Items.GOLDEN_APPLE) {
                long time = System.currentTimeMillis() - lastGappleTime;
                if (time < coolDownTime * 1000) {
                    ((EventCalculateCooldown) event).setCooldown(time / (coolDownTime * 1000));
                }
            }
        }
    };

    @EventLink()
    public final Listener<FinishEatEvent> onEatItem = event -> {
        if(event.getItem().getItem() == Items.GOLDEN_APPLE) {
            lastGappleTime = System.currentTimeMillis();
        }
    };



    public static GAppleTimer get() {
        return Nova.getInstance().getModuleManager().get(GAppleTimer.class);
    }
}
