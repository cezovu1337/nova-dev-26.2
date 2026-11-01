package net.minecraft.world.item;

import net.minecraft.inventory.EquipmentSlotType;

public class ArmorItem extends Item {
    public EquipmentSlotType slot = EquipmentSlotType.CHEST;

    public ArmorItem(Item.Properties properties) {
        super(properties);
    }
}
