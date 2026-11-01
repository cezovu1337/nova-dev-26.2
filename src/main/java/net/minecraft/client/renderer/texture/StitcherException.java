package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Locale;

public class StitcherException extends RuntimeException {
    private final Collection<Stitcher.Entry> allSprites;

    public StitcherException(Stitcher.Entry sprite, Collection<Stitcher.Entry> allSprites) {
        super(
            String.format(
                Locale.ROOT, "Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", sprite.name(), sprite.width(), sprite.height()
            )
        );
        this.allSprites = allSprites;
    }

    public Collection<Stitcher.Entry> getAllSprites() {
        return this.allSprites;
    }

    public StitcherException(Stitcher.Entry entryIn, Collection<Stitcher.Entry> entriesIn, int atlasWidth, int atlasHeight, int maxWidth, int maxHeight) {
        super(
            String.format(
                Locale.ROOT,
                "Unable to fit: %s - size: %dx%d, atlas: %dx%d, atlasMax: %dx%d - Maybe try a lower resolution resourcepack?",
                entryIn.name() + "",
                entryIn.width(),
                entryIn.height(),
                atlasWidth,
                atlasHeight,
                maxWidth,
                maxHeight
            )
        );
        this.allSprites = entriesIn;
    }
}
