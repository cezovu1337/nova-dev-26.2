package dev.fesly.client.module.impl.render;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.Priorities;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.render.BossHealthRenderEvent;
import dev.fesly.impl.event.impl.render.DrawHudPreEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.ColorSetting;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.util.Iterator;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;
@ModuleInfo(name = "Tracers", category = Category.RENDER, description = "показывает трейсеры на игроков!")
public class Tracers extends Module {
    public static Color friendColor = Color.GREEN;
    public static Color playersColor = Color.WHITE;
    public static boolean arrows = true;
    @Override
    public void init() {
        super.init();
        addSetting("arrows", new BooleanSetting("Arrows", this, Tracers::isArrows, Tracers::setArrows));
        addSetting("friendColor", new ColorSetting("Friend", this, Tracers::getFriendColor, Tracers::setFriendColor));
        addSetting("playersColor", new ColorSetting("Player", this, Tracers::getPlayersColor, Tracers::setPlayersColor));
    }
    @EventLink
    public Listener<BossHealthRenderEvent> onHudPre = evt -> {
        RenderHelper.enableStandardItemLighting();
        if (!arrows) render();
         else {
            Runnable arrows = () -> {
                if (mc.world != null && mc.player != null) {
                    for (final Entity e : mc.world.getPlayers()) {
                        if (e instanceof Player && e != mc.player) {
                            Vector3d vector3d = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
                            final double pos1 = (((e.getPosX() + (e.getPosX() - e.lastTickPosX) * mc.getRenderPartialTicks()) - vector3d.x) * 0.2);
                            final double pos2 = (((e.getPosZ() + (e.getPosZ() - e.lastTickPosZ) * mc.getRenderPartialTicks()) - vector3d.z) * 0.2);
                            final double cos = Math.cos(mc.player.rotationYaw * (Math.PI * 2.0F / 360.0F));
                            final double sin = Math.sin(mc.player.rotationYaw * (Math.PI * 2.0F / 360.0F));
                            final double rotY = -(pos2 * cos - pos1 * sin);
                            final double rotX = -(pos1 * cos + pos2 * sin);
                            final double var7 = 0 - rotX;
                            final double var9 = 0 - rotY;
                            final int kek = 20;
                            final double size = 51 + kek;
                            final double xOffset = mw.getScaledWidth() / 2.0F - 24.5 - kek / 2.0;
                            final double yOffset = mw.getScaledHeight() / 2.0F - 25.2 - kek / 2.0;
                            if (MathHelper.sqrt(var7 * var7 + var9 * var9) < size / 2 - 4) {
                                final double angle = (Math.atan2(rotY - 0.0F, rotX - 0.0F) * 180.0F / Math.PI);
                                final double x = ((size / 2.0F) * Math.cos(Math.toRadians(angle))) + xOffset + size / 2.0F;
                                final double y = ((size / 2.0F) * Math.sin(Math.toRadians(angle))) + yOffset + size / 2.0F;
                                glPushMatrix();
                                glTranslated(x, y, 0.0);
                                glRotated(angle, 0, 0, 1);
                                glScaled(1.5, 1.0, 1.0);
                                glRotatef(-90.0F, 0, 0, 1);
                                if (Nova.getInstance().getFriendManager().isFriend(e.getScoreboardName())) drawTriangle(0, 0, 6.5, getFriendColor().brighter().hashCode());
                                 else drawTriangle(0, 0, 6, getPlayersColor().brighter().hashCode());
                                glPopMatrix();
                            }
                        }
                    }
                }
            };
            IAccess.BLOOM_RUNNABLES.add(arrows);
            arrows.run();
        }
        RenderHelper.disableStandardItemLighting();
    };
    public void render() {
        GL11.glPushMatrix();
        GL11.glDisable(2896);
        GL11.glDisable(2929);
        Iterator var1 = mc.world.getPlayers().iterator();
        while (var1.hasNext()) {
            Entity e = (Entity) var1.next();
            if (e != this.mc.player && e instanceof Player) {
                Player player = (Player) e;
                float ticks = mc.getRenderPartialTicks();
                double x2 = e.getPosX();
                double y2 = e.getPosY();
                double z2 = e.getPosZ();
                double[] vec = RenderUtil.worldToScreen(x2, y2, z2);
                if (vec != null) {
                    Color c = Nova.getInstance().getFriendManager().isFriend(e.getScoreboardName()) ? friendColor : playersColor;
                    GL11.glPushMatrix();
                    GL11.glBlendFunc(770, 771);
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glColor3f(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F);
                    GL11.glEnable(2848);
                    GL11.glLineWidth(1.0F);
                    GL11.glBegin(1);
                    int startx = this.mc.getMainWindow().getScaledWidth() / 2;
                    int starty = this.mc.getMainWindow().getScaledHeight() / 2;
                    GL11.glVertex2d((double) startx, (double) starty);
                    GL11.glVertex2d(vec[0], vec[1]);
                    GL11.glEnd();
                    GL11.glDisable(3042);
                    GL11.glEnable(3553);
                    GL11.glDisable(2848);
                    GL11.glPopMatrix();
                }
            }
        }
        GL11.glEnable(2929);
        GL11.glPopMatrix();
    }
    private void drawTriangle(final double x, final double y, final double radius, int color) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableCull();
        GL11.glShadeModel(GL_FLAT);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        RenderUtil.setColor(color);
        GL11.glVertex3d(x, y + radius * 1.25F, 0);
        GL11.glVertex3d(x + radius, y, 0);
        GL11.glVertex3d(x, y + radius * 0.25F, 0);
        GL11.glVertex3d(x, y + radius * 1.25F, 0);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        RenderUtil.setColor(darker(color, 0.8F).getRGB());
        GL11.glVertex3d(x, y + radius * 1.25F, 0);
        GL11.glVertex3d(x, y + radius * 0.25F, 0);
        GL11.glVertex3d(x - radius, y, 0);
        GL11.glVertex3d(x, y + radius * 1.25F, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnd();
        GL11.glLineWidth(1.5F);
        GL11.glBegin(GL_LINE_LOOP);
        RenderUtil.setColor(new Color(color).getRGB());
        GL11.glVertex3d(x, y + radius * 1.25F, 0);
        GL11.glVertex3d(x + radius, y, 0);
        GL11.glVertex3d(x, y + radius * 0.25F, 0);
        GL11.glVertex3d(x - radius, y, 0);
        GL11.glEnd();
        RenderUtil.setColor(-1);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
    public static Color darker(Color color, float factor) {
        return new Color(Math.max((int) (color.getRed() * factor), 0), Math.max((int) (color.getGreen() * factor), 0), Math.max((int) (color.getBlue() * factor), 0), color.getAlpha());
    }
    public static Color darker(int colorb, float factor) {
        Color color = new Color(colorb);
        return new Color(Math.max((int) (color.getRed() * factor), 0), Math.max((int) (color.getGreen() * factor), 0), Math.max((int) (color.getBlue() * factor), 0), color.getAlpha());
    }
    @java.lang.SuppressWarnings("all")
    public static Color getFriendColor() {
        return Tracers.friendColor;
    }
    @java.lang.SuppressWarnings("all")
    public static void setFriendColor(final Color friendColor) {
        Tracers.friendColor = friendColor;
    }
    @java.lang.SuppressWarnings("all")
    public static Color getPlayersColor() {
        return Tracers.playersColor;
    }
    @java.lang.SuppressWarnings("all")
    public static void setPlayersColor(final Color playersColor) {
        Tracers.playersColor = playersColor;
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isArrows() {
        return Tracers.arrows;
    }
    @java.lang.SuppressWarnings("all")
    public static void setArrows(final boolean arrows) {
        Tracers.arrows = arrows;
    }
}
