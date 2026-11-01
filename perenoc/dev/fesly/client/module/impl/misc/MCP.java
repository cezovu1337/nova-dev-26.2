package dev.fesly.client.module.impl.misc;

import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.ChatReceiveEvent;
import dev.fesly.impl.event.impl.input.KeyboardInputEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(
        name = "MCP",
        category = Category.MISC,
        description = "При нажатии на центральную кнопку мыши кидает перл"
)
public class MCP extends Module {

    public static boolean enabled() {
        return nova.getModuleManager().get(MCP.class).isEnabled();
    }

    @EventLink()
    public final Listener<MotionEvent> onKeyInput = event -> {
        if (mc.player == null || mc.world == null)
        System.out.println(mc.mouseHelper.activeButton);
        if (mc.mouseHelper.activeButton == 2) {
            int slot = mc.player.inventory.currentItem;
            int pearle = InventoryHelper.getItem(Items.ENDER_PEARL);
            if (InventoryHelper.doesHotbarHaveItem(Items.ENDER_PEARL)) {
                //mc.player.inventory.currentItem = InventoryHelper.getItem(Items.ENDER_PEARL);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(pearle));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
            }
        }
    };
}
