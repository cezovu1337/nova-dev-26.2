package dev.fesly.impl.client.screen.clickui.impl;
import dev.fesly.impl.util.color.Colors;
import java.awt.*;
public enum UIColors {
    Exquisite_Blue(new Color(92, 119, 188, 163), new Color(149, 172, 228, 107), new Color(92, 119, 188), new Color(149, 172, 228), new Color(11910892), new Color(9743338)), Exquisite_Purple(new Color(124, 92, 188, 163), new Color(169, 149, 228, 107), new Color(124, 92, 188), new Color(169, 149, 228), new Color(13088236), new Color(11965674)), Exquisite_Orange(new Color(188, 172, 92, 163), new Color(228, 191, 149, 107), new Color(188, 150, 92), new Color(228, 206, 149), new Color(15522997), new Color(15384468)), Exquisite_Lime(new Color(95, 188, 92, 163), new Color(149, 228, 150, 107), new Color(92, 188, 97), new Color(149, 228, 162), new Color(11922626), new Color(9759400)), Blue_Purple(new Color(92, 119, 188, 163), new Color(171, 149, 228, 107), new Color(119, 92, 188), new Color(149, 171, 228), new Color(12957164), new Color(9743338));
    Color backgroundColor;
    Color backgroundColor2;
    Color color1N;
    Color color2N;
    Color settingBackgroundColor;
    Color settingBackgroundColor2;
    @java.lang.SuppressWarnings("all")
    public Color getBackgroundColor() {
        return this.backgroundColor;
    }
    @java.lang.SuppressWarnings("all")
    public Color getBackgroundColor2() {
        return this.backgroundColor2;
    }
    @java.lang.SuppressWarnings("all")
    public Color getColor1N() {
        return this.color1N;
    }
    @java.lang.SuppressWarnings("all")
    public Color getColor2N() {
        return this.color2N;
    }
    @java.lang.SuppressWarnings("all")
    public Color getSettingBackgroundColor() {
        return this.settingBackgroundColor;
    }
    @java.lang.SuppressWarnings("all")
    public Color getSettingBackgroundColor2() {
        return this.settingBackgroundColor2;
    }
    @java.lang.SuppressWarnings("all")
    private UIColors(final Color backgroundColor, final Color backgroundColor2, final Color color1N, final Color color2N, final Color settingBackgroundColor, final Color settingBackgroundColor2) {
        this.backgroundColor = backgroundColor;
        this.backgroundColor2 = backgroundColor2;
        this.color1N = color1N;
        this.color2N = color2N;
        this.settingBackgroundColor = settingBackgroundColor;
        this.settingBackgroundColor2 = settingBackgroundColor2;
    }
}
