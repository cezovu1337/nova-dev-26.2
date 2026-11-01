package dev.fesly.client.screen.clientmenu.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.client.screen.IScreen;
import dev.fesly.font.Fonts;
import dev.fesly.impl.interfaces.client.IAccess;
import lombok.Data;
import lombok.val;
import org.joml.Vector2d;

@Data
public class CategoryScreen implements IScreen, IAccess {
    public Vector2d position;
    public Vector2d size;

    @Override
    public void init() {

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        val clientMenu = nova.getClientMenu();
        position = clientMenu.position;
        size = clientMenu.size;

        double x = position.x;
        double y = position.y;
        double width = size.x;
        double height = size.y;

        Fonts.ROBOTO_SEMI.get(20).draw(matrixStack, "Settings", x + 10, y + 10, -1);


    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {

    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void update() {

    }

    @Override
    public void bloom() {

    }

    @Override
    public void onClose() {

    }
}
