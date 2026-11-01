package dev.fesly.client.module.impl.movement;

import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

@ModuleInfo(
        name = "ScreenWalk",
        description = "",
        category = Category.MOVEMENT
)
@Getter
@Setter
public class ScreenWalk extends Module {

    @EventLink()
    public final Listener<MotionEvent> onPlayerUpdate = event -> {
        KeyBinding[] moveKeys = {mc.gameSettings.keyBindRight, mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindBack, mc.gameSettings.keyBindForward, mc.gameSettings.keyBindJump,
                mc.gameSettings.keyBindSprint};

        if ((mc.currentScreen != null)
                && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof EditSignScreen)) {
            KeyBinding[] array;
            int length = (array = moveKeys).length;
            for (int i = 0; i < length; i++) {
                KeyBinding key = array[i];
                key.setPressed(InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), key.keyCode.getKeyCode()));
            }
        }

    };
}
