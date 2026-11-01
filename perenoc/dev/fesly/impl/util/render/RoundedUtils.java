package dev.fesly.impl.util.render;

import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.platform.GlStateManager;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RoundedUtils {
    final static Minecraft mc = Minecraft.getInstance();

    public static void enableGL2D() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(true);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
    }

    public static void disableGL2D() {
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE);
    }
    /*
     *
     * NORMAL
     *
     */

    public static void drawSmoothRoundedRectWH(float x, float y, float width, float height, float radius, Color color) {
        drawSmoothRoundedRect(x,y,x + width, y + height, radius, color.getRGB());
    }

    public static void drawRoundedRectWH(float x, float y, float width, float height, float radius, Color color) {
        drawRoundedRect(x,y,x + width, y + height, radius, color.getRGB());
    }

    /**
     * @param x       : X pos
     * @param y       : Y pos
     * @param x1      : X2 pos
     * @param y1      : Y2 pos
     * @param radius  : round of edges;
     * @param color4f : color4f;
     */

    public static void drawSmoothRoundedRect(float x, float y, float x1, float y1, float radius, int color4f) {
        glPushAttrib(0);
        glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        x1 *= 2.0D;
        y1 *= 2.0D;
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        setColor(color4f);
        glEnable(GL_LINE_SMOOTH);
        glBegin(GL_POLYGON);
        int i;
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y + radius + Math.cos(i * Math.PI / 180.0D) * radius);
        glEnd();
        glBegin(GL_LINE_LOOP);
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y + radius + Math.cos(i * Math.PI / 180.0D) * radius);
        glEnd();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glScaled(2.0D, 2.0D, 2.0D);
        glPopAttrib();
        glLineWidth(1);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawRoundedRect(float x, float y, float x1, float y1, float radius, int color4f) {
        glPushAttrib(0);
        glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        x1 *= 2.0D;
        y1 *= 2.0D;
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        setColor(color4f);
        glEnable(GL_LINE_SMOOTH);
        glBegin(GL_POLYGON);
        int i;
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y + radius + Math.cos(i * Math.PI / 180.0D) * radius);
        glEnd();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glScaled(2.0D, 2.0D, 2.0D);
        glEnable(GL_BLEND);
        glPopAttrib();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * @param x         : X pos
     * @param y         : Y pos
     * @param x1        : X2 pos
     * @param y1        : Y2 pos
     * @param radius    : round of edges;
     * @param lineWidth : width of outline line;
     * @param color4f   : color4f;
     */

    public static void drawRoundedOutline(float x, float y, float x1, float y1, float radius, float lineWidth, int color4f) {
        glPushAttrib(0);
        glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        x1 *= 2.0D;
        y1 *= 2.0D;
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        setColor(color4f);
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(lineWidth);
        glBegin(GL_LINE_LOOP);
        int i;
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y + radius + Math.cos(i * Math.PI / 180.0D) * radius);
        glEnd();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glScaled(2.0D, 2.0D, 2.0D);
        glPopAttrib();
        glLineWidth(1);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /*
     *
     * SELECTED EDGES
     *
     */

    /**
     * @param x       : X pos
     * @param y       : Y pos
     * @param x1      : X2 pos
     * @param y1      : Y2 pos
     * @param radius1 : round of left top edges;
     * @param radius2 : round of right top edges;
     * @param radius3 : round of left bottom edges;
     * @param radius4 : round of right bottom edges;
     * @param color4f : color4f;
     */

    public static void drawSelectRoundedRect(float x, float y, float x1, float y1, float radius1, float radius2, float radius3, float radius4, int color4f) {
        glPushAttrib(0);
        glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        x1 *= 2.0D;
        y1 *= 2.0D;
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        setColor(color4f);
        glEnable(GL_LINE_SMOOTH);
        glBegin(9);
        int i;
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x + radius1 + Math.sin(i * Math.PI / 180.0D) * radius1 * -1.0D, y + radius1 + Math.cos(i * Math.PI / 180.0D) * radius1 * -1.0D);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x + radius2 + Math.sin(i * Math.PI / 180.0D) * radius2 * -1.0D, y1 - radius2 + Math.cos(i * Math.PI / 180.0D) * radius2 * -1.0D);
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x1 - radius3 + Math.sin(i * Math.PI / 180.0D) * radius3, y1 - radius3 + Math.cos(i * Math.PI / 180.0D) * radius3);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x1 - radius4 + Math.sin(i * Math.PI / 180.0D) * radius4, y + radius4 + Math.cos(i * Math.PI / 180.0D) * radius4);
        glEnd();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glScaled(2.0D, 2.0D, 2.0D);
        glPopAttrib();
        glLineWidth(1);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * @param x         : X pos
     * @param y         : Y pos
     * @param x1        : X2 pos
     * @param y1        : Y2 pos
     * @param radius1   : round of left top edges;
     * @param radius2   : round of right top edges;
     * @param radius3   : round of left bottom edges;
     * @param radius4   : round of right bottom edges;
     * @param lineWidth : width of outline line;
     * @param color4f   : color4f;
     */

    public static void drawSelectRoundedOutline(float x, float y, float x1, float y1, float radius1, float radius2, float radius3, float radius4, float lineWidth, int color4f) {
        glPushAttrib(0);
        glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        x1 *= 2.0D;
        y1 *= 2.0D;
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        setColor(color4f);
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(lineWidth);
        glBegin(GL_LINE_LOOP);
        int i;
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x + radius1 + Math.sin(i * Math.PI / 180.0D) * radius1 * -1.0D, y + radius1 + Math.cos(i * Math.PI / 180.0D) * radius1 * -1.0D);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x + radius2 + Math.sin(i * Math.PI / 180.0D) * radius2 * -1.0D, y1 - radius2 + Math.cos(i * Math.PI / 180.0D) * radius2 * -1.0D);
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x1 - radius3 + Math.sin(i * Math.PI / 180.0D) * radius3, y1 - radius3 + Math.cos(i * Math.PI / 180.0D) * radius3);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x1 - radius4 + Math.sin(i * Math.PI / 180.0D) * radius4, y + radius4 + Math.cos(i * Math.PI / 180.0D) * radius4);
        glEnd();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glScaled(2.0D, 2.0D, 2.0D);
        glPopAttrib();
        glLineWidth(1);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void setColor(int color) {
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        glColor4f(r, g, b, a);
    }

    /*
     *
     * GRADIENT
     *
     */

    /**
     * @param x        : X pos
     * @param y        : Y pos
     * @param x1       : X2 pos
     * @param y1       : Y2 pos
     * @param radius   : round of edges;
     * @param color4f  : color4f;
     * @param color4f2 : color4f2;
     * @param color4f3 : color4f3;
     * @param color4f4 : color4f4;
     */
    public static void drawRoundedGradientRectCorner(float x, float y, float x1, float y1, float radius, int color4f, int color4f2, int color4f3, int color4f4) {
        setColor(-1);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glShadeModel(GL_SMOOTH);

        glPushAttrib(0);
        glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        x1 *= 2.0D;
        y1 *= 2.0D;
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        setColor(color4f);
        glEnable(GL_LINE_SMOOTH);
        glShadeModel(GL_SMOOTH);
        glBegin(9);
        int i;
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        setColor(color4f2);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        setColor(color4f3);
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius);
        setColor(color4f4);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y + radius + Math.cos(i * Math.PI / 180.0D) * radius);
        glEnd();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glScaled(2.0D, 2.0D, 2.0D);
        glPopAttrib();


        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glShadeModel(GL_FLAT);
        setColor(-1);
    }


    /**
     * @param x        : X pos
     * @param y        : Y pos
     * @param x1       : X2 pos
     * @param y1       : Y2 pos
     * @param width    : width of line;
     * @param radius   : round of edges;
     * @param color4f  : color4f;
     * @param color4f2 : color4f2;
     * @param color4f3 : color4f3;
     * @param color4f4 : color4f4;
     */
    public static void drawRoundedGradientOutlineCorner(float x, float y, float x1, float y1, float width, float radius, int color4f, int color4f2, int color4f3, int color4f4) {
        setColor(-1);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glShadeModel(GL_SMOOTH);

        glPushAttrib(0);
        glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        x1 *= 2.0D;
        y1 *= 2.0D;
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        setColor(color4f);
        glEnable(GL_LINE_SMOOTH);
        glShadeModel(GL_SMOOTH);
        glLineWidth(width);
        glBegin(GL_LINE_LOOP);
        int i;
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        setColor(color4f2);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
        setColor(color4f3);
        for (i = 0; i <= 90; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y1 - radius + Math.cos(i * Math.PI / 180.0D) * radius);
        setColor(color4f4);
        for (i = 90; i <= 180; i += 3)
            glVertex2d(x1 - radius + Math.sin(i * Math.PI / 180.0D) * radius, y + radius + Math.cos(i * Math.PI / 180.0D) * radius);
        glEnd();
        glLineWidth(1);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glScaled(2.0D, 2.0D, 2.0D);
        glPopAttrib();


        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glShadeModel(GL_FLAT);
        setColor(-1);
    }
}
