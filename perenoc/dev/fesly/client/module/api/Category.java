package dev.fesly.client.module.api;

import lombok.Getter;

@Getter
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    MISC("Misc"),

    RENDER("Render"),
    DISPLAY("Display");

    private final String name;

    Category(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}

