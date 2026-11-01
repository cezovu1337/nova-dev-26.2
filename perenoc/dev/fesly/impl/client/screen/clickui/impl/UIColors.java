package dev.fesly.impl.client.screen.clickui.impl;

import dev.fesly.impl.util.color.Colors;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@Getter
@AllArgsConstructor
public enum UIColors {
    Exquisite_Blue(new Color(92, 119, 188, 163), new Color(149, 172, 228, 107),

            new Color(92, 119, 188), new Color(149, 172, 228),

            new Color(0xB5BEEC), new Color(0x94ABEA)
            ),
    Exquisite_Purple(new Color(124, 92, 188, 163), new Color(169, 149, 228, 107),

            new Color(124, 92, 188), new Color(169, 149, 228),

            new Color(0xC7B5EC), new Color(0xB694EA)
    ),
    Exquisite_Orange(new Color(188, 172, 92, 163), new Color(228, 191, 149, 107),

            new Color(188, 150, 92), new Color(228, 206, 149),

            new Color(0xECDCB5), new Color(0xEABF94)
    ),
    Exquisite_Lime(new Color(95, 188, 92, 163), new Color(149, 228, 150, 107),

            new Color(92, 188, 97), new Color(149, 228, 162),

            new Color(0xB5ECC2), new Color(0x94EAA8)
    ),
    Blue_Purple(new Color(92, 119, 188, 163), new Color(171, 149, 228, 107),

            new Color(119, 92, 188), new Color(149, 171, 228),

            new Color(0xC5B5EC), new Color(0x94ABEA)
    ),

    ;




    Color backgroundColor;
    Color backgroundColor2;
    Color color1N;
    Color color2N;
    Color settingBackgroundColor;
    Color settingBackgroundColor2;

}
