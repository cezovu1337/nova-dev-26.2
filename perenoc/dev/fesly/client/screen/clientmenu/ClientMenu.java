package dev.fesly.client.screen.clientmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.client.screen.IScreen;
import dev.fesly.client.screen.clientmenu.impl.Bezier;
import dev.fesly.client.screen.clientmenu.impl.CategoryScreen;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.awt.*;

public class ClientMenu extends Screen implements IAccess {

    public ClientMenu() {
        super(StringTextComponent.EMPTY);
    }

    public Vector2d position;
    public Vector2d size;

    private final IScreen selected = new CategoryScreen();
    private final IScreen bezier = new Bezier(new Vector2f(100, 50));

    @Override
    protected void init() {
        super.init();

        double width = 540;
        double height = 320;
        double x = mw.getWidth() / 4F - width / 2F;
        double y = mw.getHeight() / 4F - height / 2F;
        position = new Vector2d(x, y);
        size = new Vector2d(width, height);

        bezier.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        double x = position.x;
        double y = position.y;
        double width = size.x;
        double height = size.y;

        int[] mouse = ScaleUtil.calc(mouseX, mouseY);

        ScaleUtil.scale_pre();

        drawBackground(x, y, width, height);
        selected.render(matrixStack, mouse[0], mouse[1], partialTicks);

        ScaleUtil.scale_post();
        bezier.render(matrixStack, mouseX, mouseY, partialTicks);

    }

    private void drawBackground(double x, double y, double width, double height) {
        RenderUtil.applyBlur(3,
                1,
                () -> RenderUtil.roundedRectangle(x, y, width, height, 5, new Color(0, 0, 0, 255)));
        RenderUtil.applyBloom(3,
                1,
                () -> RenderUtil.roundedRectangle(x, y, width, height, 5, new Color(0, 0, 0, 255)));
        RenderUtil.roundedRectangle(x, y, width, height, 5, new Color(0, 0, 0, 50));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        bezier.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        bezier.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void onClose() {
        bezier.onClose();
        super.onClose();
    }
}