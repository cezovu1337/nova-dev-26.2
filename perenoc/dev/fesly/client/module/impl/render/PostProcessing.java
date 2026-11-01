package dev.fesly.client.module.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.client.screen.clickui.BaseScreen;
import dev.fesly.impl.event.impl.render.ShaderEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.shader.impl.KawaseBloom;
import dev.fesly.impl.util.shader.impl.KawaseBlur;
import dev.fesly.impl.value.defaultValues.impl.BooleanValue;
import dev.fesly.impl.value.defaultValues.impl.NumberValue;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.glfw.GLFW;

@EqualsAndHashCode(callSuper = true)
@Data
@ModuleInfo(
        name = "Beautify",
        description = "module.render.postprocessing.description",
        category = Category.RENDER,
        keyBind = GLFW.GLFW_KEY_M
)
public class PostProcessing extends Module {

    private  BooleanValue blur;
    private  NumberValue blurIterations;
    private  NumberValue blurOffset;
    private  BooleanValue bloom;
    private  NumberValue bloomIterations;
    private  NumberValue bloomOffset;

    @Override
    public void init() {
        blur = new BooleanValue("Blur", this, true);
        blurIterations = new NumberValue("Blur Iterations", this, 3, 1, 8, 1, () -> !blur.getValue());
        blurOffset = new NumberValue("Blur Offset", this, 1, 1, 10, 1, () -> !blur.getValue());
        bloom = new BooleanValue("Bloom", this, true);
        bloomIterations = new NumberValue("Bloom Iterations", this, 3, 1, 8, 1, () -> !bloom.getValue());
        bloomOffset = new NumberValue("Bloom Offset", this, 1, 1, 10, 1, () -> !bloom.getValue());

        addSetting("blur",new BooleanSetting(blur));
        addSetting("blurIterations",new NumberSetting(blurIterations));
        addSetting("blurOffset",new NumberSetting(blurOffset));
        addSetting("bloom",new BooleanSetting(bloom));
        addSetting("bloomIterations",new NumberSetting(bloomIterations));
        addSetting("bloomOffset",new NumberSetting(bloomOffset));
    }

    public void stuffToBlur(boolean bloom) {


    }

    private Framebuffer stencilFramebuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);

    public void blurScreen(MatrixStack matrix) {
        if (!isEnabled() || mc.gameSettings.showDebugInfo) return;
        if (blur.getValue()) {
            IAccess.BLUR_RUNNABLES.add(( ) -> {
                Nova.getInstance().getEventBus().handle(new ShaderEvent(matrix, false));
                if (mc.currentScreen == Nova.getInstance().getClickGui() && Nova.getInstance().getClickGui().background != null)
                    Nova.getInstance().getClickGui().background.run();
                stuffToBlur(false);
            });
        }


        if (bloom.getValue()) {
            IAccess.BLOOM_RUNNABLES.add(( ) -> {
                Nova.getInstance().getEventBus().handle(new ShaderEvent(matrix, true));
                stuffToBlur(true);
            });
        }
    }

}