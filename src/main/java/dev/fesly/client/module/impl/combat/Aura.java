package dev.fesly.client.module.impl.combat;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.render.OtherParticles;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.MoveInputEvent;
import dev.fesly.impl.event.impl.motion.JumpEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.event.impl.motion.StrafeEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.event.impl.other.AttackEvent;
import dev.fesly.impl.event.impl.render.BossHealthRenderEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.player.MoveUtil;
import dev.fesly.impl.util.render.GLUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.rotation.RotationUtil;
import dev.fesly.impl.util.shader.GradientUtil;
import dev.fesly.impl.util.time.TimerUtil;
import dev.fesly.impl.value.defaultValues.impl.BooleanValue;
import dev.fesly.impl.value.defaultValues.impl.NumberValue;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.EnumSetting;
import dev.fesly.impl.value.secondValues.MultiEnumSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.optifine.render.RenderUtils;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.util.EnumSet;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import static dev.fesly.impl.util.render.HudEffects.detime;
@ModuleInfo(name = "Aura", description = "Бьет Женщин И Детей", category = Category.COMBAT)
public class Aura extends Module {
    private float range = 3.0F;
    private float preRotationRange = 1.5F;
    private BooleanValue onlyCrits;
    private BooleanValue shieldBreak;
    private BooleanValue movementCorrection;
    private BooleanValue stopSprint;
    private EnumSet<Targets> targets = EnumSet.of(Targets.Players);
    public enum Targets implements Predicate<Entity> {
        Players(e -> e instanceof Player), Mobs(e -> e instanceof PathfinderMob);
        private final Predicate<Entity> predicate;
        @Override
        public boolean test(Entity entity) {
            return predicate.test(entity);
        }
        @java.lang.SuppressWarnings("all")
        private Targets(final Predicate<Entity> predicate) {
            this.predicate = predicate;
        }
    }
    private BooleanSetting renderMarker;
    public OtherParticles.RenderMode renderMode = OtherParticles.RenderMode.Bloom;
    @Override
    public void init() {
        super.init();
        renderMarker = new BooleanSetting("Render Marker", this, true);
        onlyCrits = new BooleanValue("Crits", this, true);
        shieldBreak = new BooleanValue("Shield Break", this, true);
        movementCorrection = new BooleanValue("Movement Fix", this, true);
        stopSprint = new BooleanValue("Stop Sprint", this, true);
        addSetting("targets", new MultiEnumSetting<>("Targets", this, this::getTargets, this::setTargets, Targets.class));
        addSetting("range", new NumberSetting("Range", this, this::getRange, this::setRange, "%.1f", 3, 6));
        addSetting("preRotationRange", new NumberSetting("PreRotationRange", this, this::getPreRotationRange, this::setPreRotationRange, "%.1f", 1, 6));
        addSetting("onlyCrits", new BooleanSetting("Only Crits", this, onlyCrits::getValue, onlyCrits::setValue));
        addSetting("shieldBreak", new BooleanSetting("Shield Break", this, shieldBreak::getValue, shieldBreak::setValue));
        addSetting("movementCorrection", new BooleanSetting("Movement Correction", this, movementCorrection::getValue, movementCorrection::setValue));
        addSetting("stopSprint", new BooleanSetting("Stop Sprint", this, stopSprint::getValue, stopSprint::setValue));
        addSetting("renderMarker", renderMarker);
        addSetting("renderMode", new EnumSetting<>("Render Mode", this, () -> renderMarker.getGetter().getAsBoolean(), this::getRenderMode, this::setRenderMode, OtherParticles.RenderMode.class));
    }
    public static LivingEntity target;
    private float yaw;
    private float pitch;
    @Override
    public void onEnable() {
        super.onEnable();
        resetRotations();
    }
    @Override
    public void onDisable() {
        super.onDisable();
        target = null;
        resetRotations();
    }
    private void resetRotations() {
        yaw = mc.player.rotationYaw;
        pitch = mc.player.rotationPitch;
    }
    @EventLink
    private final Listener<MoveInputEvent> onMove = event -> {
        if (movementCorrection.getValue() && target != null) {
        } else
        {
            resetRotations();
        }
    };
    @EventLink
    private final Listener<StrafeEvent> onStrafe = event -> {
        if (movementCorrection.getValue() && target != null) {
            setRotation();
            event.setYaw(yaw);
        } else {
            resetRotations();
        }
    };
    @EventLink
    private final Listener<JumpEvent> onJump = event -> {
        if (movementCorrection.getValue() && target != null) {
            setRotation();
            event.setYaw(yaw);
        } else {
            resetRotations();
        }
    };
    @EventLink
    private final Listener<MotionEvent> onMotion = event -> {
        updateTarget();
        if (!movementCorrection.getValue()) setRotation();
        if (target != null) {
            event.setYaw(yaw);
            event.setPitch(pitch);
        } else {
            resetRotations();
        }
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = RotationUtil.calculateCorrectYawOffset(yaw);
        mc.player.rotationPitchHead = pitch;
    };
    @EventLink
    private final Listener<UpdateEvent> onUpdate = event -> {
        updateTarget();
        doAttack();
    };
    private void setRotation() {
        if (target != null && distanceToTarget() <= (range + preRotationRange)) {
            float[] rotation = RotationUtil.getDefault(mc.player, target, false);
            yaw = rotation[0];
            pitch = rotation[1];
        } else {
            resetRotations();
        }
    }
    private double distanceToTarget() {
        return mc.player.getDistance(target);
    }
    private void doAttack() {
        if (target != null) {
            if (distanceToTarget() <= range) {
                boolean check = (mc.player.abilities.isFlying || mc.player.isElytraFlying() || mc.player.isInWater() || mc.player.isInLava() || mc.player.isOnLadder() || mc.player.isRidingHorse() || (mc.player.isJumping && MoveUtil.isBlockAbove()));
                if (!check && onlyCrits.getValue() && mc.player.fallDistance < 0.1F) return;
                if (mc.player.getCooledAttackStrength(1.0F) != 1.0F) {
                    return;
                }
                attackTarget();
                shieldBreak();
            }
        }
    }
    private final TimerUtil shieldCounter = new TimerUtil();
    private void attackTarget() {
        assert mc.playerController != null;
        final AttackEvent event = new AttackEvent(target);
        Nova.getInstance().getEventBus().handle(event);
        if (event.isCancelled()) return;
        if (mc.player.isSprinting() && stopSprint.getValue()) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
        }
        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);
    }
    private void shieldBreak() {
        if (!shieldBreak.getValue()) return;
        if (target.isActiveItemStackBlocking() && shieldCounter.hasReached(550)) {
            for (int i = 0; i < 9; i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() instanceof AxeItem) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
                    mc.player.connection.sendPacket(new CUseEntityPacket(target, mc.player.isSneaking()));
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    break;
                }
            }
            shieldCounter.reset();
        }
    }
    private void updateTarget() {
        if ((target == null || !target.isAlive() || distanceToTarget() > (range + preRotationRange))) {
            target = getTarget();
        }
    }
    private LivingEntity getTarget() {
        return (LivingEntity) StreamSupport.stream(mc.world.getAllEntities().spliterator(), true).filter(ENTITY_FILTER).findFirst().orElse(null);
    }
    private final Predicate<Entity> ENTITY_FILTER = entity -> entity.isAlive() && mc.player.getDistance(entity) <= range + preRotationRange && entity instanceof LivingEntity && !(Nova.getInstance().getFriendManager().isFriend(entity.getScoreboardName())) && ((LivingEntity) entity).getHealth() != 0 && entity != mc.player && !(entity instanceof ArmorStand) && entity.isAlive() && !AntiBot.checkBot((LivingEntity) entity);
    @EventLink
    public Listener<BossHealthRenderEvent> onRender = event -> {
        if (!renderMarker.getValue() || !isEnabled()) return;
        if (renderMode.equals(OtherParticles.RenderMode.Client)) IAccess.addRunnableWithChecks(() -> drawMarker(true, true));
         else if (renderMode.equals(OtherParticles.RenderMode.Bloom)) {
            IAccess.BLOOM_RUNNABLES.add(() -> drawMarker(true, true));
            drawMarker(true, false);
        }
    };
    public void drawMarker(boolean blur, boolean vega) {
        if (target == null) return;
        double x = RenderUtils.interporate(mc.getRenderPartialTicks(), target.lastTickPosX, target.getPosX());
        double y = RenderUtils.interporate(mc.getRenderPartialTicks(), target.lastTickPosY, target.getPosY());
        double z = RenderUtils.interporate(mc.getRenderPartialTicks(), target.lastTickPosZ, target.getPosZ());
        if (mc.player == null) return;
        double[] marker = RenderUtil.worldToScreen(x, y + target.getHeight() / 1.5F + (target.getDistance(mc.player) / 20.0F), z);
        Font font = Fonts.ROBOTO_REGULAR.get(15);
        if (marker == null) return;
        float width = 21;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) marker[0], (float) marker[1], 0.0F);
        GL11.glScaled(1, 1, 1);
        GL11.glTranslatef((float) (-marker[0]) - (width / 2), (float) (-marker[1]), 0.0F);
        float val = 0.5653879F;
        ScaleUtil.scaleNonMatrix((float) marker[0] + width / 2, (float) (marker[1] - 2 + font.getHeight() / 2), Math.max(val, 0.1F));
        float size = width * 2;
        float b = (float) (width * 1.7F + ((Math.sin(((System.currentTimeMillis() - detime) / 200.0F)) * 2.0F)));
        float xM = 8;
        if (blur) {
            Runnable rotateComponent = () -> RenderUtil.drawImage(new ResourceLocation("nova", "icons/ui/arrow2.png"), (marker[0] - b - xM), marker[1], size, size, -1);
            Runnable rotateComponent2 = () -> RenderUtil.drawImage(new ResourceLocation("nova", "icons/ui/arrow.png"), (marker[0] + b - xM), marker[1], size, size, -1);
            Runnable rotateComponent3 = () -> RenderUtil.drawImage(new ResourceLocation("nova", "icons/ui/arrow3.png"), (marker[0] - xM), (marker[1] + b), size, size, -1);
            Runnable rotateComponent4 = () -> RenderUtil.drawImage(new ResourceLocation("nova", "icons/ui/arrow4.png"), (marker[0] - xM), (marker[1] - b), size, size, -1);
            Color renderColor1 = ClientUtil.getOneClientColor();
            Color renderColor2 = ClientUtil.getTwoClientColor();
            Color renderColor3 = ClientUtil.getThreeClientColor();
            Color renderColor4 = ClientUtil.getFourClientColor();
            Color gradientColor1 = ColorUtil.interpolateTwoColors(15, 0, renderColor1, renderColor2);
            Color gradientColor2 = ColorUtil.interpolateTwoColors(15, 15, renderColor3, renderColor4);
            Color gradientColor3 = ColorUtil.interpolateTwoColors(15, 30, renderColor1, renderColor2);
            Color gradientColor4 = ColorUtil.interpolateTwoColors(15, 45, renderColor3, renderColor4);
            if (renderMode.equals(OtherParticles.RenderMode.Bloom)) {
                GLUtil.rotate((float) (marker[0] - 2), (float) marker[1], (float) (Math.cos((System.currentTimeMillis() - detime) / 200.0F) * 5), () -> GradientUtil.applyGradient((float) (marker[0] - b - xM), (float) (marker[1] + b), size * 4, size * 4, vega ? 1.0F : 0.6F, gradientColor1, gradientColor2, gradientColor3, gradientColor4, () -> {
                    rotateComponent.run();
                    rotateComponent2.run();
                    rotateComponent3.run();
                    rotateComponent4.run();
                }));
            }
            if (renderMode.equals(OtherParticles.RenderMode.Client)) {
                GLUtil.rotate((float) (marker[0] - 2), (float) marker[1], (float) (Math.cos((System.currentTimeMillis() - detime) / 200.0F) * 5), () -> {
                    rotateComponent.run();
                    rotateComponent2.run();
                    rotateComponent3.run();
                    rotateComponent4.run();
                });
            }
        }
        ScaleUtil.scaleNonMatrix((float) marker[0] + width / 2, (float) (marker[1] - 2 + font.getHeight() / 2), 1.0F / (Math.max(val, 0.1F)));
        GL11.glPopMatrix();
    }
    @java.lang.SuppressWarnings("all")
    public float getRange() {
        return this.range;
    }
    @java.lang.SuppressWarnings("all")
    public float getPreRotationRange() {
        return this.preRotationRange;
    }
    @java.lang.SuppressWarnings("all")
    public BooleanValue getOnlyCrits() {
        return this.onlyCrits;
    }
    @java.lang.SuppressWarnings("all")
    public BooleanValue getShieldBreak() {
        return this.shieldBreak;
    }
    @java.lang.SuppressWarnings("all")
    public BooleanValue getMovementCorrection() {
        return this.movementCorrection;
    }
    @java.lang.SuppressWarnings("all")
    public BooleanValue getStopSprint() {
        return this.stopSprint;
    }
    @java.lang.SuppressWarnings("all")
    public EnumSet<Targets> getTargets() {
        return this.targets;
    }
    @java.lang.SuppressWarnings("all")
    public BooleanSetting getRenderMarker() {
        return this.renderMarker;
    }
    @java.lang.SuppressWarnings("all")
    public float getYaw() {
        return this.yaw;
    }
    @java.lang.SuppressWarnings("all")
    public float getPitch() {
        return this.pitch;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<MoveInputEvent> getOnMove() {
        return this.onMove;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<StrafeEvent> getOnStrafe() {
        return this.onStrafe;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<JumpEvent> getOnJump() {
        return this.onJump;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<MotionEvent> getOnMotion() {
        return this.onMotion;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<UpdateEvent> getOnUpdate() {
        return this.onUpdate;
    }
    @java.lang.SuppressWarnings("all")
    public TimerUtil getShieldCounter() {
        return this.shieldCounter;
    }
    @java.lang.SuppressWarnings("all")
    public Predicate<Entity> getENTITY_FILTER() {
        return this.ENTITY_FILTER;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<BossHealthRenderEvent> getOnRender() {
        return this.onRender;
    }
    @java.lang.SuppressWarnings("all")
    public void setRange(final float range) {
        this.range = range;
    }
    @java.lang.SuppressWarnings("all")
    public void setPreRotationRange(final float preRotationRange) {
        this.preRotationRange = preRotationRange;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnlyCrits(final BooleanValue onlyCrits) {
        this.onlyCrits = onlyCrits;
    }
    @java.lang.SuppressWarnings("all")
    public void setShieldBreak(final BooleanValue shieldBreak) {
        this.shieldBreak = shieldBreak;
    }
    @java.lang.SuppressWarnings("all")
    public void setMovementCorrection(final BooleanValue movementCorrection) {
        this.movementCorrection = movementCorrection;
    }
    @java.lang.SuppressWarnings("all")
    public void setStopSprint(final BooleanValue stopSprint) {
        this.stopSprint = stopSprint;
    }
    @java.lang.SuppressWarnings("all")
    public void setTargets(final EnumSet<Targets> targets) {
        this.targets = targets;
    }
    @java.lang.SuppressWarnings("all")
    public void setRenderMarker(final BooleanSetting renderMarker) {
        this.renderMarker = renderMarker;
    }
    @java.lang.SuppressWarnings("all")
    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }
    @java.lang.SuppressWarnings("all")
    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnRender(final Listener<BossHealthRenderEvent> onRender) {
        this.onRender = onRender;
    }
    @java.lang.SuppressWarnings("all")
    public OtherParticles.RenderMode getRenderMode() {
        return this.renderMode;
    }
    @java.lang.SuppressWarnings("all")
    public void setRenderMode(final OtherParticles.RenderMode renderMode) {
        this.renderMode = renderMode;
    }
}
