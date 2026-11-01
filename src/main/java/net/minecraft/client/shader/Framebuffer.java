package net.minecraft.client.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import java.nio.ByteBuffer;

public class Framebuffer {
    public int framebufferTextureWidth;
    public int framebufferTextureHeight;
    public int framebufferWidth;
    public int framebufferHeight;
    public final boolean useDepth;
    public int framebufferObject = -1;
    public int framebufferTexture = -1;
    public int depthBuffer = -1;
    public final float[] framebufferColor = new float[]{1.0F, 1.0F, 1.0F, 0.0F};
    public int framebufferFilter = 9728;

    public Framebuffer(int width, int height, boolean useDepth) {
        this.useDepth = useDepth;
        this.createBuffers(width, height);
    }

    public void createBuffers(int width, int height) {
        this.framebufferWidth = width;
        this.framebufferHeight = height;
        this.framebufferTextureWidth = width;
        this.framebufferTextureHeight = height;
        this.framebufferObject = GL30.glGenFramebuffers();
        this.framebufferTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.framebufferTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, this.framebufferFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, this.framebufferFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebufferObject);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.framebufferTexture, 0);
        if (this.useDepth) {
            this.depthBuffer = GL30.glGenRenderbuffers();
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.depthBuffer);
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, this.depthBuffer);
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, this.depthBuffer);
        }
        this.framebufferClear();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void setFramebufferFilter(int filter) {
        this.framebufferFilter = filter;
        if (this.framebufferTexture > -1) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
    }

    public void bindFramebuffer(boolean setViewport) {
        if (this.framebufferObject > -1) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebufferObject);
        } else {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        }
        if (setViewport) {
            GL11.glViewport(0, 0, this.framebufferWidth, this.framebufferHeight);
        }
    }

    public void bindFramebufferTexture() {
        if (this.framebufferTexture > -1) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.framebufferTexture);
        }
    }

    public void unbindFramebufferTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void setFramebufferColor(float r, float g, float b, float a) {
        this.framebufferColor[0] = r;
        this.framebufferColor[1] = g;
        this.framebufferColor[2] = b;
        this.framebufferColor[3] = a;
    }

    public void unbindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void framebufferClear() {
        this.framebufferClear(false);
    }

    public void framebufferClear(boolean checkError) {
        this.bindFramebuffer(true);
        GL11.glClearColor(this.framebufferColor[0], this.framebufferColor[1], this.framebufferColor[2], this.framebufferColor[3]);
        int mask = GL11.GL_COLOR_BUFFER_BIT;
        if (this.useDepth) {
            GL11.glClearDepth(1.0);
            mask |= GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT;
        }
        GL11.glClear(mask);
        this.unbindFramebuffer();
    }

    public void deleteFramebuffer() {
        if (this.depthBuffer > -1) {
            GL30.glDeleteRenderbuffers(this.depthBuffer);
            this.depthBuffer = -1;
        }
        if (this.framebufferTexture > -1) {
            GL11.glDeleteTextures(this.framebufferTexture);
            this.framebufferTexture = -1;
        }
        if (this.framebufferObject > -1) {
            GL30.glDeleteFramebuffers(this.framebufferObject);
            this.framebufferObject = -1;
        }
    }

    public int checkFramebufferComplete() {
        return GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
    }
}
