package dev.fesly.client.screen;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.impl.interfaces.client.IAccess;
public interface IScreen extends IAccess {
    void init();
    void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
    void mouseClicked(double mouseX, double mouseY, int button);
    void mouseReleased(double mouseX, double mouseY, int button);
    void keyTyped(char typedChar, int keyCode);
    void update();
    void bloom();
    void onClose();
}
