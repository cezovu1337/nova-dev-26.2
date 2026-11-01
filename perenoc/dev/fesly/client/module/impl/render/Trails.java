package dev.fesly.client.module.impl.render;


import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.event.impl.other.TickEvent;
import dev.fesly.impl.event.impl.render.WorldRenderEvent;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.time.TimerUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.util.MathUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ModuleInfo(
        name = "Trail",
        category = Category.RENDER,
        description = "Рисует за вами след."
)
@Getter
@Setter
public class Trails  extends Module {

    public List<Trail> trails = new ArrayList<>();

    private boolean onlyLine = true;
    private boolean renderWithShaders = false;

    private boolean hideInFirstPerson = true;


    @Override
    public void init() {
        super.init();
        addSetting("hideInFirstPerson", new BooleanSetting("Hide In First Person",this, this::isHideInFirstPerson, this::setHideInFirstPerson));

        addSetting("onlyLine", new BooleanSetting("Only Line",this, this::isOnlyLine, this::setOnlyLine));

        addSetting("renderWithShaders", new BooleanSetting("Render With Shaders",this,() -> (mc.gameRenderer.getShaderGroup() != null), this::isRenderWithShaders, this::setRenderWithShaders));

    }

    @EventLink
    public Listener<MotionEvent> onMotion = evt -> {
    };

    @EventLink
    public Listener<TickEvent> onPreTick = evt -> {
        trails.removeIf(t -> t.timer.hasReached((int) t.time / 3f));
    };

    public static MatrixStack matrixFrom(MatrixStack matrices, ActiveRenderInfo camera) {

        matrices.rotate(Vector3f.XP.rotationDegrees(camera.getPitch()));
        matrices.rotate(Vector3f.YP.rotationDegrees(camera.getYaw() + 180.0F));

        return matrices;
    }


