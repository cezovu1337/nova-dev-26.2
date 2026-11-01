package dev.fesly.font;
import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
public enum Fonts {
    MANROPE_BOLD("manrope/manrope_bold.ttf"), MANROPE_LIGHT("manrope/manrope_light.ttf"), MANROPE_MEDIUM("manrope/manrope_medium.ttf"), MANROPE_REGULAR("manrope/manrope_regular.ttf"), MANROPE_SEMI("manrope/manrope_semi.ttf"), COMFORTAA_BOLD("comfortaa/comfortaa_bold.ttf"), COMFORTAA_LIGHT("comfortaa/comfortaa_light.ttf"), COMFORTAA_MEDIUM("comfortaa/comfortaa_medium.ttf"), COMFORTAA_REGULAR("comfortaa/comfortaa_regular.ttf"), COMFORTAA_SEMIBOLD("comfortaa/comfortaa_semibold.ttf"), ULTRABOLD("other/tt.ttf"),
    SFUI_BOLD("sfui/sfuidisplay_bold.ttf"), SFUI_HEAVY("comfortaa/sfuidisplay_heavy.ttff"), OPENSANS_BOLD("opensans/opensans_bold.ttf"), OPENSANS_LIGHT("opensans/opensans_light.ttf"), OPENSANS_MEDIUM("opensans/opensans_medium.ttf"), OPENSANS_REGULAR("opensans/opensans_regular.ttf"), OPENSANS_SEMI("opensans/opensans_semi.ttf"), ROBOTO_BOLD("roboto/roboto_bold.ttf"), ROBOTO_LIGHT("roboto/roboto_light.ttf"), ROBOTO_MEDIUM("roboto/roboto_medium.ttf"), ROBOTO_REGULAR("roboto/roboto_regular.ttf"), ROBOTO_SEMI("roboto/roboto_semi.ttf");
    private final String file;
    private final Float2ObjectMap<Font> fontMap = new Float2ObjectArrayMap<>();
    public Font get(float size) {
        return fontMap.computeIfAbsent(size, font -> {
            try {
                return Font.create(getFile(), size, false, false, false);
            } catch (Exception e) {
                throw new RuntimeException("Unable to load font: " + this, e);
            }
        });
    }
    @java.lang.SuppressWarnings("all")
    private Fonts(final String file) {
        this.file = file;
    }
    @java.lang.SuppressWarnings("all")
    public String getFile() {
        return this.file;
    }
    @java.lang.SuppressWarnings("all")
    public Float2ObjectMap<Font> getFontMap() {
        return this.fontMap;
    }
}
