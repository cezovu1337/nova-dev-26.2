package net.minecraft.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;

public class Button extends AbstractButton {
    public interface IPressable {
        void onPress(Button button);
    }

    public interface ITooltip {
        void onTooltip(Button button, MatrixStack matrixStack, int mouseX, int mouseY);
    }

    public static final ITooltip NO_TOOLTIP = (button, matrixStack, mouseX, mouseY) -> {};

    protected final IPressable onPress;
    protected final ITooltip onTooltip;

    public Button(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction) {
        this(x, y, width, height, title, pressedAction, NO_TOOLTIP);
    }

    public Button(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction, ITooltip onTooltip) {
        super(x, y, width, height, title);
        this.onPress = pressedAction;
        this.onTooltip = onTooltip != null ? onTooltip : NO_TOOLTIP;
    }

    @Override
    public void onPress() {
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        if (this.isHovered() && this.onTooltip != null) {
            this.onTooltip.onTooltip(this, matrixStack, mouseX, mouseY);
        }
    }
}
