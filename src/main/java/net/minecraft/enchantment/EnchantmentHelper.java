package net.minecraft.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentHelper {
    public static int getEnchantmentLevel(Object enchantment, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        try {
            if (enchantment instanceof ResourceKey) {
                var reg = net.minecraft.client.Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                var holder = reg.get((ResourceKey<Enchantment>) enchantment);
                if (holder.isPresent()) {
                    return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(holder.get(), stack);
                }
            } else if (enchantment instanceof Holder) {
                return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel((Holder<Enchantment>) enchantment, stack);
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }
}
