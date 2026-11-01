package dev.fesly.client.screen.clickgui;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScrollUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import org.joml.Vector2d;
import java.awt.*;
public class ClickGuiScreen extends Screen implements IAccess {
    public Category category = Category.COMBAT;
    public Category[] categories;
    public ScrollUtil scrollUtil = new ScrollUtil();
    public ClickGuiScreen(ITextComponent titleIn) {
        super(titleIn);
        categories = Category.values();
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        double width = 540;
        double height = 320;
        double x = mw.getScaledWidth() / 2F - width / 2F;
        double y = mw.getScaledHeight() / 2F - height / 2F;
        RenderUtil.glowRoundedRectangle(matrixStack, x, y, width, height, 5, new Color(0, 0, 0, 128), 3, 1);
        Fonts.ROBOTO_MEDIUM.get(14).draw(matrixStack, Nova.getInstance().getClientInfo().getName(), x + 5, y + 5, -1);
        int offset = 60;
        Font font = Fonts.ROBOTO_SEMI.get(14);
        for (Category category : categories) {
            RenderUtil.glowRoundedRectangle(matrixStack, x + 5 + (5 / 2F), y - (5 / 2F) + offset, font.getWidth(category.getName()) + 5 + (5 / 2F), font.getHeight() + 5, 2, ColorUtil.withAlpha(getTheme().getFadeTheme(5, offset * 5), 100), 3, 1);
            font.draw(matrixStack, category.getName(), x + 10, y + offset, new Color(255, 255, 255, 255).getRGB());
            offset += 15;
        }
        double padding = 10;
        double positionY = y + padding + scrollUtil.getScroll();
        int iheight = 0;
        for (Module module : nova.getModuleManager().get(category)) {
            Fonts.ROBOTO_MEDIUM.get(16).draw(matrixStack, module.getName(), x + 100, positionY, -1);
            positionY += 15;
            iheight += 15;
        }
        double scrollX = x + width - 4;
        double scrollY = y + padding;
        scrollUtil.renderScrollBar(new Vector2d(scrollX, scrollY), height - padding * 2);
        scrollUtil.setMax(-iheight + height - padding * 2);
    }
}
