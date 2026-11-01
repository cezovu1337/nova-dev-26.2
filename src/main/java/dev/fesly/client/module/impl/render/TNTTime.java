package dev.fesly.client.module.impl.render;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.Priorities;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.render.DrawHudPreEvent;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import java.awt.*;
@ModuleInfo(
        name = "TNTTime",
        category = Category.RENDER,
        description = ""
)
public class TNTTime extends Module {
    @EventLink(Priorities.VERY_HIGH)
    public Listener<DrawHudPreEvent> onHudPre = evt -> {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof PrimedTnt tnt) {
                String time = MathUtil.round(tnt.getFuse() / 20.0F,1) + "s";
                double[] vec = RenderUtil.worldToScreen(tnt.getPosX(),tnt.getPosY() + tnt.getHeight() / 2f,tnt.getPosZ());
                if (vec == null) continue;
                Font font =  Fonts.ROBOTO_REGULAR.get(22);
                ScaleUtil.scaleStart((float) (vec[0]), (float) (vec[1]), (float) (1f -  MathUtil.clamp(0,1,mc.player.getDistance(tnt) / 20)));
                font.drawCenterX(time,vec[0],vec[1], Color.RED.getRGB());
                ScaleUtil.scaleEnd();
            }
        }
    };
}