    @EventLink
    public Listener<WorldRenderEvent> onRender = evt -> {
        ClientPlayerEntity t = mc.player;

        boolean thirdPerson =ClientUtil.isThirdPerson();
        if (thirdPerson && hideInFirstPerson) return;
        if (t.getPosX() != t.lastTickPosX || t.getPosZ() != t.lastTickPosZ) {
            double x = mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX) * (double) mc.getRenderPartialTicks();
            double y = mc.player.lastTickPosY + (mc.player.getPosY() - mc.player.lastTickPosY) * (double) mc.getRenderPartialTicks();
            double z = mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ) * (double) mc.getRenderPartialTicks();
            float hypot = (float) Math.hypot(x,z);
            float height = t.getHeight() / 1.4f;
            this.trails.add(new Trail(height, new Vector3d(x,
                    y, z),
                    new Vector3d(t.getPosX(), t.getPosY(),
                            t.getPosZ()),
                    (long) (2200f), this.trails.size()));

        }

        MatrixStack ms = matrixFrom(evt.getMatrixStack(),evt.getActiveRenderInfo());
        double ix = -((mc.getRenderManager().info.getProjectedView().getX()));
        double iy = -((mc.getRenderManager().info.getProjectedView().getY()));
        double iz = -((mc.getRenderManager().info.getProjectedView().getZ()));

        Collections.reverse(trails);
        for (Trail c : trails) {
            float xz;
            int color2;
            int color;
            int[] ccolors;
            float i;

            if (trails.indexOf(c) < 2) {
                c.pos.x = MathUtil.lerp(c.prevPos.x,
                        trails.get(Math.max(trails.indexOf(c) - 6, 0)).pos.x, 0.05f);
                c.pos.y = MathUtil.lerp(c.prevPos.y,
                        trails.get(Math.max(trails.indexOf(c) - 6, 0)).pos.y, 0.05f);
                c.pos.z = MathUtil.lerp(c.prevPos.z,
                        trails.get(Math.max(trails.indexOf(c) - 6, 0)).pos.z, 0.05f);
            }

            double x = c.pos.x;
            double y = c.pos.y;
            double z = c.pos.z;
                c.prevPos.x = MathUtil.lerp(c.prevPos.x,
                        trails.get(Math.max(trails.indexOf(c) - 5, 0)).pos.x, 0.95f);
                c.prevPos.y = MathUtil.lerp(c.prevPos.y,
                        trails.get(Math.max(trails.indexOf(c) - 5, 0)).pos.y, 0.95f);
                c.prevPos.z = MathUtil.lerp(c.prevPos.z,
                        trails.get(Math.max(trails.indexOf(c) - 5, 0)).pos.z, 0.95f);



            double lastX = c.prevPos.x;
            double lastY = c.prevPos.y;
            double lastZ = c.prevPos.z;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            ms.push();
            ms.translate(ix, iy, iz);
            //ms.translate(x, y, z);

            RenderUtil.setupRender();

            GL11.glDisable(GL11.GL_POINT_SMOOTH);

            int begin = GL11.GL_QUAD_STRIP;

            float rsp = (float)(c.timer.getTimePassed() + 600) / (float)(c.time);
            float sp = System.currentTimeMillis() / (float)30 % 360L;
            double radius = (0.08f);

            float apc = 1.0f - rsp;
            apc = (float)  (apc / 2f);
            apc = (float) MathUtils.easeInOutQuad(apc, 1);
            apc = MathHelper.clamp(apc, 0.0f, 1.0f);

            double vert = -1 / 100.0;
            int a = (int)MathHelper.clamp(apc * 255.0f, 0.0f, 255.0f);


            Color colorb = ColorUtil.interpolateTwoColors(15, (int) (trails.indexOf(c) * 16),
                    ClientUtil.getOneClientColor(), ClientUtil.getThreeClientColor());
            Color color2b = ColorUtil.interpolateTwoColors(15, (int) (trails.indexOf(c) * 16),
                    ClientUtil.getThreeClientColor(),ClientUtil.getOneClientColor());

            boolean ad = a / 4 >= 10;
            color = ColorUtil.withAlpha(colorb, a).getRGB();
            color2 = ColorUtil.withAlpha(color2b, a).getRGB();

            int colorD = ColorUtil.withAlpha(colorb, (int) (a * 2)).darker().darker().getRGB();
            int color2D = ColorUtil.withAlpha(color2b, (int) (a * 2f)).darker().darker().getRGB();

            int nonA = ColorUtil.withAlpha(colorb, 0).getRGB();

            float step = MathHelper.clamp(0, 9.0f, 95.0f);

            float height = c.height + (apc) - 0.2f;


            bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            bufferbuilder.pos(ms.getLast().getMatrix(), (float) lastX, (float) lastY, (float) lastZ)
                    .color(ColorUtil.r(color2), ColorUtil.g(color2), ColorUtil.b(color2), ColorUtil.a(color2)).endVertex();
            bufferbuilder.pos(ms.getLast().getMatrix(), (float) x, (float) y, (float) z)
                    .color(ColorUtil.r(color), ColorUtil.g(color), ColorUtil.b(color), ColorUtil.a(color)).endVertex();

            tessellator.draw();
            if (!onlyLine) {
                bufferbuilder.begin(begin, DefaultVertexFormats.POSITION_COLOR);

                bufferbuilder.pos(ms.getLast().getMatrix(), (float) lastX, (float) lastY, (float) lastZ)
                        .color(ColorUtil.r(color2D), ColorUtil.g(color2D), ColorUtil.b(color2D), ColorUtil.a(color2D) / 2).endVertex();
                bufferbuilder.pos(ms.getLast().getMatrix(), (float) lastX, (float) lastY + height, (float) lastZ)
                        .color(ColorUtil.r(colorD), ColorUtil.g(colorD), ColorUtil.b(colorD), ColorUtil.a(colorD) / 2).endVertex();

                bufferbuilder.pos(ms.getLast().getMatrix(), (float) x, (float) y, (float) z)
                        .color(ColorUtil.r(color2D), ColorUtil.g(color2D), ColorUtil.b(color2D), ColorUtil.a(color2D) / 2).endVertex();
                bufferbuilder.pos(ms.getLast().getMatrix(), (float) x, (float) y + height, (float) z)
                        .color(ColorUtil.r(colorD), ColorUtil.g(colorD), ColorUtil.b(colorD), ColorUtil.a(colorD) / 2).endVertex();

                tessellator.draw();

                bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

                bufferbuilder.pos(ms.getLast().getMatrix(), (float) lastX, (float) lastY + height, (float) lastZ)
                        .color(ColorUtil.r(color2), ColorUtil.g(color2), ColorUtil.b(color2), ColorUtil.a(color2)).endVertex();
                bufferbuilder.pos(ms.getLast().getMatrix(), (float) x, (float) y + height, (float) z)
                        .color(ColorUtil.r(color), ColorUtil.g(color), ColorUtil.b(color), ColorUtil.a(color)).endVertex();

                tessellator.draw();

                bufferbuilder.begin(begin, DefaultVertexFormats.POSITION_COLOR);

                bufferbuilder.pos(ms.getLast().getMatrix(), (float) lastX, (float) lastY + height, (float) lastZ)
                        .color(ColorUtil.r(color2), ColorUtil.g(color2), ColorUtil.b(color2), ColorUtil.a(color2)).endVertex();
                bufferbuilder.pos(ms.getLast().getMatrix(), (float) x, (float) y + height + 0.5f, (float) z)
                        .color(ColorUtil.r(nonA), ColorUtil.g(nonA), ColorUtil.b(nonA), ColorUtil.a(nonA)).endVertex();

                bufferbuilder.pos(ms.getLast().getMatrix(), (float) lastX, (float) lastY + height, (float) lastZ)
                        .color(ColorUtil.r(color2), ColorUtil.g(color2), ColorUtil.b(color2), ColorUtil.a(color2)).endVertex();
                bufferbuilder.pos(ms.getLast().getMatrix(), (float) x, (float) y + height + 0.5f, (float) z)
                        .color(ColorUtil.r(nonA), ColorUtil.g(nonA), ColorUtil.b(nonA), ColorUtil.a(nonA)).endVertex();


                tessellator.draw();
            }
            RenderUtil.endRender();
            ms.pop();
        }
        Collections.reverse(trails);
    };

    public class Trail {
        public Vector3d pos;
        public Vector3d prevPos;
        public long time;
        public TimerUtil timer;
        public int index;
        public float height;

        public Trail(float height,Vector3d prevPos,Vector3d pos, long time, int index) {
            this.pos = pos;
            this.prevPos = prevPos;
            this.height = height;

            this.time = time;
            this.index = index;
            this.timer = new TimerUtil();
        }
    }
}
