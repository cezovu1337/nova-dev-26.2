package dev.fesly.impl.util.color;

import dev.fesly.Nova;
import lombok.Getter;

import java.awt.*;

@Getter
public enum Colors {
    BG_COLOR(new Color(255, 255, 255, 97)),
    WHITE(new Color(-1)),
    ASTOLFO(ColorUtil.skyRainbow((int) Nova.getInstance().getClientUI().getColorSpeed(), (int) (System.currentTimeMillis() / System.nanoTime()))),
    ASTOLFO_TWO(ColorUtil.skyRainbow((int) Nova.getInstance().getClientUI().getColorSpeed(), (int) (System.currentTimeMillis() / System.nanoTime()) / 2)),

    My_Blue(new Color(52, 205, 218)),
    My_Two_Blue(new Color(0x5987E5)),

    My_Orange(new Color(0xD5B469)),
    My_Two_Orange(new Color(188, 150, 92)),

    Dark_TextCol(new Color(0x525658).darker()),
    White_TextCol(new Color(0xD0FFFFFF, true)),
    White(new Color(0xFFFFFFFF, true)),

    Exquisite_Blue(new Color(0x2CEFFF)),
    Client_Purple(new Color(0x867CD5)),
    Exquisite_Purple(new Color(0x946DB6)),

    Client_Blue(new Color(0x2556BB)),
    My_Lime(new Color(0xA7FB62)),
    My_Two_Lime(new Color(0x58BA10).darker()),

    RNB_ONE(new Color(0x6932FF)),
    RNB_TWO(new Color(0xFF263C)),
    RNB_THREE(new Color(0xFF8008)),
    RNB_FOUR(new Color(0x0052FF)),





    ;

    private final Color color;

    Colors(Color color) {
        this.color = color;

    }

    public Color getColor() {
        return color;
    }

}

