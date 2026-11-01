package dev.fesly.client.module.impl.combat;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.util.render.ScrollUtil;
import dev.fesly.impl.util.time.TimerUtil;
import dev.fesly.impl.value.secondValues.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@ModuleInfo(name = "AutoTotem", description = "", category = Category.COMBAT)
public class AutoTotem extends Module {
    private boolean swapped = true;
    private final int LEFT_HAND_SLOT_ID = 45;
    private float lastYaw;
    private float lastPitch;
    private float totemUseHealth;
    private float delay = 3;
    private Item lastItem = Items.AIR;
    private int oldTotemSlotId = -1;
    @Override
    public void init() {
        super.init();
        addSetting("health", new NumberSetting("Health", this, this::getTotemUseHealth, this::setTotemUseHealth, "%.1f", 1.0F, 20.0F));
        addSetting("delay", new NumberSetting("Ticks Delay", this, this::getDelay, this::setDelay, "%.1f", 1.0F, 19.0F));
    }
    @EventLink
    public final Listener<UpdateEvent> onCalcCoolDown = event -> {
        if (InventoryHelper.getItemIndex(Items.TOTEM_OF_UNDYING) == -1) {
            return;
        }
        float playerHealth = mc.player.getHealth();
        boolean toUseTotem = (playerHealth <= totemUseHealth);
        lastYaw = mc.player.rotationYaw;
        lastPitch = mc.player.rotationPitch;
        swapped = !(mc.player.inventory.getStackInSlot(40).getItem() == Items.TOTEM_OF_UNDYING);
        if (!swapped) {
        }
        if (toUseTotem && mc.player.ticksExisted % ((int) delay) == 0 && (mc.player.onGround || mc.player.fallDistance > 0.3 || mc.player.isInWater())) {
            if (mc.player.inventory.getStackInSlot(40).getItem() != Items.TOTEM_OF_UNDYING && mc.player.inventory.getStackInSlot(40).getItem() != Items.AIR) {
                lastItem = mc.player.inventory.getStackInSlot(40).getItem();
            }
            int totemSlotId = -1;
            int lastSlotId = -1;
            for (int i = 0; i < 45; i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    if (i != 5 && i != 6 && i != 7 && i != 8) totemSlotId = i;
                }
            }
            for (int i = 0; i < 45; i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == lastItem) {
                    if (i != 5 && i != 6 && i != 7 && i != 8) lastSlotId = i;
                }
            }
            oldTotemSlotId = totemSlotId;
            if (mc.player.inventory.getStackInSlot(40).getItem() != Items.TOTEM_OF_UNDYING && totemSlotId != -1 && swapped) {
                int index = InventoryHelper.getItemIndex(Items.TOTEM_OF_UNDYING);
                mc.playerController.windowClick(0, index < 9 ? index + 36 : index, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 45, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, index < 9 ? index + 36 : index, 1, ClickType.PICKUP, mc.player);
                swapped = !(mc.player.inventory.getStackInSlot(40).getItem() == Items.TOTEM_OF_UNDYING);
            }
        }
        if (!toUseTotem && mc.player.ticksExisted % ((int) delay) == 0 && ((!mc.player.onGround && mc.player.fallDistance > 0.3))) {
            if (mc.player.inventory.getStackInSlot(40).getItem() != lastItem && !swapped) {
                int lastSlotId = InventoryHelper.getItemIndex(lastItem);
                for (int i = 0; i < 36; i++) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == lastItem) {
                        lastSlotId = i;
                    }
                }
                int slots = -1;
                for (int i = 0; i < 36; i++) {
                    ItemStack s = mc.player.inventory.getStackInSlot(i);
                    if (s.getItem() == lastItem) {
                        slots = i;
                        break;
                    }
                }
                int slot;
                if (slots < 9 && slots != -1) {
                    slot = slots + 36;
                } else {
                    slot = slots;
                }
                mc.playerController.windowClick(0, 45, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 45, 1, ClickType.PICKUP, mc.player);
                swapped = mc.player.inventory.getStackInSlot(40).getItem() == lastItem;
                mc.player.onGround = false;
            }
        }
    };
    public static AutoTotem get() {
        return Nova.getInstance().getModuleManager().get(AutoTotem.class);
    }
    @java.lang.SuppressWarnings("all")
    public boolean isSwapped() {
        return this.swapped;
    }
    @java.lang.SuppressWarnings("all")
    public int getLEFT_HAND_SLOT_ID() {
        return this.LEFT_HAND_SLOT_ID;
    }
    @java.lang.SuppressWarnings("all")
    public float getLastYaw() {
        return this.lastYaw;
    }
    @java.lang.SuppressWarnings("all")
    public float getLastPitch() {
        return this.lastPitch;
    }
    @java.lang.SuppressWarnings("all")
    public float getTotemUseHealth() {
        return this.totemUseHealth;
    }
    @java.lang.SuppressWarnings("all")
    public float getDelay() {
        return this.delay;
    }
    @java.lang.SuppressWarnings("all")
    public Item getLastItem() {
        return this.lastItem;
    }
    @java.lang.SuppressWarnings("all")
    public int getOldTotemSlotId() {
        return this.oldTotemSlotId;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<UpdateEvent> getOnCalcCoolDown() {
        return this.onCalcCoolDown;
    }
    @java.lang.SuppressWarnings("all")
    public void setSwapped(final boolean swapped) {
        this.swapped = swapped;
    }
    @java.lang.SuppressWarnings("all")
    public void setLastYaw(final float lastYaw) {
        this.lastYaw = lastYaw;
    }
    @java.lang.SuppressWarnings("all")
    public void setLastPitch(final float lastPitch) {
        this.lastPitch = lastPitch;
    }
    @java.lang.SuppressWarnings("all")
    public void setTotemUseHealth(final float totemUseHealth) {
        this.totemUseHealth = totemUseHealth;
    }
    @java.lang.SuppressWarnings("all")
    public void setDelay(final float delay) {
        this.delay = delay;
    }
    @java.lang.SuppressWarnings("all")
    public void setLastItem(final Item lastItem) {
        this.lastItem = lastItem;
    }
    @java.lang.SuppressWarnings("all")
    public void setOldTotemSlotId(final int oldTotemSlotId) {
        this.oldTotemSlotId = oldTotemSlotId;
    }
}
