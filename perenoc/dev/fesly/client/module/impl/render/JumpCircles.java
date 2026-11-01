package dev.fesly.client.module.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.notification.Notification;
import dev.fesly.client.notification.NotificationManager;
import dev.fesly.client.notification.NotificationType;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.Priorities;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.other.TickEvent;
import dev.fesly.impl.event.impl.player.PlayerJumpEvent;
import dev.fesly.impl.event.impl.render.WorldRenderEvent;
import dev.fesly.impl.event.impl.render.WorldRenderLastEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.time.TimerUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.EnumSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.util.MathUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.*;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_COLOR;

@ModuleInfo(
        name = "JumpCircles",
        category = Category.RENDER,
        description = "Какая то хуета под ногами"
)
@Getter
@Setter
public class JumpCircles  extends Module {


    public ArrayList<Circle> circles = new ArrayList();
    private boolean sendNotification = false;

    private EnumMode mode = EnumMode.Default;
    public enum EnumMode {
        Default,
        Hell,
        Dollar,
        Bitcoin,
        SmileFace
    }
    @Override
    public void init() {
        addSetting("mode",new EnumSetting<>("Mode",this,this::getMode,this::setMode,EnumMode.class));
        addSetting("sendNotification", new BooleanSetting("Send Notification",this, this::isSendNotification, this::setSendNotification));
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        circles.clear();
    }

    @EventLink
    public Listener<PlayerJumpEvent> onJump = evt -> {
        ClientPlayerEntity t = mc.player;
        if (mc.player.isOnGround()) {
            if (circles.size() > 20) circles.remove(0);
            if (sendNotification)
                NotificationManager.sendNotification(new Notification("Spawned Circle | Size : " + circles.size(), NotificationType.INFO));
            this.circles.add(new Circle(new Vector3d(t.getPosX(), t.getPosY(), t.getPosZ()), (long) (10000.0f), this.circles.size()));
        }
    };
    @EventLink
    public Listener<TickEvent> onPreTick = evt -> {
        circles.removeIf(c -> c.timer.hasReached((int)c.time));
    };

    public static MatrixStack matrixFrom(MatrixStack matrices, ActiveRenderInfo camera) {

        matrices.rotate(Vector3f.XP.rotationDegrees(camera.getPitch()));
        matrices.rotate(Vector3f.YP.rotationDegrees(camera.getYaw() + 180.0F));

        return matrices;
    }


    @EventLink(Priorities.VERY_HIGH)
    public Listener<WorldRenderEvent> onRender = evt -> {
        MatrixStack ms = matrixFrom(evt.getMatrixStack(),mc.gameRenderer.getActiveRenderInfo());
        double ix = -((mc.getRenderManager().info.getProjectedView().getX()));
        double iy = -((mc.getRenderManager().info.getProjectedView().getY()));
        double iz = -((mc.getRenderManager().info.getProjectedView().getZ()));

        Collections.reverse(circles);
        for (Circle c : circles) {
            double x = c.positionVector.x - c.factor / 2;
            double y = c.positionVector.y;
            double z = c.positionVector.z - c.factor / 2;

            c.factor = MathUtil.lerp(c.factor, 2, 0.15f);

            c.alpha = MathUtil.lerp(c.alpha, 0, 0.015f);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            ms.push();
            ms.translate(ix, iy, iz);
            ms.translate(x, y, z);
            ms.rotate(new Quaternion(new Vector3f(1, 0, 0), 90, true));

            enableBlend();
            disableAlphaTest();
            depthMask(false);
            //RenderUtil.setupRender();

            float rsp = (float) (c.timer.getTimePassed()) / (float) (c.time);

            GL11.glAlphaFunc(GL11.GL_GREATER, 0.02f);
            GL11.glDisable(GL11.GL_POINT_SMOOTH);

            float apc = 1.0f - rsp;
            apc = (double) apc >= 0.85 ? (1.0f - apc) * 6 : apc;
            apc = (float) MathUtils.easeInOutQuad(apc, 1);
            apc = MathHelper.clamp(apc, 0.0f, 1.0f);
            int a = (int) MathHelper.clamp(apc * 255.0f, 0.0f, 255.0f);

            Color colorI = ColorUtil.interpolateTwoColors(15, (int) (4),
                    ClientUtil.getOneClientColor(), ClientUtil.getTwoClientColor());
            Color colorB = ColorUtil.interpolateTwoColors(15, (int) (6),
                    ClientUtil.getTwoClientColor(), ClientUtil.getThreeClientColor());

            colorI = Color.RED;
            colorB = ClientUtil.getOneClientColor();
            int bs = 2;
            int bs2 = 0;
            bs = bs * 4;

            float b = 0.5f;
            int finalBs = bs;
            String path = "icons/visuals/img_1.png";
            boolean renderColor = false;
            if (mode.equals(EnumMode.Dollar)) {
                path = "icons/visuals/img_2.png";
                renderColor = true;
            }else if (mode.equals(EnumMode.Bitcoin)) {
                path = "icons/visuals/img_3.png";
                renderColor = true;
            }else if (mode.equals(EnumMode.SmileFace)) {
                path = "icons/visuals/img_4.png";
                renderColor = true;
            }else if (mode.equals(EnumMode.Hell)) {
                path = "icons/visuals/img_5.png";
            }
            if (renderColor) {
                colorB = Color.WHITE;
                colorI = Color.WHITE;
            }
            int color = ColorUtil.withAlpha(colorB, (int) (a)).getRGB();
            int color2 = ColorUtil.withAlpha(colorI, a).getRGB();

            //bufferbuilder.begin(begin, DefaultVertexFormats.POSITION_COLOR);

            enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            RenderSystem.enableDepthTest();

            mc.getTextureManager().bindTexture(new ResourceLocation("nova",path));

            bufferbuilder.begin(GL_QUADS, POSITION_TEX_COLOR);

            bufferbuilder.pos(ms.getLast().getMatrix(), 0, 0, 0).tex(0, 0).color(
                    ColorUtil.r(color), ColorUtil.g(color), ColorUtil.b(color), ColorUtil.a(color)
            ).endVertex();
            bufferbuilder.pos(ms.getLast().getMatrix(),0, c.factor, 0).tex(0, 1).color(
                    ColorUtil.r(color2), ColorUtil.g(color2), ColorUtil.b(color2), ColorUtil.a(color)).endVertex();
            bufferbuilder.pos(ms.getLast().getMatrix(),c.factor, c.factor, 0).tex(1, 1).color(
                    ColorUtil.r(color), ColorUtil.g(color), ColorUtil.b(color), ColorUtil.a(color)).endVertex();
            bufferbuilder.pos(ms.getLast().getMatrix(),c.factor, 0, 0).tex(1, 0).color(
                    ColorUtil.r(color), ColorUtil.g(color), ColorUtil.b(color), ColorUtil.a(color)).endVertex();

            tessellator.draw();


            enableAlphaTest();
            disableBlend();
            depthMask(true);
            ms.pop();
        }
        Collections.reverse(circles);
    };

    public class Circle {
        public Vector3d positionVector;
        public long time;
        public TimerUtil timer;
        public int index;
        public float alpha = 255, factor = 0;
        public boolean reached = false;


        public Circle(Vector3d pos, long time, int index) {
            this.positionVector = pos;

            this.time = time;
            this.index = index;
            this.timer = new TimerUtil();
        }
    }
}

