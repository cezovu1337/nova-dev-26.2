package dev.fesly.impl.value.clientValues;
public enum ClientUICategory {
    COLOR("Color", "icons/clientui/color_bg.png"), CLIENT("Client", "icons/clientui/client_bg.png");
    private final String name;
    private final String background;
    @java.lang.SuppressWarnings("all")
    private ClientUICategory(final String name, final String background) {
        this.name = name;
        this.background = background;
    }
    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }
    @java.lang.SuppressWarnings("all")
    public String getBackground() {
        return this.background;
    }
}
