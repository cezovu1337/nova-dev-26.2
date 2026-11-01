package com.mojang.blaze3d.opengl;

import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.util.StringUtil;
import org.lwjgl.opengl.EXTDebugLabel;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.KHRDebug;
import org.slf4j.Logger;

public abstract class GlDebugLabel {
    private static final Logger LOGGER = LogUtils.getLogger();

    public void applyLabel(GlBuffer buffer, Supplier<String> label) {
    }

    public void applyLabel(GlTexture texture) {
    }

    public void applyLabel(GlShaderModule shaderModule) {
    }

    public void applyLabel(GlProgram program) {
    }

    public void applyLabel(VertexArrayCache.VertexArray vertexArray) {
    }

    public void pushDebugGroup(Supplier<String> label) {
    }

    public void popDebugGroup() {
    }

    public void applyLabelBuffer(int id, String labelIn) {
    }

    public void applyLabelTexture(int id, String labelIn) {
    }

    public void applyLabelShader(int id, String labelIn) {
    }

    public void applyLabelProgram(int id, String labelIn) {
    }

    public void applyLabelVertexArray(int id, String labelIn) {
    }

    public void applyLabelFramebuffer(int id, String labelIn) {
    }

    public static GlDebugLabel create(GLCapabilities caps, boolean wantsLabels, Set<String> enabledExtensions) {
        if (wantsLabels) {
            if (caps.GL_KHR_debug && GlDevice.USE_GL_KHR_debug) {
                enabledExtensions.add("GL_KHR_debug");
                return new GlDebugLabel.Core();
            }

            if (caps.GL_EXT_debug_label && GlDevice.USE_GL_EXT_debug_label) {
                enabledExtensions.add("GL_EXT_debug_label");
                return new GlDebugLabel.Ext();
            }

            LOGGER.warn("Debug labels unavailable: neither KHR_debug nor EXT_debug_label are supported");
        }

        return new GlDebugLabel.Empty();
    }

    public boolean exists() {
        return false;
    }

    private static class Core extends GlDebugLabel {
        private final int maxLabelLength = GL33C.glGetInteger(33512);

        @Override
        public void applyLabel(GlBuffer buffer, Supplier<String> label) {
            if (label != null) {
                String labelStr = "Buf: " + label.get();
                KHRDebug.glObjectLabel(33504, buffer.handle(), StringUtil.truncateStringIfNecessary(labelStr, this.maxLabelLength, true));
            }
        }

        @Override
        public void applyLabel(GlTexture texture) {
            String label = "Tex: " + texture.getLabel();
            KHRDebug.glObjectLabel(5890, texture.id, StringUtil.truncateStringIfNecessary(label, this.maxLabelLength, true));
        }

        @Override
        public void applyLabel(GlShaderModule shaderModule) {
            String label = "Sh: " + shaderModule.getDebugLabel();
            KHRDebug.glObjectLabel(33505, shaderModule.getShaderId(), StringUtil.truncateStringIfNecessary(label, this.maxLabelLength, true));
        }

        @Override
        public void applyLabel(GlProgram program) {
            String label = "Prog: " + program.getDebugLabel();
            KHRDebug.glObjectLabel(33506, program.getProgramId(), StringUtil.truncateStringIfNecessary(label, this.maxLabelLength, true));
        }

        @Override
        public void applyLabel(VertexArrayCache.VertexArray vertexArray) {
            String label = "VA: " + vertexArray.formatName;
            KHRDebug.glObjectLabel(32884, vertexArray.id, StringUtil.truncateStringIfNecessary(label, this.maxLabelLength, true));
        }

        @Override
        public void pushDebugGroup(Supplier<String> label) {
            KHRDebug.glPushDebugGroup(33354, 0, label.get());
        }

