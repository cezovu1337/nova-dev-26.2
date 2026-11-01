package dev.fesly.impl.value.clientValues;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Долбаеб (саня)
 */
@AllArgsConstructor
@Getter
public enum ClientUICategory {

    COLOR("Color", "icons/clientui/color_bg.png"),
    CLIENT("Client", "icons/clientui/client_bg.png");


    private final String name;
    private final String background;
}
