package dev.fesly.client.theme;
public final class ThemeManager {
    private Themes theme = Themes.WATER;
    @java.lang.SuppressWarnings("all")
    public void setTheme(final Themes theme) {
        this.theme = theme;
    }
    @java.lang.SuppressWarnings("all")
    public Themes getTheme() {
        return this.theme;
    }
}
