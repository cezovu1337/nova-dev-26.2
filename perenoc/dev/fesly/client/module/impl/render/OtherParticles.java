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
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(
        name = "Other Particles",
        description = "При дамаге создает вокруг энтити \n" +
                " красивые партиклы",
        category = Category.RENDER)
@Getter
@Setter
public class OtherParticles extends Module {

    public List<Particle> particles = new ArrayList<>();

    @Getter
    @Setter
    public boolean fromSelf = true;

    @Getter
    @Setter
    public OtherParticlesMode otherParticles = OtherParticlesMode.Lightning;

    @Getter
    @Setter
    public RenderMode renderMode = RenderMode.Bloom;

    public float slowDown = 20f;
    public float slowDownFromSelf = 20f;



    public enum OtherParticlesMode {
        Lightning,
        Youtube,
        HeartOutline,
        Heart,
        Star
    }

    public enum RenderMode {
        Client,
        Bloom
    }

    @Getter
    @Setter
    private boolean rainbowMode = false;

    @Override
    public void init() {
        super.init();
        addSetting("otherParticles", new EnumSetting<>("Spawn Particles", this, this::getOtherParticles, this::setOtherParticles, OtherParticlesMode.class));
        addSetting("renderMode", new EnumSetting<>("Render Mode", this, this::getRenderMode, this::setRenderMode, RenderMode.class));

        addSetting("slowdown",new NumberSetting("SlowDown",this,this::getSlowDown,this::setSlowDown,"%.2f",5,30));
        addSetting("slowdownSelf",new NumberSetting("SlowDownSelf",this,this::isFromSelf,this::getSlowDownFromSelf,this::setSlowDownFromSelf,"%.2f",5,30));


        addSetting("rainbowMode",
                new BooleanSetting("Rainbow Mode",this,() -> this.renderMode.equals(RenderMode.Bloom), this::isRainbowMode,this::setRainbowMode)
                        .setDescription("Партиклы в цветах радуги!"));

        addSetting("spawnFromSelf", new BooleanSetting("From Self",this, this::isFromSelf,this::setFromSelf));
    }

    @EventLink
    public Listener<TickEvent> onTick = event -> {
        particles.removeIf(particle -> {
            if (mc.player.getDistanceSq(particle.x,particle.y,particle.z) > 5550) return  true;
            if (particles.size() > 100 && particles.indexOf(particle) < 10) return true;
            if (particle.check()) return true;
            assert mc.player != null;
            return mc.player.getDistanceSq(new Vector3d(particle.x, particle.y, particle.z)) > 30;
        });
    };
    @EventLink(Priorities.VERY_LOW)
    public Listener<BossHealthRenderEvent> onHudPre = evt -> {
        if (renderMode.equals(RenderMode.Client))
            IAccess.addRunnableWithChecks(render(true));
        else {
            render(true).run();
            IAccess.BLOOM_RUNNABLES.add(render(true));
        }
        var player = mc.player;
        if (fromSelf)
            if (player.getPosX() != player.lastTickPosX || player.getPosZ() != player.lastTickPosZ || player.getPosY() != player.lastTickPosY ) {
                if (Math.random() < 0.2) {
                    particles.add(new Particle(false,
                            getRandomNumber(player.getPosX(), (float) 0.3f),
                            getRandomY(player.getPosY() + 0.4f, player.getHeight() - 0.8f),
                            getRandomNumber(player.getPosZ(), (float) 0.3f),otherParticles
                            )
                    );
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
                    particles.add(new Particle(true,
                            (float) getRandomNumber(player.getPosX(), 0.5f),
                            (float) MathUtil.getRandom(player.getPosY(), player.getPosY() + player.getHeight()),
                            getRandomNumber(player.getPosZ(), 0.5f),otherParticles
                    ));
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

                float val = 0.9f;
                float vv = Math.max(val, 0.5f);
                float size = lightning ? 8 : 8;
                particle.x2D = (float) vec[0];
                particle.y2D = (float) vec[1];

                GL11.glPushMatrix();

                GL11.glTranslatef((float) vec[0], (float) vec[1], 0.0F);

                GL11.glScaled(1, 1, 1);

                GL11.glTranslatef((float) (-vec[0]) - (size / 2), (float) (-vec[1]), 0.0F);

                if (particle.checkRemove) {
                    //GL11.glRotatef((float) Math.sin(particle.motionX) * 360,0,0,0);
                    GLUtil.startRotate((float) vec[0] + size / 2, (float) (vec[1] - 2),(float) Math.sin(particle.motionX) * 360);
                }

                ScaleUtil.scaleNonMatrix((float) vec[0] + size / 2, (float) (vec[1] - 2), vv);
                if (blur) {
                    String png = "";
                    if (particle.mode.equals(OtherParticlesMode.Star)) {
                        png = "icons/visuals/star.png";
                    }else if (particle.mode.equals(OtherParticlesMode.Lightning)) {
                        png = "icons/visuals/lightningW.png";
                    }else if (particle.mode.equals(OtherParticlesMode.Heart)) {
                        png = "icons/visuals/heart.png";
                    }else if (particle.mode.equals(OtherParticlesMode.HeartOutline)) {
                        png = "icons/visuals/heartoutline2.png";
                    }else if (particle.mode.equals(OtherParticlesMode.Youtube)) {
                        png = "icons/visuals/youtube.png";
                    }


                    RenderUtil.drawImage(new ResourceLocation("nova", png),
                            (float) (vec[0] - 1), (float) vec[1], size, size, particle.color);
                    //RenderUtil.roundedRectangle(vec[0] - 1, vec[1], size,size,size / 2f,new Color(particle.color));
                }
                ScaleUtil.scaleNonMatrix((float) vec[0] + size / 2, (float) (vec[1] - 2), 1f / (vv));

                if (particle.checkRemove) {
                    //GL11.glRotatef((float) Math.sin(particle.motionX) * 360,0,0,0);
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
        public Animation animation = new DecelerateAnimation(50000, 1f, Direction.FORWARDS);

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

        public Particle(boolean checkRemove,float x, float y, float z,OtherParticlesMode mode) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.checkRemove = checkRemove;
            this.mode = mode;
            slowdown = checkRemove ? (slowDown * 10) : (slowDownFromSelf * 100);
            gravity = 1;
            friction = 0.15f;
            motionX = (float) ((float) (MathUtil.getRandom(-0.848f, 0.848f) / slowdown));
            motionY = (float) ((float) (MathUtil.getRandom(-0.848f, 0.848f) / slowdown));
            motionZ = (float) ((float) (MathUtil.getRandom(-0.848f, 0.848f) / slowdown));
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
                // частица достигла поверхности блока, замедление скорости
                motionY = (float) (-motionY * friction);
            }else if (posBlock(x + motionX * deltaTime * 2,y,z + motionZ * deltaTime * 2)) {
                motionX = (float) (-motionX);
                motionZ = (float) (-motionZ);
            }else if (posBlock(x + motionX * deltaTime * 2,y,z)) {
                motionX = (float) (-motionX);
            }else if (posBlock(x,y,z + motionZ * deltaTime * 2)) {
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
}
