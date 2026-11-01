package dev.fesly.impl.client.clientui;
import ca.weblite.objc.Client;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.Nova;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.screen.clickui.impl.UIColorUtil;
import dev.fesly.impl.client.screen.clickui.impl.UIColors;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScrollUtil;
import dev.fesly.impl.value.clientValues.ClientSetting;
import dev.fesly.impl.value.clientValues.ClientUICategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.util.List;
import static dev.fesly.impl.client.clientui.ClientUiScreen.alphaAnim;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glScissor;
public class ClientCategoryWidget extends Widget implements IAccess {
    public static int WIDTH = 140;
    public static int HEIGHT = 182;
    private final ClientUICategory category;
    public boolean directionOpen;
    private int scrollValue = 0;
    private int scrollAnim = 0;
    public ClientCategoryWidget(ClientUICategory category, int x, int y) {
        super(x, y, WIDTH, HEIGHT, NarratorChatListener.EMPTY);
        this.category = category;
    }
    public static void scissorRect(float x, float y, float width, double height) {
        Minecraft mc = Minecraft.getInstance();
        int dwidth = mw.getFramebufferWidth();
        int dheight = mw.getScaledHeight();
        int factor = (int) mw.getScaleFactor();
        glScissor((int) (x * (float) factor), (int) (((double) dheight - height) * (double) factor), (int) ((width - x) * (float) factor), (int) ((height - (double) y) * (double) factor));
    }
    ScrollUtil scrollUtil = new ScrollUtil();
    private float lastTranslateY = 0;
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderWidget(matrixStack, mouseX, mouseY, partialTicks, 0);
    }
    public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, float translateY) {
        this.lastTranslateY = translateY;
        float y = this.y + translateY;
        this.scrollAnim = (int) MathUtil.lerp(this.scrollAnim, scrollValue, 0.25F);
        drawWithOutlineDefault(x, y, WIDTH, HEIGHT, 6, true, (Math.max(0, alphaAnim - 50)));
        {
            {
                String pathIn = "icons/clientui/" + category.getName().toLowerCase() + ".png";
                RenderUtil.drawImage(new ResourceLocation("nova", pathIn), x + 5, y + 4, 12, 12, new Color(255, 255, 255, alphaAnim - 60).getRGB());
            }
            {
                Fonts.ROBOTO_REGULAR.get(15.0F).draw(category.getName() + " Settings", x + 21, y + 5 + Fonts.ROBOTO_MEDIUM.get(15.0F).getMiddleOfBox(12), new Color(255, 255, 255, alphaAnim).getRGB());
            }
            {
            }
        }
        {
            List<ClientSetting> settings = Nova.getInstance().getClientUI().getSettings(category);
            int yB = (int) (y + 25 + scrollAnim);
            float offset = 0;
            for (ClientSetting setting : settings) {
                setting.visibleAnimation.run(setting.getVisible().getAsBoolean() ? 1 : 0);
                if (!setting.getVisible().getAsBoolean()) continue;
                GL11.glEnable(GL_SCISSOR_TEST);
                scissorRect(x, y + 19, x + WIDTH, y + HEIGHT);
                setting.draw(this.x + 4, (int) (yB + offset), WIDTH - 8, mouseX, mouseY, partialTicks);
                GL11.glDisable(GL_SCISSOR_TEST);
                offset += setting.getHeight();
            }
        }
        scrollUtil.setMax(-1000);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float translateY = this.lastTranslateY;
        float actualY = this.y + translateY;
        if (MathUtil.isHovered(x, actualY, WIDTH, HEIGHT, mouseX, mouseY)) {
            scrollValue += (int) (delta * 21);
            scrollValue = Math.min(0, scrollValue);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    public static void drawWithOutlineDefault(float rectX, float rectY, float rectWidth, float rectHeight, float radius, boolean outline, int alpha) {
        UIColors colorEnum = UIColorUtil.getColorEnum();
        Color c1 = colorEnum.getBackgroundColor();
        Color c2 = colorEnum.getBackgroundColor2();
        Color color1;
        Color color2;
        if (alpha == 365) {
            color1 = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), 255);
            color2 = new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), 255);
        } else {
            color1 = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), (int) (163 * (alpha / 255.0F)));
            color2 = new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), (int) (107 * (alpha / 255.0F)));
        }
        if (outline) {
            RenderUtil.roundedOutlineGradientRectangle(rectX, rectY, rectWidth, rectHeight, radius, 0.6F, color1.brighter().brighter(), color1.brighter().brighter(), color2.brighter().brighter(), color2.brighter().brighter());
        }
        RenderUtil.roundedGradientRectangle(rectX + 0.5F, rectY + 0.5F, rectWidth - 1, rectHeight - 1, radius, color1, color1, color2, color2);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<ClientSetting> settings = Nova.getInstance().getClientUI().getSettings(category);
        float translateY = this.lastTranslateY;
        float actualY = this.y + translateY;
        int y = (int) (actualY + 25 + scrollAnim);
        float offset = 0;
        if (MathUtil.isHovered(this.x, actualY, WIDTH, HEIGHT, mouseX, mouseY)) {
            for (ClientSetting setting : settings) {
                if (!setting.getVisible().getAsBoolean()) continue;
                float currentOffset = offset;
                float currentHeight = setting.getHeight();
                offset += currentHeight;
                if (mouseY >= actualY + 19 && mouseY <= actualY + HEIGHT) {
                    if (MathUtil.isHovered(this.x + 4, y + currentOffset, WIDTH - 8, currentHeight, mouseX, mouseY)) {
                        setting.mousePressed(this.x + 4, (int) (y + currentOffset), WIDTH - 8, (int) mouseX, (int) mouseY, button);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        List<ClientSetting> settings = Nova.getInstance().getClientUI().getSettings(category);
        float translateY = this.lastTranslateY;
        float actualY = this.y + translateY;
        int y = (int) (actualY + 25 + scrollAnim);
        float offset = 0;
        for (ClientSetting setting : settings) {
            if (!setting.getVisible().getAsBoolean()) continue;
            setting.mouseReleased(this.x + 4, (int) (y + offset), (int) mouseX, (int) mouseY, button);
            offset += setting.getHeight();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    @java.lang.SuppressWarnings("all")
    public void setDirectionOpen(final boolean directionOpen) {
        this.directionOpen = directionOpen;
    }
    @java.lang.SuppressWarnings("all")
    public void setScrollAnim(final int scrollAnim) {
        this.scrollAnim = scrollAnim;
    }
    @java.lang.SuppressWarnings("all")
    public void setScrollUtil(final ScrollUtil scrollUtil) {
        this.scrollUtil = scrollUtil;
    }
    @java.lang.SuppressWarnings("all")
    public int getScrollValue() {
        return this.scrollValue;
    }
    @java.lang.SuppressWarnings("all")
    public void setScrollValue(final int scrollValue) {
        this.scrollValue = scrollValue;
    }
}
