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
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.glfw.GLFW;
@ModuleInfo(name = "Beautify", description = "module.render.postprocessing.description", category = Category.RENDER, keyBind = GLFW.GLFW_KEY_M)
public class PostProcessing extends Module {
    private BooleanValue blur;
    private NumberValue blurIterations;
    private NumberValue blurOffset;
    private BooleanValue bloom;
    private NumberValue bloomIterations;
    private NumberValue bloomOffset;
    @Override
    public void init() {
        blur = new BooleanValue("Blur", this, true);
        blurIterations = new NumberValue("Blur Iterations", this, 3, 1, 8, 1, () -> !blur.getValue());
        blurOffset = new NumberValue("Blur Offset", this, 1, 1, 10, 1, () -> !blur.getValue());
        bloom = new BooleanValue("Bloom", this, true);
        bloomIterations = new NumberValue("Bloom Iterations", this, 3, 1, 8, 1, () -> !bloom.getValue());
        bloomOffset = new NumberValue("Bloom Offset", this, 1, 1, 10, 1, () -> !bloom.getValue());
        addSetting("blur", new BooleanSetting(blur));
        addSetting("blurIterations", new NumberSetting(blurIterations));
        addSetting("blurOffset", new NumberSetting(blurOffset));
        addSetting("bloom", new BooleanSetting(bloom));
        addSetting("bloomIterations", new NumberSetting(bloomIterations));
        addSetting("bloomOffset", new NumberSetting(bloomOffset));
    }
    public void stuffToBlur(boolean bloom) {
    }
    private Framebuffer stencilFramebuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);
    public void blurScreen(MatrixStack matrix) {
        if (!isEnabled() || mc.gameSettings.showDebugInfo) return;
        if (blur.getValue()) {
            IAccess.BLUR_RUNNABLES.add(() -> {
                Nova.getInstance().getEventBus().handle(new ShaderEvent(matrix, false));
                stuffToBlur(false);
            });
        }
        if (bloom.getValue()) {
            IAccess.BLOOM_RUNNABLES.add(() -> {
                Nova.getInstance().getEventBus().handle(new ShaderEvent(matrix, true));
                stuffToBlur(true);
            });
        }
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PostProcessing)) return false;
        final PostProcessing other = (PostProcessing) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$blur = this.getBlur();
        final java.lang.Object other$blur = other.getBlur();
        if (this$blur == null ? other$blur != null : !this$blur.equals(other$blur)) return false;
        final java.lang.Object this$blurIterations = this.getBlurIterations();
        final java.lang.Object other$blurIterations = other.getBlurIterations();
        if (this$blurIterations == null ? other$blurIterations != null : !this$blurIterations.equals(other$blurIterations)) return false;
        final java.lang.Object this$blurOffset = this.getBlurOffset();
        final java.lang.Object other$blurOffset = other.getBlurOffset();
        if (this$blurOffset == null ? other$blurOffset != null : !this$blurOffset.equals(other$blurOffset)) return false;
        final java.lang.Object this$bloom = this.getBloom();
        final java.lang.Object other$bloom = other.getBloom();
        if (this$bloom == null ? other$bloom != null : !this$bloom.equals(other$bloom)) return false;
        final java.lang.Object this$bloomIterations = this.getBloomIterations();
        final java.lang.Object other$bloomIterations = other.getBloomIterations();
        if (this$bloomIterations == null ? other$bloomIterations != null : !this$bloomIterations.equals(other$bloomIterations)) return false;
        final java.lang.Object this$bloomOffset = this.getBloomOffset();
        final java.lang.Object other$bloomOffset = other.getBloomOffset();
        if (this$bloomOffset == null ? other$bloomOffset != null : !this$bloomOffset.equals(other$bloomOffset)) return false;
        final java.lang.Object this$stencilFramebuffer = this.getStencilFramebuffer();
        final java.lang.Object other$stencilFramebuffer = other.getStencilFramebuffer();
        if (this$stencilFramebuffer == null ? other$stencilFramebuffer != null : !this$stencilFramebuffer.equals(other$stencilFramebuffer)) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof PostProcessing;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final java.lang.Object $blur = this.getBlur();
        result = result * PRIME + ($blur == null ? 43 : $blur.hashCode());
        final java.lang.Object $blurIterations = this.getBlurIterations();
        result = result * PRIME + ($blurIterations == null ? 43 : $blurIterations.hashCode());
        final java.lang.Object $blurOffset = this.getBlurOffset();
        result = result * PRIME + ($blurOffset == null ? 43 : $blurOffset.hashCode());
        final java.lang.Object $bloom = this.getBloom();
        result = result * PRIME + ($bloom == null ? 43 : $bloom.hashCode());
        final java.lang.Object $bloomIterations = this.getBloomIterations();
        result = result * PRIME + ($bloomIterations == null ? 43 : $bloomIterations.hashCode());
        final java.lang.Object $bloomOffset = this.getBloomOffset();
        result = result * PRIME + ($bloomOffset == null ? 43 : $bloomOffset.hashCode());
        final java.lang.Object $stencilFramebuffer = this.getStencilFramebuffer();
        result = result * PRIME + ($stencilFramebuffer == null ? 43 : $stencilFramebuffer.hashCode());
        return result;
    }
    @java.lang.SuppressWarnings("all")
    public PostProcessing() {
    }
    @java.lang.SuppressWarnings("all")
    public BooleanValue getBlur() {
        return this.blur;
    }
    @java.lang.SuppressWarnings("all")
    public NumberValue getBlurIterations() {
        return this.blurIterations;
    }
    @java.lang.SuppressWarnings("all")
    public NumberValue getBlurOffset() {
        return this.blurOffset;
    }
    @java.lang.SuppressWarnings("all")
    public BooleanValue getBloom() {
        return this.bloom;
    }
    @java.lang.SuppressWarnings("all")
    public NumberValue getBloomIterations() {
        return this.bloomIterations;
    }
    @java.lang.SuppressWarnings("all")
    public NumberValue getBloomOffset() {
        return this.bloomOffset;
    }
    @java.lang.SuppressWarnings("all")
    public Framebuffer getStencilFramebuffer() {
        return this.stencilFramebuffer;
    }
    @java.lang.SuppressWarnings("all")
    public void setBlur(final BooleanValue blur) {
        this.blur = blur;
    }
    @java.lang.SuppressWarnings("all")
    public void setBlurIterations(final NumberValue blurIterations) {
        this.blurIterations = blurIterations;
    }
    @java.lang.SuppressWarnings("all")
    public void setBlurOffset(final NumberValue blurOffset) {
        this.blurOffset = blurOffset;
    }
    @java.lang.SuppressWarnings("all")
    public void setBloom(final BooleanValue bloom) {
        this.bloom = bloom;
    }
    @java.lang.SuppressWarnings("all")
    public void setBloomIterations(final NumberValue bloomIterations) {
        this.bloomIterations = bloomIterations;
    }
    @java.lang.SuppressWarnings("all")
    public void setBloomOffset(final NumberValue bloomOffset) {
        this.bloomOffset = bloomOffset;
    }
    @java.lang.SuppressWarnings("all")
    public void setStencilFramebuffer(final Framebuffer stencilFramebuffer) {
        this.stencilFramebuffer = stencilFramebuffer;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "PostProcessing(blur=" + this.getBlur() + ", blurIterations=" + this.getBlurIterations() + ", blurOffset=" + this.getBlurOffset() + ", bloom=" + this.getBloom() + ", bloomIterations=" + this.getBloomIterations() + ", bloomOffset=" + this.getBloomOffset() + ", stencilFramebuffer=" + this.getStencilFramebuffer() + ")";
    }
}
