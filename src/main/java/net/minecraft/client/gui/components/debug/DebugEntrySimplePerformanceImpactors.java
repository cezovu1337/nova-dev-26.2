package net.minecraft.client.gui.components.debug;

import java.util.Locale;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.optifine.Config;

public class DebugEntrySimplePerformanceImpactors implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer displayer, Level serverOrClientLevel, LevelChunk clientChunk, LevelChunk serverChunk) {
        Minecraft minecraft = Minecraft.getInstance();
        Options options = minecraft.options;
        displayer.addLine(
            String.format(
                Locale.ROOT,
                "%s%sB: %d U: %d",
                options.improvedTransparency().get() ? "improved-transparency " : "",
                options.cloudStatus().get() == CloudStatus.OFF ? "" : (options.cloudStatus().get() == CloudStatus.FAST ? "fast-clouds " : "fancy-clouds "),
                options.biomeBlendRadius().get(),
                Config.getChunkUpdates()
            )
        );
        TextureFilteringMethod filteringMethod = options.textureFiltering().get();
        if (filteringMethod == TextureFilteringMethod.ANISOTROPIC) {
            displayer.addLine(String.format(Locale.ROOT, "Filtering: %s %dx", filteringMethod.caption().getString(), options.maxAnisotropyValue()));
        } else {
            displayer.addLine(String.format(Locale.ROOT, "Filtering: %s", filteringMethod.caption().getString()));
        }
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }
}
