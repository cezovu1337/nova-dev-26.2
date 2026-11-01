package net.optifine.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderTypeBuffers;

public class RenderUtils
{
    private static boolean flushRenderBuffers = true;
    private static Minecraft mc = Minecraft.getInstance();

    public static boolean setFlushRenderBuffers(boolean flushRenderBuffers)
    {
        boolean flag = RenderUtils.flushRenderBuffers;
        RenderUtils.flushRenderBuffers = flushRenderBuffers;
        return flag;
    }
    public static double interporate(double p_219803_0_, double p_219803_2_, double p_219803_4_) {
        return p_219803_2_ + p_219803_0_ * (p_219803_4_ - p_219803_2_);
    }

    public static boolean isFlushRenderBuffers()
    {
        return flushRenderBuffers;
    }

    public static void flushRenderBuffers()
    {
        if (flushRenderBuffers)
        {
            RenderTypeBuffers rendertypebuffers = mc.getRenderTypeBuffers();
            rendertypebuffers.getBufferSource().flushRenderBuffers();
            rendertypebuffers.getCrumblingBufferSource().flushRenderBuffers();
        }
    }
}
