package dev.fesly.client.module.impl.movement;

import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.impl.client.screen.mainmenu.AltManager;
import dev.fesly.impl.client.screen.mainmenu.MainMenu;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.ClientMovementInput;
import dev.fesly.impl.event.impl.input.MoveInputEvent;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.util.player.MoveUtil;
import dev.fesly.impl.util.shader.ShaderUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.client.CEncryptionResponsePacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.status.client.CPingPacket;
import net.minecraft.network.status.client.CServerQueryPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.optifine.BlockDir.DOWN;

@ModuleInfo(
        name = "NoSlow",
        description = "",
        category = Category.MOVEMENT
)
@Getter
@Setter
public class NoSlowDown extends Module {

    private List<IPacket<?>> packets = new ArrayList<>();

    private boolean blinking = false;
    private boolean grimAc = true;

    @Override
    public void init() {
        super.init();
        addSetting("grimAC", new BooleanSetting("grimAC", this,this::isGrimAc,this::setGrimAc));
    }

    @EventLink()
    public final Listener<PacketSendEvent> onSendPacket = evt -> {
        IPacket<?> packet = evt.getPacket();

        if (packet instanceof CHandshakePacket || packet instanceof CLoginStartPacket || packet instanceof CServerQueryPacket || packet instanceof CPingPacket
        || packet instanceof CEncryptionResponsePacket) return;

        if (mc.player != null)
            if (mc.player.isHandActive()) {
                if (mc.getCurrentServerData() == null) return;
                if (mc.getCurrentServerData().serverIP != null) {
                    if (packet instanceof SConfirmTransactionPacket) {
                        evt.setCancelled();
                    }
                }
            }
    };

    private float usingTicks = 0;

    @EventLink()
    public final Listener<ClientMovementInput> onInput = event -> {
        boolean handActive = grimAc ? mc.player.isHandActive(Hand.OFF_HAND) : mc.player.isHandActive();

        if (handActive && MoveUtil.isMoving()) {
            if (!grimAc || (usingTicks > 3))
                event.setCancelled();
        }
    };


    @EventLink()
    public final Listener<UpdateEvent> onPlayerUpdate = event -> {

        assert mc.playerController != null;

        boolean handActive = grimAc ? mc.player.isHandActive(Hand.OFF_HAND) : mc.player.isHandActive();

        assert mc.player != null;
        if (handActive) {
            usingTicks++;
            if (!grimAc || (usingTicks > 3)) {
                mc.playerController.updateController();
                if (mc.player.ticksExisted % 4 == 0) {
                    int old = mc.player.inventory.currentItem;
                    mc.player.connection.sendPacket(new CHeldItemChangePacket((old + 1) > 8 ? old - 1 : old + 1));
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(old));
                    //mc.player.mulVelocity(0.63,1,0.63);
                    //mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
                }
            }
        }else {
            usingTicks = 0;
        }
    };
}
