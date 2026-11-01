package dev.fesly.impl.client.screen.clickui.impl;
import dev.fesly.Nova;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.util.math.MathUtil;
import java.awt.*;
public final class UIColorUtil {
    public static UIColors colorEnum = UIColors.Exquisite_Blue;
    public static UIColors nowColorEnum = UIColors.Exquisite_Blue;
    public static void updateColors() {
        nowColorEnum = getFullColorEnum();
        int red = colorEnum.backgroundColor.getRed();
        int green = colorEnum.backgroundColor.getGreen();
        int blue = colorEnum.backgroundColor.getBlue();
        int red2 = colorEnum.backgroundColor2.getRed();
        int green2 = colorEnum.backgroundColor2.getGreen();
        int blue2 = colorEnum.backgroundColor2.getBlue();
        int red3 = colorEnum.color1N.getRed();
        int green3 = colorEnum.color1N.getGreen();
        int blue3 = colorEnum.color1N.getBlue();
        int red4 = colorEnum.color2N.getRed();
        int green4 = colorEnum.color2N.getGreen();
        int blue4 = colorEnum.color2N.getBlue();
        red3 = MathUtil.lerp(red3, nowColorEnum.color1N.getRed(), 0.05F);
        green3 = MathUtil.lerp(green3, nowColorEnum.color1N.getGreen(), 0.05F);
        blue3 = MathUtil.lerp(blue3, nowColorEnum.color1N.getBlue(), 0.05F);
        red4 = MathUtil.lerp(red4, nowColorEnum.color2N.getRed(), 0.05F);
        green4 = MathUtil.lerp(green4, nowColorEnum.color2N.getGreen(), 0.05F);
        blue4 = MathUtil.lerp(blue4, nowColorEnum.color2N.getBlue(), 0.05F);
        red = MathUtil.lerp(red, nowColorEnum.backgroundColor.getRed(), 0.05F);
        green = MathUtil.lerp(green, nowColorEnum.backgroundColor.getGreen(), 0.05F);
        blue = MathUtil.lerp(blue, nowColorEnum.backgroundColor.getBlue(), 0.05F);
        red2 = MathUtil.lerp(red2, nowColorEnum.backgroundColor2.getRed(), 0.05F);
        green2 = MathUtil.lerp(green2, nowColorEnum.backgroundColor2.getGreen(), 0.05F);
        blue2 = MathUtil.lerp(blue2, nowColorEnum.backgroundColor2.getBlue(), 0.05F);
        colorEnum.backgroundColor = new Color(red, green, blue, nowColorEnum.backgroundColor.getAlpha());
        colorEnum.backgroundColor2 = new Color(red2, green2, blue2, nowColorEnum.backgroundColor2.getAlpha());
        colorEnum.color1N = new Color(red3, green3, blue3, nowColorEnum.color1N.getAlpha());
        colorEnum.color2N = new Color(red4, green4, blue4, nowColorEnum.color2N.getAlpha());
    }
    private static UIColors getFullColorEnum() {
        ClientUI clientUI = Nova.getInstance().getClientUI();
        if (Nova.getInstance().getClientUI().getColorEnum() == ClientUI.ColorEnum.Exquisite_Blue) return UIColors.Exquisite_Blue;
         else if (Nova.getInstance().getClientUI().getColorEnum() == ClientUI.ColorEnum.Exquisite_Purple) return UIColors.Exquisite_Purple;
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Orange)) return UIColors.Exquisite_Orange;
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Blue_Purple)) {
            return UIColors.Blue_Purple;
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Lime)) {
            return UIColors.Exquisite_Lime;
        }
        return UIColors.Exquisite_Blue;
    }
    public static UIColors getColorEnum() {
        ClientUI clientUI = Nova.getInstance().getClientUI();
        if (Nova.getInstance().getClientUI().getColorEnum() == ClientUI.ColorEnum.Exquisite_Blue) return UIColors.Exquisite_Blue;
         else if (Nova.getInstance().getClientUI().getColorEnum() == ClientUI.ColorEnum.Exquisite_Purple) return UIColors.Exquisite_Purple;
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Orange)) return UIColors.Exquisite_Orange;
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Blue_Purple)) {
            return UIColors.Blue_Purple;
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Lime)) {
            return UIColors.Exquisite_Lime;
        }
        return UIColors.Exquisite_Blue;
    }
    public static Color getBackgroundColor() {
        return Color.WHITE;
    }
    @java.lang.SuppressWarnings("all")
    private UIColorUtil() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
