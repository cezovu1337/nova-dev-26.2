package net.minecraftforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;

public class RenderHighlightEvent {
    public interface Callback {
        void render(SubmitNodeCollector var1, PoseStack var2, LevelRenderState var3);
    }
}
