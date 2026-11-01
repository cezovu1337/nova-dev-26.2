package dev.fesly.impl.client;
public enum Release {
    PUBLIC("Public"), BETA("Beta"), DEV("Dev");
    private final String name;
    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }
    @java.lang.SuppressWarnings("all")
    private Release(final String name) {
        this.name = name;
    }
}
