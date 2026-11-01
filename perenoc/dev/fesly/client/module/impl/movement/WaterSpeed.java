package dev.fesly.client.module.impl.movement;

import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.util.player.MoveUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.potion.Effects;

@ModuleInfo(
        name = "WaterSpeed",
        description = "",
        category = Category.MOVEMENT
)
@Getter
@Setter
public class WaterSpeed extends Module {

    @EventLink()
    public final Listener<MotionEvent> onPlayerUpdate = event -> {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.getActivePotionEffect(Effects.DOLPHINS_GRACE) != null) {
            MoveUtil.setSpeed(0.99f);
        }

    };
}
