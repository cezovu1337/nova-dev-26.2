package dev.fesly.impl.util.player;

import dev.fesly.client.module.impl.combat.AntiBot;
import dev.fesly.client.module.impl.misc.Blink;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.event.impl.other.WorldChangeEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BlinkUtils implements IAccess {
    public static boolean blinking = false;
    public static int next;
    public static int plus;
    private static boolean disableLogger;
    private static final List<double[]> positions = new LinkedList<>();
    private static RemoteClientPlayerEntity blinkEntity;

    public static final List<IPacket<?>> packets = new ArrayList<>();
    private void blink() {
        if (next == 0) {
            next = 1;
        }else {
            next = 0;
        }
        try {
            disableLogger = true;
            Iterator<IPacket<?>> packetIterator = packets.iterator();
            while (packetIterator.hasNext()) {
                mc.player.connection.sendPacket(packetIterator.next());
                packetIterator.remove();
            }
            disableLogger = false;
        } catch (Exception e) {
            e.printStackTrace();
            disableLogger = false;
        }
        synchronized (positions) {
            positions.clear();
        }
    }
    public BlinkUtils() {
    }
    public void startBlinking() {
        //getNext();
        blink();
        blinking = true;
        synchronized (positions) {
            positions.add(new double[]{mc.player.getPosX(), mc.player.getBoundingBox().minY + mc.player.getEyeHeight() / 2.0f, mc.player.getPosZ()});
            positions.add(new double[]{mc.player.getPosX(), mc.player.getBoundingBox().minY, mc.player.getPosZ()});
        }
        if (Blink.isSpawnGhost())
            spawnEntity();

    }

    public void stopBlinking() {
        blink();
        blinking = false;
        if (Blink.isSpawnGhost())
            deSpawnEntity();
    }

    @EventLink()
    public final Listener<PacketSendEvent> onSendPacket = event -> {
        if (blinking) {
            if (mc.player == null || !(event.getPacket() instanceof CPlayerPacket) || disableLogger) {
                return;
            }
            event.setCancelled(true);
            if (!(event.getPacket() instanceof CPlayerPacket.PositionPacket) && !(event.getPacket() instanceof CPlayerPacket.PositionRotationPacket)) {
                return;
            }
            packets.add(event.getPacket());
        }
    };
    @EventLink()
    public final Listener<UpdateEvent> onPlayerUpdate = event -> {
        if (blinking) plus++;
        else plus = 0;
    };
    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        if (blinking) {
            getNext();
            blinking = false;
            if (Blink.isSpawnGhost())
                stopBlinking();
        }
    };
    public void getNext() {
        if (mc.player == null) return;
        //next = mc.player.ticksExisted + plus;
    }
    public void deSpawnEntity() {
        if (blinkEntity != null) {

            AntiBot.isBot.remove(blinkEntity);
            if (mc.world != null)
                mc.world.removeEntityFromWorld(blinkEntity.getEntityId());
            blinkEntity = null;
        }
    }

    public void spawnEntity() {
        if (blinkEntity == null && mc.world != null && mc.player != null) {
            blinkEntity = new RemoteClientPlayerEntity(mc.world, mc.player.getGameProfile());
            blinkEntity.setPositionAndRotation(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch);
            blinkEntity.rotationYawHead = mc.player.rotationYawHead;
            blinkEntity.setSprinting(mc.player.isSprinting());
            blinkEntity.setInvisible(mc.player.isInvisible());
            blinkEntity.setSneaking(mc.player.isSneaking());
            blinkEntity.inventory = mc.player.inventory;
            AntiBot.isBot.add(blinkEntity);
            mc.world.addEntity(blinkEntity.getEntityId(), blinkEntity);
        }
    }
}