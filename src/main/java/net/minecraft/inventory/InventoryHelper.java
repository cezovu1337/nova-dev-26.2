package net.minecraft.inventory;

import dev.fesly.impl.interfaces.client.IAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InventoryHelper implements IAccess {
    public static int getItemIndex(Item item) {
        if (Minecraft.getInstance().player == null) return -1;
        for (int i = 0; i < 45; i++) {
            ItemStack stack = Minecraft.getInstance().player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static int getItemFromIndex(Item item, boolean inHotbar) {
        return getItemIndex(item);
    }

    public static int getItem(Item item) {
        return getItemIndex(item);
    }

    public static boolean doesHotbarHaveItem(Item item) {
        if (Minecraft.getInstance().player == null) return false;
        for (int i = 0; i < 9; i++) {
            if (Minecraft.getInstance().player.getInventory().getItem(i).getItem() == item) {
                return true;
            }
        }
        return false;
    }

    public static int getBlockItem() {
        if (Minecraft.getInstance().player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Minecraft.getInstance().player.getInventory().getItem(i);
            if (stack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                return i;
            }
        }
        return -1;
    }

    public static boolean doesHotbarHaveBlock() {
        return getBlockItem() != -1;
    }
}
