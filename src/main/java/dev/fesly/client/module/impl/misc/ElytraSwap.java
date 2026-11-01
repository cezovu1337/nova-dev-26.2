package dev.fesly.client.module.impl.misc;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.value.secondValues.BindSetting;
import dev.fesly.impl.value.secondValues.Setting;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.world.item.*;
@ModuleInfo(name = "ElytraSwap", description = "", category = Category.MOVEMENT)
public class ElytraSwap extends Module {
    private int oldElytraSlot = -1;
    private int bind = -1;
    private BindSetting setting = null;
    @Override
    public void init() {
        super.init();
        Setting keyValue = new BindSetting("Key Value", this, this::getBind, this::setBind, () -> {
            if (mc.player != null && mc.world != null) {
                if (mc.player.inventory.armorItemInSlot(2).getItem() != Items.ELYTRA) {
                    int emptySlot = InventoryHelper.getItemIndex(Item.getItemFromBlock(Blocks.AIR));
                    if (emptySlot != -1 && mc.player.inventory.getItemStack().getItem() == Items.ELYTRA && InventoryHelper.getItemIndex(Items.ELYTRA) == -1) {
                        mc.playerController.windowClick(0, (emptySlot < 9) ? (emptySlot + 36) : emptySlot, 1, ClickType.SWAP, mc.player);
                    }
                    if (mc.player.inventory.getItemStack().getItem() instanceof ArmorItem && mc.player.inventory.armorItemInSlot(2).isEmpty()) {
                        ArmorItem armor = (ArmorItem) mc.player.inventory.getItemStack().getItem();
                        if (armor.slot == EquipmentSlotType.CHEST) {
                            mc.playerController.windowClick(0, 6, 1, ClickType.SWAP, mc.player);
                        }
                    }
                    if (mc.player.inventory.getItemStack().getItem() instanceof ArmorItem && !mc.player.inventory.armorItemInSlot(2).isEmpty()) {
                        mc.playerController.windowClick(0, 6, 1, ClickType.SWAP, mc.player);
                        int emptySlot2 = InventoryHelper.getItemIndex(Item.getItemFromBlock(Blocks.AIR));
                        if (emptySlot2 != -1 && mc.player.inventory.getItemStack().getItem() == Items.ELYTRA && InventoryHelper.getItemIndex(Items.ELYTRA) == -1) {
                            mc.playerController.windowClick(0, (emptySlot2 < 9) ? (emptySlot2 + 36) : emptySlot2, 1, ClickType.SWAP, mc.player);
                        }
                    }
                    oldElytraSlot = InventoryHelper.getItemFromIndex(Items.ELYTRA, false);
                    if (mc.player.inventory.getItemStack().getItem() != Items.ELYTRA) {
                        mc.playerController.windowClick(0, (oldElytraSlot < 9) ? (oldElytraSlot + 36) : oldElytraSlot, 1, ClickType.SWAP, mc.player);
                    }
                    mc.playerController.windowClick(0, 6, 1, ClickType.SWAP, mc.player);
                } else {
                    mc.playerController.windowClick(0, 6, 1, ClickType.SWAP, mc.player);
                    mc.playerController.windowClick(0, (oldElytraSlot < 9) ? (oldElytraSlot + 36) : oldElytraSlot, 1, ClickType.SWAP, mc.player);
                }
            }
        });
        addSetting("keyValue", keyValue);
        Nova.getInstance().getEventBus().register(keyValue);
    }
    public int findAirSlot() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof AirItem) {
                if (i < 9) {
                    i += 36;
                }
                return i;
            }
        }
        return -1;
    }
    @Override
    protected void onEnable() {
        super.onEnable();
        if (mc.player != null && mc.world != null) {
            oldElytraSlot = InventoryHelper.getItemFromIndex(Items.ELYTRA, false);
            if (mc.player.inventory.getItemStack().getItem() != Items.ELYTRA) {
                mc.playerController.windowClick(0, (oldElytraSlot < 9) ? (oldElytraSlot + 36) : oldElytraSlot, 1, ClickType.SWAP, mc.player);
            }
            mc.playerController.windowClick(0, 6, 1, ClickType.SWAP, mc.player);
        }
    }
    @Override
    protected void onDisable() {
        super.onDisable();
        mc.playerController.windowClick(0, 6, 1, ClickType.SWAP, mc.player);
        mc.playerController.windowClick(0, (oldElytraSlot < 9) ? (oldElytraSlot + 36) : oldElytraSlot, 1, ClickType.SWAP, mc.player);
    }
    @java.lang.SuppressWarnings("all")
    public int getOldElytraSlot() {
        return this.oldElytraSlot;
    }
    @java.lang.SuppressWarnings("all")
    public BindSetting getSetting() {
        return this.setting;
    }
    @java.lang.SuppressWarnings("all")
    public void setOldElytraSlot(final int oldElytraSlot) {
        this.oldElytraSlot = oldElytraSlot;
    }
    @java.lang.SuppressWarnings("all")
    public void setSetting(final BindSetting setting) {
        this.setting = setting;
    }
    @java.lang.SuppressWarnings("all")
    public int getBind() {
        return this.bind;
    }
    @java.lang.SuppressWarnings("all")
    public void setBind(final int bind) {
        this.bind = bind;
    }
}
