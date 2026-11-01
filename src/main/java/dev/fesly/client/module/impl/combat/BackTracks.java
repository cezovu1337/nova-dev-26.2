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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import java.awt.*;
@ModuleInfo(name = "BackTracks", description = "", category = Category.COMBAT)
public class BackTracks extends Module {
    private float saveTicks = 5;
    private boolean pingSync = true;
    private boolean noLimit = true;
    @Override
    protected void onEnable() {
        super.onEnable();
        ChatUtil.addText("Функция еще не до конца доработана, вас может кикать /" + " в некоторых случаях могут не идти удары. +" + TextFormatting.RED + "Используйте на свой страх и риск");
    }
    @Override
    public void init() {
        super.init();
        addSetting("pingSync", new BooleanSetting("Ping Sync", this, this::isPingSync, this::setPingSync));
        addSetting("noLimit", new BooleanSetting("No Limit", this, this::isNoLimit, this::setNoLimit));
        addSetting("saveTicks", new NumberSetting("Save Ticks", this, () -> !isNoLimit(), this::getSaveTicks, this::setSaveTicks, "%.0f", 1, 10));
    }
    long skip_packet_ka;
    long skip_packet_ct;
    long skip_packet_cwt;
    public boolean check() {
        return mc.player == null || mc.world == null;
    }
    @EventLink
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
    @EventLink
    public final Listener<DrawHudPostEvent> onWorldRender = event -> {
        assert mc.world != null;
        if (Aura.target == null) return;
        Entity entity = Aura.target;
        if (!entity.isAlive()) return;
        if (checkBool(entity.getPosX(), entity.lastTickPosX) || checkBool(entity.getPosY(), entity.lastTickPosY)) {
            if (entity.lastBoundingBoxes.size() > ((int) (saveTicks))) entity.lastBoundingBoxes.remove(1);
            entity.lastBoundingBoxes.removeIf(bounding -> bounding.timerUtil.hasReached(1000));
            entity.lastBoundingBoxes.add(new BackTrack(entity.getBoundingBox(), entity.getPositionVec(), entity.rotationYaw, (LivingEntity) entity));
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
        double[] vec = RenderUtil.worldToScreen(backTrack.position.x, backTrack.position.y + backTrack.entity.getHeight(), backTrack.position.z);
        double[] vec2 = RenderUtil.worldToScreen(backTrack.position.x, backTrack.position.y, backTrack.position.z);
        if (vec == null || vec2 == null) return;
        float size = entity.lastBoundingBoxes.size() + 1;
        float val = entity.lastBoundingBoxes.indexOf(backTrack) / size;
        float alpha = val / 4.0F;
        {
            float width = (float) Math.max(((vec[0] - vec2[0])), 32);
            RenderUtil.roundedRectangle((float) vec[0] - 16, (float) vec[1], (float) 32, (float) vec2[1] - vec[1], 6, new Color(1.0F, 1.0F, 1.0F, 0.2F));
        }
    };
    public boolean checkBool(double a, double b) {
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
    public class BackTrack {
        private net.minecraft.world.phys.AABB boundingBox;
        public Vector3d position;
        private float yaw;
        public LivingEntity entity;
        public TimerUtil timerUtil = new TimerUtil();
        public BackTrack(net.minecraft.world.phys.AABB bb, Vector3d pos, float yaw, LivingEntity entity) {
            timerUtil.reset();
            this.boundingBox = bb;
            this.position = pos;
            this.yaw = yaw;
            this.entity = entity;
        }
        @java.lang.SuppressWarnings("all")
        public net.minecraft.world.phys.AABB getBoundingBox() {
            return this.boundingBox;
        }
        @java.lang.SuppressWarnings("all")
        public Vector3d getPosition() {
            return this.position;
        }
        @java.lang.SuppressWarnings("all")
        public float getYaw() {
            return this.yaw;
        }
        @java.lang.SuppressWarnings("all")
        public LivingEntity getEntity() {
            return this.entity;
        }
        @java.lang.SuppressWarnings("all")
        public TimerUtil getTimerUtil() {
            return this.timerUtil;
        }
        @java.lang.SuppressWarnings("all")
        public void setBoundingBox(final AxisAlignedBB boundingBox) {
            this.boundingBox = boundingBox;
        }
        @java.lang.SuppressWarnings("all")
        public void setPosition(final Vector3d position) {
            this.position = position;
        }
        @java.lang.SuppressWarnings("all")
        public void setYaw(final float yaw) {
            this.yaw = yaw;
        }
        @java.lang.SuppressWarnings("all")
        public void setEntity(final LivingEntity entity) {
            this.entity = entity;
        }
        @java.lang.SuppressWarnings("all")
        public void setTimerUtil(final TimerUtil timerUtil) {
            this.timerUtil = timerUtil;
        }
    }
    @java.lang.SuppressWarnings("all")
    public float getSaveTicks() {
        return this.saveTicks;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isPingSync() {
        return this.pingSync;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isNoLimit() {
        return this.noLimit;
    }
    @java.lang.SuppressWarnings("all")
    public long getSkip_packet_ka() {
        return this.skip_packet_ka;
    }
    @java.lang.SuppressWarnings("all")
    public long getSkip_packet_ct() {
        return this.skip_packet_ct;
    }
    @java.lang.SuppressWarnings("all")
    public long getSkip_packet_cwt() {
        return this.skip_packet_cwt;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<PacketSendEvent> getOnPacketSend() {
        return this.onPacketSend;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<DrawHudPostEvent> getOnWorldRender() {
        return this.onWorldRender;
    }
    @java.lang.SuppressWarnings("all")
    public void setSaveTicks(final float saveTicks) {
        this.saveTicks = saveTicks;
    }
    @java.lang.SuppressWarnings("all")
    public void setPingSync(final boolean pingSync) {
        this.pingSync = pingSync;
    }
    @java.lang.SuppressWarnings("all")
    public void setNoLimit(final boolean noLimit) {
        this.noLimit = noLimit;
    }
    @java.lang.SuppressWarnings("all")
    public void setSkip_packet_ka(final long skip_packet_ka) {
        this.skip_packet_ka = skip_packet_ka;
    }
    @java.lang.SuppressWarnings("all")
    public void setSkip_packet_ct(final long skip_packet_ct) {
        this.skip_packet_ct = skip_packet_ct;
    }
    @java.lang.SuppressWarnings("all")
    public void setSkip_packet_cwt(final long skip_packet_cwt) {
        this.skip_packet_cwt = skip_packet_cwt;
    }
}
