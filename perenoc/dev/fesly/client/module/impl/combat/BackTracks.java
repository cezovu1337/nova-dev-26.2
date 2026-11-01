package dev.fesly.client.module.impl.combat;

import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.render.DrawHudPostEvent;
import dev.fesly.impl.util.chat.ChatUtil;
import dev.fesly.impl.util.player.ThreadUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.time.TimerUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleInfo(
        name = "BackTracks",
        description = "",
        category = Category.COMBAT
)
@Getter
@Setter
public class BackTracks extends Module {

    private float saveTicks = 5;

    private boolean pingSync = true;

    private boolean noLimit = true;

    @Override
    protected void onEnable() {
        super.onEnable();
        ChatUtil.addText("Функция еще не до конца доработана, вас может кикать /" +
                " в некоторых случаях могут не идти удары. +" + TextFormatting.RED + "Используйте на свой страх и риск");
    }

    @Override
    public void init() {
        super.init();
        addSetting("pingSync", new BooleanSetting("Ping Sync", this, this::isPingSync,this::setPingSync));

        addSetting("noLimit", new BooleanSetting("No Limit", this, this::isNoLimit,this::setNoLimit));

        addSetting("saveTicks", new NumberSetting("Save Ticks", this,() -> !isNoLimit(), this::getSaveTicks,this::setSaveTicks,"%.0f",1,10));

    }

    long skip_packet_ka, skip_packet_ct, skip_packet_cwt;

    public boolean check() {
        return mc.player == null || mc.world == null;
    }

    @EventLink()
    public final Listener<PacketSendEvent> onPacketSend = event -> {
       if (pingSync || check()) return;

       if (event.getPacket() instanceof CKeepAlivePacket) {
           if (((CKeepAlivePacket) event.getPacket()).getKey() == skip_packet_ka) {
               return;
           }
           event.setCancelled();
           ThreadUtil.run(() -> {
               skip_packet_ka = ((CKeepAlivePacket) event.getPacket()).getKey();
               mc.player.connection.sendPacket(event.getPacket());
           }, (long) (saveTicks * 150));
       }
       if (event.getPacket() instanceof CConfirmTransactionPacket) {
           if (((CConfirmTransactionPacket) event.getPacket()).getUid() == skip_packet_ct) {
               return;
           }
           if (((CConfirmTransactionPacket) event.getPacket()).getWindowId() == skip_packet_cwt) {
               return;
           }
           event.setCancelled();
           ThreadUtil.run(() -> {
               skip_packet_ct = ((CConfirmTransactionPacket) event.getPacket()).getUid();
               skip_packet_cwt = ((CConfirmTransactionPacket) event.getPacket()).getWindowId();
               mc.player.connection.sendPacket(event.getPacket());
           }, (long) (saveTicks * 150));
       }
    };
    @EventLink()
    public final Listener<DrawHudPostEvent> onWorldRender = event -> {

        assert mc.world != null;
        if (Aura.target == null) return;
        Entity entity = Aura.target;
        if (!entity.isAlive()) return;
            if (checkBool(entity.getPosX(), entity.lastTickPosX) ||
                    checkBool(entity.getPosY(), entity.lastTickPosY)
            ) {
                if (entity.lastBoundingBoxes.size() > ((int) (saveTicks))) entity.lastBoundingBoxes.remove(1);
                entity.lastBoundingBoxes.removeIf(bounding -> bounding.timerUtil.hasReached(1000));

                entity.lastBoundingBoxes.add(new BackTrack(entity.getBoundingBox(), entity.getPositionVec(), entity.rotationYaw, (LivingEntity) entity));
                //System.out.println(entity.lastBoundingBoxes.size());
                if (!entity.lastBoundingBoxes.isEmpty() && entity.lastBoundingBoxes.size() > 2) {
                    if (((LivingEntity) entity).hurtTime > 0) {
                        entity.hurtTimer.reset();
                    }
                    if (!entity.hurtTimer.hasReached(2000)) {
                        BackTrack track = entity.lastBoundingBoxes.get(0);
                        entity.setPosition(track.position.x, track.position.y, track.position.z);
                    }
                }
            }
            if (entity.lastBoundingBoxes.isEmpty()) return;
            BackTrack backTrack = entity.lastBoundingBoxes.get(entity.lastBoundingBoxes.size() - 1);

                if (backTrack.timerUtil.hasReached(1000)) return;
                /*drawCylinder(backTrack.position, backTrack.entity.getHeight(), 3, backTrack.yaw);*/
                double[] vec = RenderUtil.worldToScreen(backTrack.position.x, backTrack.position.y + backTrack.entity.getHeight(), backTrack.position.z);
                double[] vec2 = RenderUtil.worldToScreen(backTrack.position.x, backTrack.position.y, backTrack.position.z);

                if (vec == null || vec2 == null) return;
                float size = entity.lastBoundingBoxes.size() + 1;
                float val = entity.lastBoundingBoxes.indexOf(backTrack) / size;
                float alpha = val / 4f;

                {
                    float width = (float) Math.max(((vec[0] - vec2[0])), 32);

                    /*IAccess.addRunnableWithChecks(() -> {*/
                        RenderUtil.roundedRectangle((float) vec[0] - 16, (float) vec[1], (float) 32, (float) vec2[1] - vec[1], 6, new Color(1f, 1f, 1f, 0.2f));
                    /*});*/
                }
       /* Vector3d position = new Vector3d(10, 5, 20);
        float height = 10.0f;
        float radius = 3.0f;
        float yaw = 45.0f;

        drawCylinder(position, height, radius, yaw);*/
    };

    public boolean checkBool(double a,double b) {
        return a != b;
    }

    public static void drawCylinder(Vector3d pos, float height, float radius, float yaw) {
        GL11.glPushMatrix();
        GL11.glTranslated(pos.x, pos.y, pos.z);
        GL11.glRotatef(yaw, 0, 1, 0);

        int sides = 32;

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3f(0, height, 0);
        for (int i = 0; i <= sides; i++) {
            double angle = Math.PI * 2 * i / sides;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            GL11.glVertex3f(x, height, z);
        }
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3f(0, 0, 0);
        for (int i = sides; i >= 0; i--) {
            double angle = Math.PI * 2 * i / sides;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            GL11.glVertex3f(x, 0, z);
        }
        GL11.glEnd();

        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= sides; i++) {
            double angle = Math.PI * 2 * i / sides;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            GL11.glVertex3f(x, 0, z);
            GL11.glVertex3f(x, height, z);
        }
        GL11.glEnd();

        GL11.glPopMatrix();
    }


    @Getter
    @Setter
    public class BackTrack {
        private AxisAlignedBB boundingBox;
        public Vector3d position;
        private float yaw;

        public LivingEntity entity;

        private TimerUtil timerUtil = new TimerUtil();


        public BackTrack(AxisAlignedBB bb, Vector3d pos, float yaw, LivingEntity entity) {
            timerUtil.reset();
            this.boundingBox = bb;
            this.position = pos;
            this.yaw = yaw;
            this.entity = entity;
        }
    }

}
