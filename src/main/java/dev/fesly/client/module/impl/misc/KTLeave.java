package dev.fesly.client.module.impl.misc;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.event.impl.other.WorldChangeEvent;
import dev.fesly.impl.util.chat.ChatUtil;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;
@ModuleInfo(name = "KTLeave", description = "", category = Category.MOVEMENT)
public class KTLeave extends Module {
    @Override
    protected void onEnable() {
        super.onEnable();
    }
    @EventLink
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        toggle();
    };
    @EventLink
    public final Listener<UpdateEvent> onPlayerUpdate = event -> {
        if (mc.player == null || mc.world == null) return;
        for (int i = 0; i < 41; i++) {
            assert mc.player != null;
            mc.player.setSneaking(true);
            Vector3d v = mc.player.getPositionVec().add(i, 0, i);
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(v.x, v.y, v.z, Math.random() > 0.5));
            mc.player.connection.sendPacket(new CKeepAlivePacket((int) (Math.random() * 8)));
        }
    };
    @java.lang.SuppressWarnings("all")
    public Listener<WorldChangeEvent> getOnWorldChange() {
        return this.onWorldChange;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<UpdateEvent> getOnPlayerUpdate() {
        return this.onPlayerUpdate;
    }
}
