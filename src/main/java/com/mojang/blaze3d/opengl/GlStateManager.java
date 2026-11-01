package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import net.optifine.Config;
import net.optifine.SmartAnimations;
import net.optifine.render.GlAlphaState;
import net.optifine.render.GlBlendState;
import net.optifine.render.GlCullState;
import net.optifine.render.GlDepthState;
import net.optifine.render.GlScissorState;
import net.optifine.render.RenderUtils;
import net.optifine.shaders.GlState;
import net.optifine.shaders.Shaders;
import net.optifine.util.LockCounter;
import org.joml.Vector4fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.ARBDrawBuffersBlend;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class GlStateManager {
    private static final Plot PLOT_TEXTURES = TracyClient.createPlot("GPU Textures");
    private static int numTextures = 0;
    private static final Plot PLOT_BUFFERS = TracyClient.createPlot("GPU Buffers");
    private static int numBuffers = 0;
    private static final GlStateManager.BlendState[] BLEND = new GlStateManager.BlendState[8];
    private static final GlStateManager.DepthState DEPTH = new GlStateManager.DepthState();
    private static final GlStateManager.CullState CULL = new GlStateManager.CullState();
    private static final GlStateManager.PolygonOffsetState POLY_OFFSET = new GlStateManager.PolygonOffsetState();
    private static final GlStateManager.ColorLogicState COLOR_LOGIC = new GlStateManager.ColorLogicState();
    private static final GlStateManager.ScissorState SCISSOR = new GlStateManager.ScissorState();
    private static int activeTexture;
    private static final int TEXTURE_COUNT = 12;
    private static final GlStateManager.TextureState[] TEXTURES = IntStream.range(0, 32)
        .mapToObj(i -> new GlStateManager.TextureState())
        .toArray(GlStateManager.TextureState[]::new);
    private static final @ColorTargetState.WriteMask int[] COLOR_MASK = new int[8];
    private static int readFbo;
    private static int writeFbo;
    private static boolean alphaTest = false;
    private static int alphaTestFunc = 519;
    private static float alphaTestRef = 0.0F;
    private static LockCounter alphaLock = new LockCounter();
    private static GlAlphaState alphaLockState = new GlAlphaState();
    private static LockCounter blendLock = new LockCounter();
    private static GlBlendState blendLockState = new GlBlendState();
    private static LockCounter cullLock = new LockCounter();
    private static GlCullState cullLockState = new GlCullState();
    private static LockCounter depthLock = new LockCounter();
    private static GlDepthState depthLockState = new GlDepthState();
    private static LockCounter scissorLock = new LockCounter();
    private static GlScissorState scissorLockState = new GlScissorState();
    public static boolean vboRegions;
    public static int GL_COPY_READ_BUFFER;
    public static int GL_COPY_WRITE_BUFFER;
    public static int GL_ARRAY_BUFFER;
    public static int GL_STATIC_DRAW;
    public static final int GL_QUADS = 7;
    public static final int GL_TRIANGLES = 4;
    public static final int GL_TEXTURE0 = 33984;
    public static final int GL_TEXTURE1 = 33985;
    public static final int GL_TEXTURE2 = 33986;
    private static final int[] IMAGE_TEXTURES = new int[8];
    private static int glProgram = 0;
    public static float lastBrightnessX = 0.0F;
    public static float lastBrightnessY = 0.0F;
    private static final GlStateManager.BlendState BLEND_SINGLE = new GlStateManager.BlendState();

    public static void disableAlphaTest() {
        if (alphaLock.isLocked()) {
            alphaLockState.setDisabled();
        } else {
            alphaTest = false;
            applyAlphaTest();
        }
    }

    public static void enableAlphaTest() {
        if (alphaLock.isLocked()) {
            alphaLockState.setEnabled();
        } else {
            alphaTest = true;
            applyAlphaTest();
        }
    }

    public static void alphaFunc(int func, float ref) {
        if (alphaLock.isLocked()) {
            alphaLockState.setFuncRef(func, ref);
        } else {
            alphaTestFunc = func;
            alphaTestRef = ref;
            applyAlphaTest();
        }
    }

    public static void applyAlphaTest() {
        if (Config.isShaders()) {
            if (alphaTest && alphaTestFunc == 516) {
                Shaders.uniform_alphaTestRef.setValue(alphaTestRef);
            } else {
                Shaders.uniform_alphaTestRef.setValue(0.0F);
            }
        }
    }

    public static void _disableScissorTest() {
        RenderSystem.assertOnRenderThread();
        if (scissorLock.isLocked()) {
            scissorLockState.setEnabled(false);
        } else {
            SCISSOR.mode.disable();
        }
    }

    public static void _enableScissorTest() {
        RenderSystem.assertOnRenderThread();
        if (scissorLock.isLocked()) {
            scissorLockState.setEnabled(true);
        } else {
            SCISSOR.mode.enable();
        }
    }

    public static void _scissorBox(int x, int y, int width, int height) {
        RenderSystem.assertOnRenderThread();
        if (scissorLock.isLocked()) {
            scissorLockState.setBox(x, y, width, height);
        } else {
            GL33C.glScissor(x, y, width, height);
        }
    }

    public static void _disableDepthTest() {
        RenderSystem.assertOnRenderThread();
        if (depthLock.isLocked()) {
            depthLockState.setEnabled(false);
        } else {
            DEPTH.mode.disable();
        }
    }

    public static void _enableDepthTest() {
        RenderSystem.assertOnRenderThread();
        if (depthLock.isLocked()) {
            depthLockState.setEnabled(true);
        } else {
            DEPTH.mode.enable();
        }
    }

    public static void _depthFunc(int func) {
        RenderSystem.assertOnRenderThread();
        if (depthLock.isLocked()) {
            depthLockState.setFunc(func);
        } else {
            if (func != DEPTH.func) {
                DEPTH.func = func;
                GL33C.glDepthFunc(func);
            }
        }
    }

    public static void _depthMask(boolean mask) {
        RenderSystem.assertOnRenderThread();
        if (depthLock.isLocked()) {
            depthLockState.setMask(mask);
        } else {
            if (mask != DEPTH.mask) {
                DEPTH.mask = mask;
                GL33C.glDepthMask(mask);
            }
        }
    }

    public static void _disableBlend(int index) {
        RenderSystem.assertOnRenderThread();
        if (blendLock.isLocked()) {
            blendLockState.setDisabled();
        } else {
            BLEND[0].mode.disable();
        }
    }

    public static void _enableBlend(int index) {
        RenderSystem.assertOnRenderThread();
        if (blendLock.isLocked()) {
            blendLockState.setEnabled();
        } else {
            BLEND[0].mode.enable();
        }
    }

    public static void _disableBlend() {
        RenderSystem.assertOnRenderThread();
        if (blendLock.isLocked()) {
            blendLockState.setDisabled();
        } else {
            BLEND[0].mode.disable();
        }
    }

    public static void _enableBlend() {
        RenderSystem.assertOnRenderThread();
        if (blendLock.isLocked()) {
            blendLockState.setEnabled();
        } else {
            BLEND[0].mode.enable();
        }
    }

    public static void blendFunc(int srcFactor, int dstFactor) {
        RenderSystem.assertOnRenderThread();
        if (blendLock.isLocked()) {
            blendLockState.setFactors(srcFactor, dstFactor);
        } else {
            GlStateManager.BlendState blend = BLEND[0];
            if (srcFactor != blend.srcRgb || dstFactor != blend.dstRgb || srcFactor != blend.srcAlpha || dstFactor != blend.dstAlpha) {
                blend.srcRgb = srcFactor;
                blend.dstRgb = dstFactor;
                blend.srcAlpha = srcFactor;
                blend.dstAlpha = dstFactor;
                if (Config.isShaders()) {
                    Shaders.uniform_blendFunc.setValue(srcFactor, dstFactor, srcFactor, dstFactor);
                }

                GL11.glBlendFunc(srcFactor, dstFactor);
            }
        }
    }

    public static void _blendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
        RenderSystem.assertOnRenderThread();
        GlStateManager.BlendState firstBlend = BLEND[0];
        if (blendLock.isLocked()) {
            blendLockState.setFactors(srcRgb, dstRgb, srcAlpha, dstAlpha);
        } else {
            if (srcRgb != firstBlend.srcRgb || dstRgb != firstBlend.dstRgb || srcAlpha != firstBlend.srcAlpha || dstAlpha != firstBlend.dstAlpha) {
                firstBlend.srcRgb = srcRgb;
                firstBlend.dstRgb = dstRgb;
                firstBlend.srcAlpha = srcAlpha;
                firstBlend.dstAlpha = dstAlpha;
                if (Config.isShaders()) {
                    Shaders.uniform_blendFunc.setValue(srcRgb, dstRgb, srcAlpha, dstAlpha);
                }

                glBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
            }
        }
    }

    public static void _blendEquationSeparate(int modeRgb, int modeAlpha) {
        RenderSystem.assertOnRenderThread();
        GlStateManager.BlendState firstBlend = BLEND[0];
        if (modeRgb != firstBlend.modeRgb || modeAlpha != firstBlend.modeAlpha) {
            firstBlend.modeRgb = modeRgb;
            firstBlend.modeAlpha = modeAlpha;
            glBlendEquationSeparate(modeRgb, modeAlpha);
        }
    }

    public static void init(GLCapabilities glCapabilities) {
        RenderSystem.assertOnRenderThread();
        Config.initDisplay();
        GL_COPY_READ_BUFFER = 36662;
        GL_COPY_WRITE_BUFFER = 36663;
        GL_ARRAY_BUFFER = 34962;
        GL_STATIC_DRAW = 35044;
        boolean copyBuffer = true;
        boolean multiDrawArrays = true;
        vboRegions = copyBuffer && multiDrawArrays;
        if (!vboRegions) {
            List<String> list = new ArrayList<>();
            if (!copyBuffer) {
                list.add("OpenGL 1.3, ARB_copy_buffer");
            }

            if (!multiDrawArrays) {
                list.add("OpenGL 1.4");
            }

            String vboRegionWarn = "VboRegions not supported, missing: " + Config.listToString(list);
            Config.dbg(vboRegionWarn);
        }
    }

    public static int glGetProgrami(int program, int pname) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glGetProgrami(program, pname);
    }

    public static void glAttachShader(int program, int shader) {
        RenderSystem.assertOnRenderThread();
        GL33C.glAttachShader(program, shader);
    }

    public static void glDeleteShader(int shader) {
        RenderSystem.assertOnRenderThread();
        GL33C.glDeleteShader(shader);
    }

    public static int glCreateShader(int type) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glCreateShader(type);
    }

    public static void glShaderSource(int shader, String source) {
        RenderSystem.assertOnRenderThread();
        byte[] encoded = source.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = MemoryUtil.memAlloc(encoded.length + 1);
        buffer.put(encoded);
        buffer.put((byte)0);
        buffer.flip();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointers = stack.mallocPointer(1);
            pointers.put(buffer);
            GL33C.nglShaderSource(shader, 1, pointers.address0(), 0L);
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    public static void glCompileShader(int shader) {
        RenderSystem.assertOnRenderThread();
        GL33C.glCompileShader(shader);
    }

    public static int glGetShaderi(int shader, int pname) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glGetShaderi(shader, pname);
    }

    public static void _glUseProgram(int program) {
        RenderSystem.assertOnRenderThread();
        GL33C.glUseProgram(program);
        glProgram = program;
    }

    public static int glCreateProgram() {
        RenderSystem.assertOnRenderThread();
        return GL33C.glCreateProgram();
    }

    public static void glDeleteProgram(int program) {
        RenderSystem.assertOnRenderThread();
        GL33C.glDeleteProgram(program);
    }

    public static void glLinkProgram(int program) {
        RenderSystem.assertOnRenderThread();
        GL33C.glLinkProgram(program);
    }

    public static int _glGetUniformLocation(int program, CharSequence name) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glGetUniformLocation(program, name);
    }

    public static void _glUniform1i(int location, int v0) {
        RenderSystem.assertOnRenderThread();
        GL33C.glUniform1i(location, v0);
    }

    public static void _glBindAttribLocation(int program, int location, CharSequence name) {
        RenderSystem.assertOnRenderThread();
        GL33C.glBindAttribLocation(program, location, name);
    }

    static void incrementTrackedBuffers() {
        numBuffers++;
        PLOT_BUFFERS.setValue(numBuffers);
    }

    public static int _glGenBuffers() {
        RenderSystem.assertOnRenderThread();
        incrementTrackedBuffers();
        return GL33C.glGenBuffers();
    }

    public static int _glGenVertexArrays() {
        RenderSystem.assertOnRenderThread();
        return GL33C.glGenVertexArrays();
    }

    public static void _glBindBuffer(int target, int buffer) {
        RenderSystem.assertOnRenderThread();
        GL33C.glBindBuffer(target, buffer);
    }

    public static void _glBindVertexArray(int arrayId) {
        RenderSystem.assertOnRenderThread();
        GL33C.glBindVertexArray(arrayId);
    }

    public static void _glBufferData(int target, ByteBuffer data, int usage) {
        RenderSystem.assertOnRenderThread();
        GL33C.glBufferData(target, data, usage);
    }

    public static void _glBufferSubData(int target, long offset, ByteBuffer data) {
        RenderSystem.assertOnRenderThread();
        GL33C.glBufferSubData(target, offset, data);
    }

    public static void _glBufferData(int target, long size, int usage) {
        RenderSystem.assertOnRenderThread();
        GL33C.glBufferData(target, size, usage);
    }

    public static ByteBuffer _glMapBufferRange(int target, long offset, long length, int access) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glMapBufferRange(target, offset, length, access);
    }

    public static void _glUnmapBuffer(int target) {
        RenderSystem.assertOnRenderThread();
        GL33C.glUnmapBuffer(target);
    }

    public static void _glDeleteBuffers(int buffer) {
        RenderSystem.assertOnRenderThread();
        numBuffers--;
        PLOT_BUFFERS.setValue(numBuffers);
        GL33C.glDeleteBuffers(buffer);
    }

    public static void deleteVertexArrays(int array) {
        RenderSystem.assertOnRenderThread();
        GL30.glDeleteVertexArrays(array);
    }

    public static void _glBindFramebuffer(int target, int framebuffer) {
        if (Config.isShaders() && GlState.getFramebuffer() != null && (RenderUtils.isMainFramebuffer(framebuffer) || framebuffer == 0)) {
            framebuffer = GlState.getFramebuffer().getGlFramebuffer();
        }

        if ((target == 36008 || target == 36160) && readFbo != framebuffer) {
            GL33C.glBindFramebuffer(36008, framebuffer);
            readFbo = framebuffer;
        }

        if ((target == 36009 || target == 36160) && writeFbo != framebuffer) {
            GL33C.glBindFramebuffer(36009, framebuffer);
            writeFbo = framebuffer;
        }
    }

    public static int getFrameBuffer(int target) {
        if (target == 36008) {
            return readFbo;
        } else {
            return target == 36009 ? writeFbo : 0;
        }
    }

    public static void _glBlitFrameBuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        RenderSystem.assertOnRenderThread();
        GL33C.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    public static void _glDeleteFramebuffers(int framebuffer) {
        RenderSystem.assertOnRenderThread();
        GL33C.glDeleteFramebuffers(framebuffer);
        if (readFbo == framebuffer) {
            readFbo = 0;
        }

        if (writeFbo == framebuffer) {
            writeFbo = 0;
        }
    }

    public static int glGenFramebuffers() {
        RenderSystem.assertOnRenderThread();
        return GL33C.glGenFramebuffers();
    }

    public static void _glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        RenderSystem.assertOnRenderThread();
        GL33C.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    public static void glBlendFuncSeparate(int srcColor, int dstColor, int srcAlpha, int dstAlpha) {
        RenderSystem.assertOnRenderThread();
        GL33C.glBlendFuncSeparate(srcColor, dstColor, srcAlpha, dstAlpha);
    }

    public static void glBlendEquationSeparate(int modeRgb, int modeAlpha) {
        RenderSystem.assertOnRenderThread();
        GL33C.glBlendEquationSeparate(modeRgb, modeAlpha);
    }

    public static String glGetShaderInfoLog(int shader, int maxLength) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glGetShaderInfoLog(shader, maxLength);
    }

    public static String glGetProgramInfoLog(int program, int maxLength) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glGetProgramInfoLog(program, maxLength);
    }

    public static void _enableCull() {
        RenderSystem.assertOnRenderThread();
        if (cullLock.isLocked()) {
            cullLockState.setEnabled();
        } else {
            CULL.enable.enable();
        }
    }

    public static void _disableCull() {
        RenderSystem.assertOnRenderThread();
        if (cullLock.isLocked()) {
            cullLockState.setDisabled();
        } else {
            CULL.enable.disable();
        }
    }

    public static void _polygonMode(int face, int mode) {
        RenderSystem.assertOnRenderThread();
        GL33C.glPolygonMode(face, mode);
    }

    public static void _enablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        POLY_OFFSET.fill.enable();
    }

    public static void _disablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        POLY_OFFSET.fill.disable();
    }

    public static void _polygonOffset(float factor, float units) {
        RenderSystem.assertOnRenderThread();
        if (factor != POLY_OFFSET.factor || units != POLY_OFFSET.units) {
            POLY_OFFSET.factor = factor;
            POLY_OFFSET.units = units;
            GL33C.glPolygonOffset(factor, units);
        }
    }

    public static void _enableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        COLOR_LOGIC.enable.enable();
    }

    public static void _disableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        COLOR_LOGIC.enable.disable();
    }

    public static void _logicOp(int op) {
        RenderSystem.assertOnRenderThread();
        if (op != COLOR_LOGIC.op) {
            COLOR_LOGIC.op = op;
            GL33C.glLogicOp(op);
        }
    }

    public static void _activeTexture(int texture) {
        RenderSystem.assertOnRenderThread();
        if (activeTexture != texture - 33984) {
            activeTexture = texture - 33984;
            GL33C.glActiveTexture(texture);
        }
    }

    public static void enableTexture() {
        RenderSystem.assertOnRenderThread();
        TEXTURES[activeTexture].texture2DState = true;
    }

    public static void disableTexture() {
        RenderSystem.assertOnRenderThread();
        TEXTURES[activeTexture].texture2DState = false;
    }

    public static void texParameter(int target, int parameterName, float parameter) {
        RenderSystem.assertOnRenderThread();
        GL11.glTexParameterf(target, parameterName, parameter);
    }

    public static void _texParameter(int target, int name, int value) {
        RenderSystem.assertOnRenderThread();
        GL33C.glTexParameteri(target, name, value);
    }

    public static int _getTexLevelParameter(int target, int level, int name) {
        return GL33C.glGetTexLevelParameteri(target, level, name);
    }

    public static int _genTexture() {
        RenderSystem.assertOnRenderThread();
        numTextures++;
        PLOT_TEXTURES.setValue(numTextures);
        return GL33C.glGenTextures();
    }

    public static void _deleteTexture(int id) {
        RenderSystem.assertOnRenderThread();
        if (id != 0) {
            for (int i = 0; i < IMAGE_TEXTURES.length; i++) {
                if (IMAGE_TEXTURES[i] == id) {
                    IMAGE_TEXTURES[i] = 0;
                }
            }

            GL33C.glDeleteTextures(id);

            for (GlStateManager.TextureState state : TEXTURES) {
                if (state.binding == id) {
                    state.binding = 0;
                }
            }

            numTextures--;
            PLOT_TEXTURES.setValue(numTextures);
        }
    }

    public static void _bindTexture(int id) {
        RenderSystem.assertOnRenderThread();
        if (id != TEXTURES[activeTexture].binding) {
            TEXTURES[activeTexture].binding = id;
            GL33C.glBindTexture(3553, id);
            if (SmartAnimations.isActive()) {
                SmartAnimations.textureRendered(id);
            }
        }
    }

    public static void _texImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
        RenderSystem.assertOnRenderThread();
        GL33C.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public static void _texSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels) {
        RenderSystem.assertOnRenderThread();
        GL33C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void _texSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, ByteBuffer pixels) {
        RenderSystem.assertOnRenderThread();
        GL33C.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void _viewport(int x, int y, int width, int height) {
        GL33C.glViewport(x, y, width, height);
    }

    public static void _colorMask(@ColorTargetState.WriteMask int writeMask) {
        RenderSystem.assertOnRenderThread();

        for (int i = 0; i < COLOR_MASK.length; i++) {
            if (writeMask != COLOR_MASK[i]) {
                COLOR_MASK[i] = writeMask;
                GL33C.glColorMaski(i, (writeMask & 1) != 0, (writeMask & 2) != 0, (writeMask & 4) != 0, (writeMask & 8) != 0);
            }
        }
    }

    public static void _colorMask(int index, @ColorTargetState.WriteMask int writeMask) {
        RenderSystem.assertOnRenderThread();
        if (writeMask != COLOR_MASK[index]) {
            COLOR_MASK[index] = writeMask;
            GL33C.glColorMaski(index, (writeMask & 1) != 0, (writeMask & 2) != 0, (writeMask & 4) != 0, (writeMask & 8) != 0);
        }
    }

    public static void _clear(int mask) {
        RenderSystem.assertOnRenderThread();
        GL33C.glClear(mask);
        if (MacosUtil.IS_MACOS) {
            _getError();
        }
    }

    public static void _clearBuffer(int index, Vector4fc clearColor) {
        RenderSystem.assertOnRenderThread();
        GL33C.glClearBufferfv(6144, index, new float[]{clearColor.x(), clearColor.y(), clearColor.z(), clearColor.w()});
        if (MacosUtil.IS_MACOS) {
            _getError();
        }
    }

    public static void _clearBuffer(double clearDepth) {
        RenderSystem.assertOnRenderThread();
        GL33C.glClearBufferfv(6145, 0, new float[]{(float)clearDepth});
        if (MacosUtil.IS_MACOS) {
            _getError();
        }
    }

    public static void clearDepth(double depth) {
        RenderSystem.assertOnRenderThread();
        GL11.glClearDepth(depth);
    }

    public static void clearColor(float red, float green, float blue, float alpha) {
        RenderSystem.assertOnRenderThread();
        GL11.glClearColor(red, green, blue, alpha);
    }

    public static void _vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long value) {
        RenderSystem.assertOnRenderThread();
        GL33C.glVertexAttribPointer(index, size, type, normalized, stride, value);
    }

    public static void _vertexAttribIPointer(int index, int size, int type, int stride, long value) {
        RenderSystem.assertOnRenderThread();
        GL33C.glVertexAttribIPointer(index, size, type, stride, value);
    }

    public static void _enableVertexAttribArray(int index) {
        RenderSystem.assertOnRenderThread();
        GL33C.glEnableVertexAttribArray(index);
    }

    public static void _drawElements(int mode, int count, int type, long indices) {
        RenderSystem.assertOnRenderThread();
        GL33C.glDrawElements(mode, count, type, indices);
        if (Config.isShaders() && Shaders.isRenderingWorld) {
            int countInstances = Shaders.activeProgram.getCountInstances();
            if (countInstances > 1) {
                for (int i = 1; i < countInstances; i++) {
                    Shaders.uniform_instanceId.setValue(i);
                    GL33C.glDrawElements(mode, count, type, indices);
                }

                Shaders.uniform_instanceId.setValue(0);
            }
        }
    }

    public static void _drawArrays(int mode, int first, int count) {
        RenderSystem.assertOnRenderThread();
        GL33C.glDrawArrays(mode, first, count);
        if (Config.isShaders() && Shaders.isRenderingWorld) {
            int countInstances = Shaders.activeProgram.getCountInstances();
            if (countInstances > 1) {
                for (int i = 1; i < countInstances; i++) {
                    Shaders.uniform_instanceId.setValue(i);
                    GL33C.glDrawArrays(mode, first, count);
                }

                Shaders.uniform_instanceId.setValue(0);
            }
        }
    }

    public static void _pixelStore(int name, int value) {
        RenderSystem.assertOnRenderThread();
        GL33C.glPixelStorei(name, value);
    }

    public static void _readPixels(int x, int y, int width, int height, int format, int type, long pixels) {
        RenderSystem.assertOnRenderThread();
        GL33C.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public static int _getError() {
        RenderSystem.assertOnRenderThread();
        return GL33C.glGetError();
    }

    public static void clearGlErrors() {
        RenderSystem.assertOnRenderThread();

        while (GL33C.glGetError() != 0) {
        }
    }

    public static String _getString(int id) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glGetString(id);
    }

    public static int _getInteger(int name) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glGetInteger(name);
    }

    public static long _glFenceSync(int condition, int flags) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glFenceSync(condition, flags);
    }

    public static int _glClientWaitSync(long sync, int flags, long timeout) {
        RenderSystem.assertOnRenderThread();
        return GL33C.glClientWaitSync(sync, flags, timeout);
    }

    public static void _glDeleteSync(long sync) {
        RenderSystem.assertOnRenderThread();
        GL33C.glDeleteSync(sync);
    }

    public static int getActiveTextureUnit() {
        return 33984 + activeTexture;
    }

    public static void bindCurrentTexture() {
        GL11.glBindTexture(3553, TEXTURES[activeTexture].binding);
    }

    public static int getBoundTexture() {
        return TEXTURES[activeTexture].binding;
    }

    public static int getBoundTexture(int textureUnit) {
        return TEXTURES[textureUnit].binding;
    }

    public static void checkBoundTexture() {
        if (Config.isMinecraftThread()) {
            int glAct = GL11.glGetInteger(34016);
            int glTex = GL11.glGetInteger(32873);
            int act = getActiveTextureUnit();
            int tex = getBoundTexture();
            if (tex > 0) {
                if (glAct != act || glTex != tex) {
                    Config.dbg("checkTexture: act: " + act + ", glAct: " + glAct + ", tex: " + tex + ", glTex: " + glTex);
                }
            }
        }
    }

    public static void genTextures(IntBuffer buf) {
        GL11.glGenTextures(buf);
    }

    public static void deleteTextures(IntBuffer buf) {
        buf.rewind();

        while (buf.position() < buf.limit()) {
            int texId = buf.get();
            _deleteTexture(texId);
        }

        buf.rewind();
    }

    public static void lockAlpha(GlAlphaState stateNew) {
        if (!alphaLock.isLocked()) {
            getAlphaState(alphaLockState);
            if (stateNew != null) {
                setAlphaState(stateNew);
            }

            alphaLock.lock();
        }
    }

    public static void unlockAlpha() {
        if (alphaLock.unlock()) {
            setAlphaState(alphaLockState);
        }
    }

    public static void getAlphaState(GlAlphaState state) {
        if (alphaLock.isLocked()) {
            state.setState(alphaLockState);
        } else {
            state.setState(alphaTest, alphaTestFunc, alphaTestRef);
        }
    }

    public static void setAlphaState(GlAlphaState state) {
        if (alphaLock.isLocked()) {
            alphaLockState.setState(state);
        } else {
            alphaTest = state.isEnabled();
            alphaFunc(state.getFunc(), state.getRef());
        }
    }

    public static void lockBlend(GlBlendState stateNew) {
        if (!blendLock.isLocked()) {
            getBlendState(blendLockState);
            if (stateNew != null) {
                setBlendState(stateNew);
            }

            blendLock.lock();
        }
    }

    public static void unlockBlend() {
        if (blendLock.unlock()) {
            setBlendState(blendLockState);
        }
    }

    public static void getBlendState(GlBlendState gbs) {
        if (blendLock.isLocked()) {
            gbs.setState(blendLockState);
        } else {
            GlStateManager.BlendState blend = BLEND[0];
            gbs.setState(blend.mode.enabled, blend.srcRgb, blend.dstRgb, blend.srcAlpha, blend.dstAlpha);
        }
    }

    public static void setBlendState(GlBlendState gbs) {
        if (blendLock.isLocked()) {
            blendLockState.setState(gbs);
        } else {
            BLEND[0].mode.setEnabled(gbs.isEnabled());
            if (!gbs.isSeparate()) {
                blendFunc(gbs.getSrcFactor(), gbs.getDstFactor());
            } else {
                _blendFuncSeparate(gbs.getSrcFactor(), gbs.getDstFactor(), gbs.getSrcFactorAlpha(), gbs.getDstFactorAlpha());
            }
        }
    }

    public static void lockCull(GlCullState stateNew) {
        if (!cullLock.isLocked()) {
            getCullState(cullLockState);
            if (stateNew != null) {
                setCullState(stateNew);
            }

            cullLock.lock();
        }
    }

    public static void unlockCull() {
        if (cullLock.unlock()) {
            setCullState(cullLockState);
        }
    }

    public static void getCullState(GlCullState state) {
        if (cullLock.isLocked()) {
            state.setState(cullLockState);
        } else {
            state.setState(CULL.enable.enabled, CULL.mode);
        }
    }

    public static void setCullState(GlCullState state) {
        if (cullLock.isLocked()) {
            cullLockState.setState(state);
        } else {
            CULL.enable.setEnabled(state.isEnabled());
            CULL.mode = state.getMode();
        }
    }

    public static void lockDepth(GlDepthState stateNew) {
        if (!depthLock.isLocked()) {
            getDepthState(depthLockState);
            if (stateNew != null) {
                setDepthState(stateNew);
            }

            depthLock.lock();
        }
    }

    public static void unlockDepth() {
        if (depthLock.unlock()) {
            setDepthState(depthLockState);
        }
    }

    public static void getDepthState(GlDepthState state) {
        if (depthLock.isLocked()) {
            state.setState(depthLockState);
        } else {
            state.setState(DEPTH.mode.enabled, DEPTH.mask, DEPTH.func);
        }
    }

    public static void setDepthState(GlDepthState state) {
        if (depthLock.isLocked()) {
            depthLockState.setState(state);
        } else {
            DEPTH.mode.setEnabled(state.isEnabled());
            _depthMask(state.isMask());
            _depthFunc(state.getFunc());
        }
    }

    public static void lockScissor(GlScissorState stateNew) {
        if (!scissorLock.isLocked()) {
            scissorLockState.setEnabled(SCISSOR.mode.enabled);
            scissorLockState.clearBox();
            if (stateNew != null) {
                setScissorState(stateNew);
            }

            scissorLock.lock();
        }
    }

    public static void unlockScissor() {
        if (scissorLock.unlock()) {
            setScissorState(scissorLockState);
        }
    }

    public static void setScissorState(GlScissorState state) {
        if (scissorLock.isLocked()) {
            scissorLockState.setState(state);
        } else {
            SCISSOR.mode.setEnabled(state.isEnabled());
            if (state.isBoxSet()) {
                GL33C.glScissor(state.getX(), state.getY(), state.getWidth(), state.getHeight());
            }
        }
    }

    public static void glMultiDrawArrays(int mode, IntBuffer bFirst, IntBuffer bCount) {
        GL14.glMultiDrawArrays(mode, bFirst, bCount);
        if (Config.isShaders()) {
            int countInstances = Shaders.activeProgram.getCountInstances();
            if (countInstances > 1) {
                for (int i = 1; i < countInstances; i++) {
                    Shaders.uniform_instanceId.setValue(i);
                    GL14.glMultiDrawArrays(mode, bFirst, bCount);
                }

                Shaders.uniform_instanceId.setValue(0);
            }
        }
    }

    public static void glMultiDrawElements(int modeIn, IntBuffer countsIn, int typeIn, PointerBuffer indicesIn) {
        RenderSystem.assertOnRenderThread();
        GL14.glMultiDrawElements(modeIn, countsIn, typeIn, indicesIn);
        if (Config.isShaders() && Shaders.isRenderingWorld) {
            int countInstances = Shaders.activeProgram.getCountInstances();
            if (countInstances > 1) {
                for (int i = 1; i < countInstances; i++) {
                    Shaders.uniform_instanceId.setValue(i);
                    GL14.glMultiDrawElements(modeIn, countsIn, typeIn, indicesIn);
                }

                Shaders.uniform_instanceId.setValue(0);
            }
        }
    }

    public static void glMultiDrawElementsBaseVertex(int modeIn, IntBuffer countsIn, int typeIn, PointerBuffer indicesIn, IntBuffer baseVertex) {
        RenderSystem.assertOnRenderThread();
        GL32.glMultiDrawElementsBaseVertex(modeIn, countsIn, typeIn, indicesIn, baseVertex);
        if (Config.isShaders() && Shaders.isRenderingWorld) {
            int countInstances = Shaders.activeProgram.getCountInstances();
            if (countInstances > 1) {
                for (int i = 1; i < countInstances; i++) {
                    Shaders.uniform_instanceId.setValue(i);
                    GL32.glMultiDrawElementsBaseVertex(modeIn, countsIn, typeIn, indicesIn, baseVertex);
                }

                Shaders.uniform_instanceId.setValue(0);
            }
        }
    }

    public static void bufferSubData(int target, long offset, ByteBuffer data) {
        GL15.glBufferSubData(target, offset, data);
    }

    public static void copyBufferSubData(int readTarget, int writeTarget, long readOffset, long writeOffset, long size) {
        GL31.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
    }

    public static void applyCurrentBlend() {
        if (BLEND[0].mode.enabled) {
            GL11.glEnable(3042);
        } else {
            GL11.glDisable(3042);
        }

        GL14.glBlendFuncSeparate(BLEND[0].srcRgb, BLEND[0].dstRgb, BLEND[0].srcAlpha, BLEND[0].dstAlpha);
    }

    public static void setBlendsIndexed(GlBlendState[] blends) {
        if (blends != null) {
            for (int i = 0; i < blends.length; i++) {
                GlBlendState bs = blends[i];
                if (bs != null) {
                    if (bs.isEnabled()) {
                        GL30.glEnablei(3042, i);
                    } else {
                        GL30.glDisablei(3042, i);
                    }

                    ARBDrawBuffersBlend.glBlendFuncSeparateiARB(i, bs.getSrcFactor(), bs.getDstFactor(), bs.getSrcFactorAlpha(), bs.getDstFactorAlpha());
                }
            }
        }
    }

    public static void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
        if (unit >= 0 && unit < IMAGE_TEXTURES.length) {
            if (IMAGE_TEXTURES[unit] == texture) {
                return;
            }

            IMAGE_TEXTURES[unit] = texture;
        }

        GL42.glBindImageTexture(unit, texture, level, layered, layer, access, format);
    }

    public static int getProgram() {
        return glProgram;
    }

    public static void bindSampler(int unit, int sampler) {
        GL33.glBindSampler(unit, sampler);
    }

    public static void bindTexture(int target, int texture) {
        GL11.glBindTexture(target, texture);
    }

    public static boolean _isBlendEnabled(int index) {
        RenderSystem.assertOnRenderThread();
        return BLEND[0].mode.enabled;
    }

    static {
        Arrays.setAll(COLOR_MASK, var0 -> 15);
        Arrays.setAll(BLEND, var0 -> new GlStateManager.BlendState());
    }

    private static class BlendState {
        public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3042);
        public int srcRgb = 1;
        public int dstRgb = 0;
        public int modeRgb = 32774;
        public int srcAlpha = 1;
        public int dstAlpha = 0;
        public int modeAlpha = 32774;
    }

    private static class BooleanState {
        private final int state;
        private boolean enabled;

        public BooleanState(int state) {
            this.state = state;
        }

        public void disable() {
            this.setEnabled(false);
        }

        public void enable() {
            this.setEnabled(true);
        }

        public void setEnabled(boolean enabled) {
            RenderSystem.assertOnRenderThread();
            if (enabled != this.enabled) {
                this.enabled = enabled;
                if (enabled) {
                    GL33C.glEnable(this.state);
                } else {
                    GL33C.glDisable(this.state);
                }
            }
        }
    }

    private static class ColorLogicState {
        public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(3058);
        public int op = 5379;
    }

    private static class CullState {
        public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2884);
        public int mode = 1029;
    }

    private static class DepthState {
        public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(2929);
        public boolean mask = true;
        public int func = 513;
    }

    private static class PolygonOffsetState {
        public final GlStateManager.BooleanState fill = new GlStateManager.BooleanState(32823);
        public float factor;
        public float units;
    }

    private static class ScissorState {
        public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3089);
    }

    private static class TextureState {
        public int binding;
        public boolean texture2DState;
    }
}
