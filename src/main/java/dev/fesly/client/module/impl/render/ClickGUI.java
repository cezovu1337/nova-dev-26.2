package dev.fesly.client.module.impl.render;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import org.lwjgl.glfw.GLFW;
@ModuleInfo(
        name = "ClickGUI",
        category = Category.RENDER,
        description = "Открывает меню клиента",
        keyBind = GLFW.GLFW_KEY_RIGHT_SHIFT
)
public class ClickGUI extends Module {
    @Override
    public void onEnable() {
        super.onEnable();
        mc.displayScreen(Nova.getInstance().getClickGui());
        setEnabled(false);
    }
}
