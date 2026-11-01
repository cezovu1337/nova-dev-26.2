package dev.fesly.client.module.impl.render;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.Priorities;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.other.AttackEvent;
import dev.fesly.impl.event.impl.other.TickEvent;
import dev.fesly.impl.event.impl.render.BossHealthRenderEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.DecelerateAnimation;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.GLUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.EnumSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;
@ModuleInfo(name = "Other Particles", description = "При дамаге создает вокруг энтити \n" + " красивые партиклы", category = Category.RENDER)
public class OtherParticles extends Module {
    public List<Particle> particles = new ArrayList<>();
    public boolean fromSelf = true;
    public OtherParticlesMode otherParticles = OtherParticlesMode.Lightning;
    public RenderMode renderMode = RenderMode.Bloom;
    public float slowDown = 20.0F;
    public float slowDownFromSelf = 20.0F;
    public enum OtherParticlesMode {
        Lightning, Youtube, HeartOutline, Heart, Star;
    }
    public enum RenderMode {
        Client, Bloom;
    }
    private boolean rainbowMode = false;
    @Override
    public void init() {
        super.init();
        addSetting("otherParticles", new EnumSetting<>("Spawn Particles", this, this::getOtherParticles, this::setOtherParticles, OtherParticlesMode.class));
        addSetting("renderMode", new EnumSetting<>("Render Mode", this, this::getRenderMode, this::setRenderMode, RenderMode.class));
        addSetting("slowdown", new NumberSetting("SlowDown", this, this::getSlowDown, this::setSlowDown, "%.2f", 5, 30));
        addSetting("slowdownSelf", new NumberSetting("SlowDownSelf", this, this::isFromSelf, this::getSlowDownFromSelf, this::setSlowDownFromSelf, "%.2f", 5, 30));
        addSetting("rainbowMode", new BooleanSetting("Rainbow Mode", this, () -> this.renderMode.equals(RenderMode.Bloom), this::isRainbowMode, this::setRainbowMode).setDescription("Партиклы в цветах радуги!"));
        addSetting("spawnFromSelf", new BooleanSetting("From Self", this, this::isFromSelf, this::setFromSelf));
    }
    @EventLink
    public Listener<TickEvent> onTick = event -> {
        particles.removeIf(particle -> {
            if (mc.player.getDistanceSq(particle.x, particle.y, particle.z) > 5550) return true;
            if (particles.size() > 100 && particles.indexOf(particle) < 10) return true;
            if (particle.check()) return true;
            assert mc.player != null;
            return mc.player.getDistanceSq(new Vector3d(particle.x, particle.y, particle.z)) > 30;
        });
    };
    @EventLink(Priorities.VERY_LOW)
    public Listener<BossHealthRenderEvent> onHudPre = evt -> {
        if (renderMode.equals(RenderMode.Client)) IAccess.addRunnableWithChecks(render(true));
         else {
            render(true).run();
            IAccess.BLOOM_RUNNABLES.add(render(true));
        }
        var player = mc.player;
        if (fromSelf) if (player.getPosX() != player.lastTickPosX || player.getPosZ() != player.lastTickPosZ || player.getPosY() != player.lastTickPosY) {
            if (Math.random() < 0.2) {
                particles.add(new Particle(false, getRandomNumber(player.getPosX(), (float) 0.3F), getRandomY(player.getPosY() + 0.4F, player.getHeight() - 0.8F), getRandomNumber(player.getPosZ(), (float) 0.3F), otherParticles));
            }
        }
    };
    @EventLink
    public Listener<AttackEvent> onAttack = evt -> {
        var player = evt.getTarget();
        double x2 = player.getPosX() + (player.lastTickPosX - player.getPosX());
        double y2 = player.getPosY() + (player.lastTickPosY - player.getPosY());
        double z2 = player.getPosZ() + (player.lastTickPosZ - player.getPosZ());
        float dst = mc.player.getDistance(player) / 125;
        {
            for (int i = 0; i < 20; i++) {
                particles.add(new Particle(true, (float) getRandomNumber(player.getPosX(), 0.5F), (float) MathUtil.getRandom(player.getPosY(), player.getPosY() + player.getHeight()), getRandomNumber(player.getPosZ(), 0.5F), otherParticles));
            }
        }
    };
    public Runnable render(boolean blur) {
        if (mc.world == null) return () -> {
        };
        return () -> {
            for (Particle particle : particles) {
                boolean lightning = particle.mode.equals(OtherParticlesMode.Lightning);
                boolean star = particle.mode.equals(OtherParticlesMode.Star);
                particle.updatePositions(mc.getRenderPartialTicks());
                double[] vec = RenderUtil.worldToScreen(particle.x, particle.y, particle.z);
                if (vec == null) continue;
                float dst = (float) mc.player.getDistanceSq(new Vector3d(particle.x, particle.y, particle.z)) / 50;
                float val = 0.9F;
                float vv = Math.max(val, 0.5F);
                float size = lightning ? 8 : 8;
                particle.x2D = (float) vec[0];
                particle.y2D = (float) vec[1];
                GL11.glPushMatrix();
                GL11.glTranslatef((float) vec[0], (float) vec[1], 0.0F);
                GL11.glScaled(1, 1, 1);
                GL11.glTranslatef((float) (-vec[0]) - (size / 2), (float) (-vec[1]), 0.0F);
                if (particle.checkRemove) {
                    GLUtil.startRotate((float) vec[0] + size / 2, (float) (vec[1] - 2), (float) Math.sin(particle.motionX) * 360);
                }
                ScaleUtil.scaleNonMatrix((float) vec[0] + size / 2, (float) (vec[1] - 2), vv);
                if (blur) {
                    String png = "";
                    if (particle.mode.equals(OtherParticlesMode.Star)) {
                        png = "icons/visuals/star.png";
                    } else if (particle.mode.equals(OtherParticlesMode.Lightning)) {
                        png = "icons/visuals/lightningW.png";
                    } else if (particle.mode.equals(OtherParticlesMode.Heart)) {
                        png = "icons/visuals/heart.png";
                    } else if (particle.mode.equals(OtherParticlesMode.HeartOutline)) {
                        png = "icons/visuals/heartoutline2.png";
                    } else if (particle.mode.equals(OtherParticlesMode.Youtube)) {
                        png = "icons/visuals/youtube.png";
                    }
                    RenderUtil.drawImage(new ResourceLocation("nova", png), (float) (vec[0] - 1), (float) vec[1], size, size, particle.color);
                }
                ScaleUtil.scaleNonMatrix((float) vec[0] + size / 2, (float) (vec[1] - 2), 1.0F / (vv));
                if (particle.checkRemove) {
                    GLUtil.endRotate();
                }
                GL11.glPopMatrix();
            }
        };
    }
    public float getRandomNumber(double value, float t) {
        return (float) MathUtil.getRandom(value - t, value + t);
    }
    public float getRandomY(double value, float t) {
        return (float) MathUtil.getRandom(value, value + t);
    }
    public class Particle {
        public Animation animation = new DecelerateAnimation(50000, 1.0F, Direction.FORWARDS);
        public float x;
        public float y;
        public float z;
        public float x2D;
        public float y2D;
        private float motionX;
        private float motionY;
        private float motionZ;
        double gravity;
        double friction;
        double slowdown;
        public int color;
        public boolean checkRemove;
        public OtherParticlesMode mode;
        public Particle(boolean checkRemove, float x, float y, float z, OtherParticlesMode mode) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.checkRemove = checkRemove;
            this.mode = mode;
            slowdown = checkRemove ? (slowDown * 10) : (slowDownFromSelf * 100);
            gravity = 1;
            friction = 0.15F;
            motionX = (float) ((float) (MathUtil.getRandom(-0.848F, 0.848F) / slowdown));
            motionY = (float) ((float) (MathUtil.getRandom(-0.848F, 0.848F) / slowdown));
            motionZ = (float) ((float) (MathUtil.getRandom(-0.848F, 0.848F) / slowdown));
            double random = Math.random();
            color = random > 0.5 ? ClientUtil.getThreeClientColor().getRGB() : ClientUtil.getTwoClientColor().getRGB();
            if (renderMode.equals(RenderMode.Bloom)) {
                if (rainbowMode || nova.getClientUI().getColorEnum().equals(ClientUI.ColorEnum.Rainbow)) {
                    color = ColorUtil.rainbow(5 * 100, particles.size() * 25, 1, 1, 1).getRGB();
                }
            }
        }
        public void updatePositions(float deltaTime) {
            if (mc.world == null) return;
            if (posBlock(x, y + motionY * deltaTime * 2, z)) {
                motionY = (float) (-motionY * friction);
            } else if (posBlock(x + motionX * deltaTime * 2, y, z + motionZ * deltaTime * 2)) {
                motionX = (float) (-motionX);
                motionZ = (float) (-motionZ);
            } else if (posBlock(x + motionX * deltaTime * 2, y, z)) {
                motionX = (float) (-motionX);
            } else if (posBlock(x, y, z + motionZ * deltaTime * 2)) {
                motionZ = (float) (-motionZ);
            }
            x += motionX * deltaTime * 2;
            y += motionY * deltaTime * 2;
            z += motionZ * deltaTime * 2;
        }
        public boolean posBlock(double x, double y, double z) {
            return mc.world.getBlockState(new BlockPos(x, y, z)).getMaterial() != Material.AIR;
        }
        public boolean check() {
            return checkRemove && animation.finished(Direction.FORWARDS);
        }
    }
    @java.lang.SuppressWarnings("all")
    public List<Particle> getParticles() {
        return this.particles;
    }
    @java.lang.SuppressWarnings("all")
    public float getSlowDown() {
        return this.slowDown;
    }
    @java.lang.SuppressWarnings("all")
    public float getSlowDownFromSelf() {
        return this.slowDownFromSelf;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<TickEvent> getOnTick() {
        return this.onTick;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<BossHealthRenderEvent> getOnHudPre() {
        return this.onHudPre;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<AttackEvent> getOnAttack() {
        return this.onAttack;
    }
    @java.lang.SuppressWarnings("all")
    public void setParticles(final List<Particle> particles) {
        this.particles = particles;
    }
    @java.lang.SuppressWarnings("all")
    public void setSlowDown(final float slowDown) {
        this.slowDown = slowDown;
    }
    @java.lang.SuppressWarnings("all")
    public void setSlowDownFromSelf(final float slowDownFromSelf) {
        this.slowDownFromSelf = slowDownFromSelf;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnTick(final Listener<TickEvent> onTick) {
        this.onTick = onTick;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnHudPre(final Listener<BossHealthRenderEvent> onHudPre) {
        this.onHudPre = onHudPre;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnAttack(final Listener<AttackEvent> onAttack) {
        this.onAttack = onAttack;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isFromSelf() {
        return this.fromSelf;
    }
    @java.lang.SuppressWarnings("all")
    public void setFromSelf(final boolean fromSelf) {
        this.fromSelf = fromSelf;
    }
    @java.lang.SuppressWarnings("all")
    public OtherParticlesMode getOtherParticles() {
        return this.otherParticles;
    }
    @java.lang.SuppressWarnings("all")
    public void setOtherParticles(final OtherParticlesMode otherParticles) {
        this.otherParticles = otherParticles;
    }
    @java.lang.SuppressWarnings("all")
    public RenderMode getRenderMode() {
        return this.renderMode;
    }
    @java.lang.SuppressWarnings("all")
    public void setRenderMode(final RenderMode renderMode) {
        this.renderMode = renderMode;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isRainbowMode() {
        return this.rainbowMode;
    }
    @java.lang.SuppressWarnings("all")
    public void setRainbowMode(final boolean rainbowMode) {
        this.rainbowMode = rainbowMode;
    }
}
