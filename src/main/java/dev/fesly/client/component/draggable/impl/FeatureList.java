package dev.fesly.client.component.draggable.impl;
import dev.fesly.client.component.draggable.Draggable;
import dev.fesly.client.component.draggable.DraggableManager;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.impl.render.Interface;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.util.shader.ShaderUtil;
import lombok.val;
import net.minecraft.util.text.TextFormatting;
import java.awt.*;
public class FeatureList extends Draggable {
    private Font font = Fonts.ROBOTO_MEDIUM.get(16);
    public FeatureList() {
        super("arraylist", 4, 25, 100, 20);
    }
    @Override
    public boolean isVisible() {
        return true;
    }
    @Override
    public void drawPre(double mouseX, double mouseY, float partialTicks) {
        IAccess.addRunnableWithChecks(getArrayList(1));
        IAccess.POST_RUNNABLES.add(getArrayList(2));
    }
    @Override
    public void draw(double mouseX, double mouseY, float partialTicks) {
    }
    private float longestWidth;
    private static final dev.fesly.impl.util.animation.compact.Animation tap = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 155);
    public static float getScaleAnim() {
        return Math.max(1.0F - tap.getNumberValue().floatValue(), 0);
    }
    private float fullheight = 0;
    public Runnable getArrayList(int lvl) {
        font = nova.getClientUI().isThinFonts() ? Fonts.ROBOTO_REGULAR.get(16) : Fonts.ROBOTO_MEDIUM.get(16);
        if (!ClientUI.getHudElements().contains(ClientUI.HudElementsEnum.ArrayList)) {
            return () -> {
            };
        }
        boolean dragged = DraggableManager.dragged instanceof FeatureList;
        tap.run((dragged && !DraggableManager.draggedTimer.hasReached(150)) ? 0.01F : 0);
        return () -> {
            ScaleUtil.scaleStart(x + width / 2.0F, y + height / 2.0F, getScaleAnim());
            float offset = 0;
            int id = 0;
            for (final Interface.ModuleComponent module : Interface.activeModuleComponents) {
                boolean isAnimationScaling = Interface.animationEnum == Interface.AnimationEnum.Scaling;
                String lowercase = module.getModule().getModuleInfo().name().replace(" ", "");
                String arrow = ClientUtil.getClientFormatting() + " -> " + TextFormatting.WHITE;
                String name = lowercase + (module.getModule().prefix != null ? arrow + module.getModule().getPrefix() : " ");
                boolean right = getX() > mw.getScaledWidth() / 2.0F;
                float x1 = right ? x + width - font.getWidth(name) - 2 : x;
                float wdth = right ? font.getWidth(name) + 3 : 0;
                float height = 11;
                if (isAnimationScaling) ScaleUtil.scaleStart(x1 + (font.getWidth(name) + 2) / 2.0F, y + offset + 5.25F * module.module.animation.getNonNullOutput().floatValue(), module.getModule().animation.getOutput().floatValue());
                if (lvl == 1) {
                    if (id < Interface.activeModuleComponents.size() - 1) {
                        RenderUtil.roundedGradientRectangle(x1, y + offset, font.getWidth(name) + 4, height * module.module.animation.getNonNullOutput().floatValue(), 2.5F, ClientUtil.getOneClientColor(), ClientUtil.getTwoClientColor(), ClientUtil.getThreeClientColor(), ClientUtil.getFourClientColor());
                        RenderUtil.roundedGradientRectangle(x1 + wdth, y + 2 + offset, 1, 7.3F + (Interface.activeModuleComponents.get(Math.min(Interface.activeModuleComponents.size(), id + 1)).module.animation.getNonNullOutput().floatValue() * 3 * (module.module.animation.getNonNullOutput().floatValue())), 0, ClientUtil.getOneClientColor(), ClientUtil.getTwoClientColor(), ClientUtil.getThreeClientColor(), ClientUtil.getFourClientColor());
                    } else {
                        RenderUtil.roundedGradientRectangle(x1, y + offset, font.getWidth(name) + 4, height * module.module.animation.getNonNullOutput().floatValue(), 2.5F, ClientUtil.getOneClientColor(), ClientUtil.getTwoClientColor(), ClientUtil.getThreeClientColor(), ClientUtil.getFourClientColor());
                    }
                }
                if (lvl == 2) {
                    font.draw(name, x1 + 1, y + offset + font.getMiddleOfBox(height * module.module.animation.getNonNullOutput().floatValue()), new Color(255, 255, 255, isAnimationScaling ? 220 : (int) (Math.min(module.module.animation.getNonNullOutput(), 1) * 230)).getRGB());
                }
                if (isAnimationScaling) ScaleUtil.scaleEnd();
                offset += font.getHeight() * Math.min(module.module.animation.getNonNullOutput(), 1);
                id++;
            }
            ScaleUtil.scaleEnd();
            setHeight((int) offset);
        };
    }
    public class LongestConstructor {
        private final Module module;
        @java.lang.SuppressWarnings("all")
        public LongestConstructor(final Module module) {
            this.module = module;
        }
    }
}
