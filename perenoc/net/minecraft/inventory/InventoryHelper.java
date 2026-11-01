package net.minecraft.inventory;

import java.util.Random;

import dev.fesly.impl.interfaces.client.IAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@UtilityClass
public class InventoryHelper implements IAccess
{
    private static final Random RANDOM = new Random();

    public static void dropInventoryItems(World worldIn, BlockPos pos, IInventory inventory)
    {
        dropInventoryItems(worldIn, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), inventory);
    }

    public static int getItemIndex(Item item) {
        for (int i = 0; i < 45; i++) {
            if (Minecraft.getInstance().player.inventory.getStackInSlot(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static int getItemFromIndex(Item item, boolean hotbar) {
        for (int i = 0; i < (hotbar ? 9 : 45); i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static int getAxe() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() instanceof AxeItem) {
                return i;
            }
        }
        return 1;
    }

    public static int getItem(Item item) {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == item) {
                return i;
            }
        }
        return 1;
    }
    public static int getBlockItem() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return 1;
    }
    public static boolean doesHotbarHaveAxe() {
        for (int i = 0; i < 9; ++i) {
            mc.player.inventory.getStackInSlot(i);
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof AxeItem) {
                return true;
            }
        }
        return false;
    }
    public static boolean doesHotbarHaveItem(Item item) {
        for (int i = 0; i < 9; ++i) {
            mc.player.inventory.getStackInSlot(i);
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return true;
            }
        }
        return false;
    }
    public static boolean doesHotbarHaveBlock() {
        for (int i = 0; i < 9; ++i) {
            mc.player.inventory.getStackInSlot(i);
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof BlockItem) {
                return true;
            }
        }
        return false;
    }

    public static void dropInventoryItems(World worldIn, Entity entityAt, IInventory inventory)
    {
        dropInventoryItems(worldIn, entityAt.getPosX(), entityAt.getPosY(), entityAt.getPosZ(), inventory);
    }

    private static void dropInventoryItems(World worldIn, double x, double y, double z, IInventory inventory)
    {
        for (int i = 0; i < inventory.getSizeInventory(); ++i)
        {
            mc.player.inventory.dropAllItems();
            //spawnItemStack(worldIn, x, y, z, inventory.getStackInSlot(i));
        }
    }

    public static void dropItems(World p_219961_0_, BlockPos p_219961_1_, NonNullList<ItemStack> p_219961_2_)
    {
        p_219961_2_.forEach((p_219962_2_) ->
        {
            spawnItemStack(p_219961_0_, (double)p_219961_1_.getX(), (double)p_219961_1_.getY(), (double)p_219961_1_.getZ(), p_219962_2_);
        });
    }

    public static void spawnItemStack(World worldIn, double x, double y, double z, ItemStack stack)
    {
        double d0 = (double)EntityType.ITEM.getWidth();
        double d1 = 1.0D - d0;
        double d2 = d0 / 2.0D;
        double d3 = Math.floor(x) + RANDOM.nextDouble() * d1 + d2;
        double d4 = Math.floor(y) + RANDOM.nextDouble() * d1;
        double d5 = Math.floor(z) + RANDOM.nextDouble() * d1 + d2;

        while (!stack.isEmpty())
        {
            ItemEntity itementity = new ItemEntity(worldIn, d3, d4, d5, stack.split(RANDOM.nextInt(21) + 10));
            float f = 0.05F;
            itementity.setMotion(RANDOM.nextGaussian() * (double)0.05F, RANDOM.nextGaussian() * (double)0.05F + (double)0.2F, RANDOM.nextGaussian() * (double)0.05F);
            worldIn.addEntity(itementity);
        }
    }
}
