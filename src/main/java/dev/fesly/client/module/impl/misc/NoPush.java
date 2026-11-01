package dev.fesly.client.module.impl.misc;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
@ModuleInfo(
        name = "NoPush",
        category = Category.MISC,
        description = "Какая то хуета под ногами"
)
public class NoPush extends Module {
    @Override
    public void init() {
        super.init();
    }
    public static NoPush getInstance() {
        return nova.getModuleManager().get(NoPush.class);
    }
}
