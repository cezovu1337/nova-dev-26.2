package net.minecraft.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.world.entity.player.Player;

public class PlayerController {
    public float getBlockReachDistance() {
        return 4.5F;
    }

    public void windowClick(int windowId, int slotId, int mouseButton, ClickType type, Player player) {
        if (Minecraft.getInstance().gameMode != null && player != null) {
            net.minecraft.world.inventory.ContainerInput input = net.minecraft.world.inventory.ContainerInput.values()[Math.min(type.ordinal(), net.minecraft.world.inventory.ContainerInput.values().length - 1)];
            Minecraft.getInstance().gameMode.handleContainerInput(windowId, slotId, mouseButton, input, player);
        }
    }

    public void attackEntity(Player player, net.minecraft.world.entity.Entity target) {
        if (Minecraft.getInstance().gameMode != null && player != null && target != null) {
            Minecraft.getInstance().gameMode.attack(player, target);
        }
    }

    public void updateController() {
        if (Minecraft.getInstance().gameMode != null) {
            Minecraft.getInstance().gameMode.tick();
        }
    }
}