        @Override
        public void popDebugGroup() {
            KHRDebug.glPopDebugGroup();
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public void applyLabelBuffer(int id, String labelIn) {
            String label = "Buf: " + labelIn;
            KHRDebug.glObjectLabel(33504, id, StringUtil.truncateStringIfNecessary(label, this.maxLabelLength, true));
        }

        @Override
        public void applyLabelTexture(int id, String labelIn) {
            String label = "Tex: " + labelIn;
            KHRDebug.glObjectLabel(5890, id, StringUtil.truncateStringIfNecessary(label, this.maxLabelLength, true));
        }

        @Override
        public void applyLabelShader(int id, String labelIn) {
            String label = "Sh: " + labelIn;
            KHRDebug.glObjectLabel(33505, id, StringUtil.truncateStringIfNecessary(label, this.maxLabelLength, true));
        }

        @Override
        public void applyLabelProgram(int id, String labelIn) {
            String label = "Prog: " + labelIn;
            KHRDebug.glObjectLabel(33506, id, StringUtil.truncateStringIfNecessary(label, this.maxLabelLength, true));
        }

        @Override
        public void applyLabelVertexArray(int id, String labelIn) {
            String label = "VA: " + labelIn;
            KHRDebug.glObjectLabel(32884, id, StringUtil.truncateStringIfNecessary(label, this.maxLabelLength, true));
        }

        @Override
        public void applyLabelFramebuffer(int id, String labelIn) {
            String label = "FB: " + labelIn;
            KHRDebug.glObjectLabel(36160, id, StringUtil.truncateStringIfNecessary(label, this.maxLabelLength, true));
        }
    }

    private static class Empty extends GlDebugLabel {
    }

    private static class Ext extends GlDebugLabel {
        @Override
        public void applyLabel(GlBuffer buffer, Supplier<String> label) {
            if (label != null) {
                String labelStr = "Buf: " + label.get();
                EXTDebugLabel.glLabelObjectEXT(37201, buffer.handle(), StringUtil.truncateStringIfNecessary(labelStr, 256, true));
            }
        }

        @Override
        public void applyLabel(GlTexture texture) {
            String label = "Tex: " + texture.getLabel();
            EXTDebugLabel.glLabelObjectEXT(5890, texture.id, StringUtil.truncateStringIfNecessary(label, 256, true));
        }

        @Override
        public void applyLabel(GlShaderModule shaderModule) {
            String label = "Sh: " + shaderModule.getDebugLabel();
            EXTDebugLabel.glLabelObjectEXT(35656, shaderModule.getShaderId(), StringUtil.truncateStringIfNecessary(label, 256, true));
        }

        @Override
        public void applyLabel(GlProgram program) {
            String label = "Prog: " + program.getDebugLabel();
            EXTDebugLabel.glLabelObjectEXT(35648, program.getProgramId(), StringUtil.truncateStringIfNecessary(label, 256, true));
        }

        @Override
        public void applyLabel(VertexArrayCache.VertexArray vertexArray) {
            String label = "VA: " + vertexArray.formatName;
            EXTDebugLabel.glLabelObjectEXT(32884, vertexArray.id, StringUtil.truncateStringIfNecessary(label, 256, true));
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public void applyLabelBuffer(int id, String labelIn) {
            String label = "Buf: " + labelIn;
            EXTDebugLabel.glLabelObjectEXT(37201, id, StringUtil.truncateStringIfNecessary(label, 256, true));
        }

        @Override
        public void applyLabelTexture(int id, String labelIn) {
            String label = "Tex: " + labelIn;
            EXTDebugLabel.glLabelObjectEXT(5890, id, StringUtil.truncateStringIfNecessary(label, 256, true));
        }

        @Override
        public void applyLabelProgram(int id, String labelIn) {
            String label = "Prog: " + labelIn;
            EXTDebugLabel.glLabelObjectEXT(35648, id, StringUtil.truncateStringIfNecessary(label, 256, true));
        }

        @Override
        public void applyLabelVertexArray(int id, String labelIn) {
            String label = "VA: " + labelIn;
            EXTDebugLabel.glLabelObjectEXT(32884, id, StringUtil.truncateStringIfNecessary(label, 256, true));
        }

        @Override
        public void applyLabelFramebuffer(int id, String labelIn) {
            String label = "FB: " + labelIn;
            EXTDebugLabel.glLabelObjectEXT(36160, id, StringUtil.truncateStringIfNecessary(label, 256, true));
        }
    }
}
