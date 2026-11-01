package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.ITextComponent;

public class Screen extends net.minecraft.client.gui.screens.Screen {
    public java.util.List<net.minecraft.client.gui.widget.Widget> buttons = new java.util.ArrayList<>();
    public net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().fontRenderer;

    public <T extends net.minecraft.client.gui.widget.Widget> T addButton(T button) {
        this.buttons.add(button);
        return button;
    }

    public static void fill(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, int color) {
        dev.fesly.impl.util.render.RenderUtil.drawRect(minX, minY, maxX - minX, maxY - minY, color);
    }

    public void renderTooltip(MatrixStack matrixStack, Object text, int x, int y) {
    }

    public Screen(ITextComponent title) {
        super(Component.literal(title != null ? title.getString() : ""));
    }

    public Screen() {
        super(Component.literal(""));
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (net.minecraft.client.gui.widget.Widget widget : this.buttons) {
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void extractBackground(final net.minecraft.client.gui.GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
    }

    @Override
    protected void init() {
        super.init();
    }

    public void closeScreen() {
        this.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return super.shouldCloseOnEsc();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.shouldCloseOnEsc()) {
            this.closeScreen();
            return true;
        }
        return false;
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (net.minecraft.client.gui.widget.Widget widget : this.buttons) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (net.minecraft.client.gui.widget.Widget widget : this.buttons) {
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.keyPressed(event.key(), event.scancode(), event.modifiers())) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (this.keyReleased(event.key(), event.scancode(), event.modifiers())) {
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.charTyped((char) event.codepoint(), 0)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.mouseClicked(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.mouseReleased(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.mouseDragged(event.x(), event.y(), event.button(), dx, dy)) {
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (this.mouseScrolled(x, y, scrollY)) {
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }
}
