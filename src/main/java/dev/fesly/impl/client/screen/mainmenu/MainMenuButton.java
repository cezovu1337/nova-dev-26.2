package dev.fesly.impl.client.screen.mainmenu;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.render.RenderUtil;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.ITextComponent;
import java.awt.*;
public class MainMenuButton extends AbstractButton
{
    public static final MainMenuButton.ITooltip field_238486_s_ = (button, matrixStack, mouseX, mouseY) ->
    {
    };
    protected final MainMenuButton.IPressable onPress;
    protected final MainMenuButton.ITooltip onTooltip;
    public MainMenuButton(int x, int y, int width, int height, ITextComponent title, MainMenuButton.IPressable pressedAction)
    {
        this(x, y, width, height, title, pressedAction, field_238486_s_);
    }
    public MainMenuButton(int x, int y, int width, int height, ITextComponent title, MainMenuButton.IPressable pressedAction, MainMenuButton.ITooltip onTooltip)
    {
        super(x, y, width, height, title);
        this.onPress = pressedAction;
        this.onTooltip = onTooltip;
    }
    public void onPress()
    {
        this.onPress.onPress(this);
    }
    private final Animation alpha = new Animation(Easing.EASE_OUT_CUBIC, 350);
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (alpha.getValue() < 150) {
            alpha.setValue(248);
        }
        if (isHovered) {
            alpha.run(220);
        }else{
            alpha.run(248);
        }
        int v = alpha.getNumberValue().intValue();
        RenderUtil.applyBloom(3,1,() -> RenderUtil.roundedRectangle(x + 1.5f,y + 4,width - 3,height-6,5, new Color(31,31,31)));
        RenderUtil.roundedRectangle(x,y,width,height,5, new Color(v, v, v));
        Font font =   Fonts.ROBOTO_REGULAR.get(18);
        font.drawCenterXY(getMessage().getString(), x + width / 2f, y +
                height / 2f,Color.decode("#222222").getRGB());
    }
    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        this.onTooltip.onTooltip(this, matrixStack, mouseX, mouseY);
    }
    public interface IPressable
    {
        void onPress(MainMenuButton p_onPress_1_);
    }
    public interface ITooltip
    {
        void onTooltip(MainMenuButton p_onTooltip_1_, MatrixStack p_onTooltip_2_, int p_onTooltip_3_, int p_onTooltip_4_);
    }
}
