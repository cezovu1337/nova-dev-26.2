package dev.fesly.impl.util.color;
import dev.fesly.Nova;
import java.awt.*;
public enum Colors {
    BG_COLOR(new Color(255, 255, 255, 97)), WHITE(new Color(-1)), ASTOLFO(ColorUtil.skyRainbow((int) Nova.getInstance().getClientUI().getColorSpeed(), (int) (System.currentTimeMillis() / System.nanoTime()))), ASTOLFO_TWO(ColorUtil.skyRainbow((int) Nova.getInstance().getClientUI().getColorSpeed(), (int) (System.currentTimeMillis() / System.nanoTime()) / 2)), My_Blue(new Color(52, 205, 218)), My_Two_Blue(new Color(5867493)), My_Orange(new Color(14005353)), My_Two_Orange(new Color(188, 150, 92)), Dark_TextCol(new Color(5396056).darker()), White_TextCol(new Color(-788529153, true)), White(new Color(-1, true)), Exquisite_Blue(new Color(2945023)), Client_Purple(new Color(8813781)), Exquisite_Purple(new Color(9727414)), Client_Blue(new Color(2447035)), My_Lime(new Color(11008866)), My_Two_Lime(new Color(5814800).darker()), RNB_ONE(new Color(6894335)), RNB_TWO(new Color(16721468)), RNB_THREE(new Color(16744456)), RNB_FOUR(new Color(21247));
    private final Color color;
    Colors(Color color) {
        this.color = color;
    }
    public Color getColor() {
        return color;
    }
}
