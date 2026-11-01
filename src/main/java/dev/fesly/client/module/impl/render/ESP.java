package dev.fesly.client.module.impl.render;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.Priorities;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.other.WorldChangeEvent;
import dev.fesly.impl.event.impl.render.*;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.color.Colors;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.player.BlockUtils;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.shader.ShaderUtil;
import dev.fesly.impl.value.secondValues.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import java.awt.*;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;
import static com.mojang.blaze3d.systems.RenderSystem.glUniform1;
@ModuleInfo(name = "ESP", category = Category.RENDER, description = "Добавляет красивые эффекты на энтити!")
public class ESP extends Module {
    public static Minecraft mc = Minecraft.getInstance();
    public Map<LivingEntity, double[]> entityPositions = new HashMap<LivingEntity, double[]>();
    public Runnable PlayerHand = new Runnable() {
        @Override
        public void run() {
        }
    };
    public enum EnumentityList {
        Players, Other, Hands;
    }
    public enum EnumEspMode {
        Glow, Corner;
    }
    public enum NameTagElements {
        Potions, Armor;
    }
    public static EnumSet<EnumentityList> entityList = EnumSet.of(EnumentityList.Players, EnumentityList.Other, EnumentityList.Hands);
    public EnumEspMode espList = EnumEspMode.Corner;
    public EnumSet<NameTagElements> nameTagElements = EnumSet.of(NameTagElements.Armor, NameTagElements.Potions);
    public static boolean blurAgain;
    public boolean cutModel;
    private static float blurRadius = 7.5F;
    private static float nearMaxDistance = 10.0F;
    private static float blurExposure = 0.9F;
    public static float gradientRange = 5.5F;
    public boolean hand;
    public static boolean glow = true;
    public static boolean renderOnlyTarget = true;
    public static boolean outline = true;
    public static boolean glass = false;
    static float distortionLvl = 0.7F;
    static float distortionAmount = 0.001F;
    @Override
    public void init() {
        Map<String, Setting> settingsAb = new LinkedHashMap<>();
        addSetting("espMode", new EnumSetting<>("Esp Mode", this, this::getEspList, this::setEspList, EnumEspMode.class));
        addSetting("entityList", new MultiEnumSetting<>("Elements List", this, ESP::getEntityList, ESP::setEntityList, ESP.EnumentityList.class));
        addSetting("blurRadius", new NumberSetting("Blur Radius", this, ESP::getBlurRadius, ESP::setBlurRadius, "%.1f", 2.5F, 20.0F));
        addSetting("blurExposure", new NumberSetting("Blur Exposure", this, ESP::getBlurExposure, ESP::setBlurExposure, "%.1f", 0.5F, 1.0F));
        addSetting("glow", new BooleanSetting("Glow", this, ESP::isGlow, ESP::setGlow));
        addSetting("outline", new BooleanSetting("Outline", this, ESP::isOutline, ESP::setOutline));
    }
    public static final ShaderUtil outlineShader = new ShaderUtil("outline");
    public static final ShaderUtil glowShader = new ShaderUtil("clientbloom");
    private static final ShaderUtil glowOutlineShader = new ShaderUtil("clientoutline");
    public static Framebuffer framebuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);
    public static Framebuffer entityFramebuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);
    public Framebuffer handFramebuffer;
    public static Framebuffer outlineFrameBuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);
    public static Framebuffer glowFrameBuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);
    public static Framebuffer glassFrameBuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);
    private final List<Entity> entities = new ArrayList<>();
    public static MatrixStack ultraMatrix = new MatrixStack();
    public static MatrixStack matrixFrom(MatrixStack matrices, ActiveRenderInfo camera) {
        matrices.rotate(Vector3f.XP.rotationDegrees(camera.getPitch()));
        matrices.rotate(Vector3f.YP.rotationDegrees(camera.getYaw() + 180.0F));
        return matrices;
    }
    public void renderEntities(float ticks, MatrixStack stack, ActiveRenderInfo activeRenderInfo) {
        Minecraft mc = Minecraft.getInstance();
        if (!entities.isEmpty()) {
            for (Entity entity : entities) {
                if (entity == mc.player) continue;
                EntityRendererManager renderManager = mc.getRenderManager();
                if (renderManager == null) continue;
                Vector3d vector3d = activeRenderInfo.getProjectedView();
                double camX = vector3d.getX();
                double camY = vector3d.getY();
                double camZ = vector3d.getZ();
                float partialTicks = mc.getRenderPartialTicks();
                double d0 = MathHelper.lerp((double) partialTicks, entity.lastTickPosX, entity.getPosX());
                double d1 = MathHelper.lerp((double) partialTicks, entity.lastTickPosY, entity.getPosY());
                double d2 = MathHelper.lerp((double) partialTicks, entity.lastTickPosZ, entity.getPosZ());
                float f = MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw);
                renderManager.renderEntityStatic(entity, d0 - camX, d1 - camY, d2 - camZ, f, partialTicks, stack, mc.getRenderTypeBuffers().getBufferSource(), renderManager.getPackedLight(entity, partialTicks));
            }
        }
    }
    @EventLink
    public Listener<WorldChangeEvent> onChange = evt -> {
        framebuffer.framebufferClear();
        entityFramebuffer.framebufferClear();
    };
    @EventLink
    public Listener<WorldRenderLastEvent> onLastWorldRender = evt -> {
        if (espList.equals(EnumEspMode.Glow)) {
            if ((!entityList.contains(EnumentityList.Players) && !entityList.contains(EnumentityList.Other) || !mc.gameSettings.getPointOfView().func_243192_a())) {
                entities.clear();
                entityFramebuffer.framebufferClear();
                return;
            }
            boolean thirdPerson;
            {
                double x = (int) evt.getActiveRenderInfo().getBlockPos().getX();
                double y = evt.getActiveRenderInfo().getBlockPos().getX();
                double z = (int) evt.getActiveRenderInfo().getBlockPos().getX();
                double x2 = (int) mc.player.getPosX();
                double y2 = mc.player.getPosY();
                double z2 = (int) mc.player.getPosZ();
                thirdPerson = x != x2 || z2 != z;
            }
            if (thirdPerson) {
                collectEntities();
                entityFramebuffer = RenderUtil.createFrameBuffer(entityFramebuffer);
                entityFramebuffer.framebufferClear();
                entityFramebuffer.bindFramebuffer(true);
                MatrixStack matrixStack = new MatrixStack();
                ActiveRenderInfo activeRenderInfo = evt.getActiveRenderInfo();
                matrixStack.rotate(Vector3f.XP.rotationDegrees(activeRenderInfo.getPitch()));
                matrixStack.rotate(Vector3f.YP.rotationDegrees(activeRenderInfo.getYaw() + 180.0F));
                renderEntities(mc.getRenderPartialTicks(), matrixStack, activeRenderInfo);
                entityFramebuffer.unbindFramebuffer();
            } else {
                entityFramebuffer.framebufferClear();
            }
        }
    };
    @EventLink(Priorities.VERY_LOW)
    public Listener<WorldRenderEvent> onWorldRender = evt -> {
        MatrixStack stack = new MatrixStack();
        if (espList.equals(EnumEspMode.Glow)) {
            if (
            (!mc.gameSettings.getPointOfView().func_243192_a()) || BlockUtils.getBlock(Objects.requireNonNull(mc.player).getPosition().add(0, 1.99, 0)) == Blocks.LAVA) {
                framebuffer.framebufferClear();
                return;
            }
            stack.push();
            framebuffer = RenderUtil.createFrameBuffer(framebuffer);
            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(true);
            mc.gameRenderer.renderHand(matrixFrom(stack, evt.getActiveRenderInfo()), evt.getActiveRenderInfo(), evt.getPartialTicks(), true, true, false);
            framebuffer.unbindFramebuffer();
            stack.pop();
        }
    };
    private Vector3d prevPlayerPos = null;
    @EventLink(Priorities.VERY_LOW)
    public Listener<DrawHudPreEvent> render = evt -> {
        if (espList.equals(EnumEspMode.Glow)) {
            if (!entities.isEmpty()) {
                if (entityList.contains(EnumentityList.Hands)) render2D(framebuffer, blurRadius, blurExposure, blurExposure, outline, glass, false);
                if (entityList.contains(EnumentityList.Players) || entityList.contains(EnumentityList.Other)) render2D(entityFramebuffer, blurRadius, blurExposure, blurExposure, outline, glass, false);
            } else {
                entityFramebuffer.framebufferClear();
            }
        } else if (mc.world != null) {
            for (Entity player : mc.world.getAllEntities()) {
                if (player == mc.player) continue;
                if (!entityList.contains(EnumentityList.Players) && player instanceof Player) continue;
                if (!entityList.contains(EnumentityList.Other) && !(player instanceof Player)) continue;
                float partialTicks = mc.getRenderPartialTicks();
                double d0 = MathHelper.lerp((double) partialTicks, player.lastTickPosX, player.getPosX());
                double d1 = MathHelper.lerp((double) partialTicks, player.lastTickPosY, player.getPosY());
                double d2 = MathHelper.lerp((double) partialTicks, player.lastTickPosZ, player.getPosZ());
                AxisAlignedBB axisAlignedBB = player.getBoundingBox();
                double expandAmount = 0.2;
                AxisAlignedBB expandedBoundingBox = axisAlignedBB.grow(expandAmount, expandAmount, expandAmount);
                List<Vector3d> vectors = Arrays.asList(new Vector3d(expandedBoundingBox.minX, expandedBoundingBox.minY, expandedBoundingBox.minZ), new Vector3d(expandedBoundingBox.minX, expandedBoundingBox.maxY, expandedBoundingBox.minZ), new Vector3d(expandedBoundingBox.maxX, expandedBoundingBox.minY, expandedBoundingBox.minZ), new Vector3d(expandedBoundingBox.maxX, expandedBoundingBox.maxY, expandedBoundingBox.minZ), new Vector3d(expandedBoundingBox.minX, expandedBoundingBox.minY, expandedBoundingBox.maxZ), new Vector3d(expandedBoundingBox.minX, expandedBoundingBox.maxY, expandedBoundingBox.maxZ), new Vector3d(expandedBoundingBox.maxX, expandedBoundingBox.minY, expandedBoundingBox.maxZ), new Vector3d(expandedBoundingBox.maxX, expandedBoundingBox.maxY, expandedBoundingBox.maxZ));
                vectors.sort(Comparator.comparingDouble(v -> mc.player.getDistance((Vector3d) v)).thenComparingDouble(v -> mc.player.getDistance((Vector3d) v)));
                Vector3d vector1 = vectors.get(0);
                Vector3d vector2 = vectors.get(1);
                Vector3d vector3 = vectors.get(2);
                Vector3d vector4 = vectors.get(3);
                double[] vec1 = RenderUtil.worldToScreen(vector1.x, vector1.y, vector1.z);
                double[] vec2 = RenderUtil.worldToScreen(vector2.x, vector2.y, vector2.z);
                double[] vec3 = RenderUtil.worldToScreen(vector3.x, vector3.y, vector3.z);
                double[] vec4 = RenderUtil.worldToScreen(vector4.x, vector4.y, vector4.z);
                float b = 0;
                if (player instanceof ItemEntity) {
                    b = MathHelper.sin(((float) ((ItemEntity) player).getAge() + partialTicks) / 10.0F + ((ItemEntity) player).hoverStart) * 0.1F + 0.1F + 0.2F;
                }
                double[] vecY = RenderUtil.worldToScreen(vector2.x, axisAlignedBB.maxY + (b), vector2.z);
                double[] vecH = RenderUtil.worldToScreen(vector2.x, axisAlignedBB.minY + (b), vector2.z);
                if (vec1 == null || vec2 == null || vec3 == null || vec4 == null || vecY == null || vecH == null) continue;
                double x = Math.min(Math.min(vec1[0], vec2[0]), Math.min(vec3[0], vec4[0]));
                double y = vecY[1] - ((mc.player.getPosY() > player.getPosY()) ? ((1.0F - (mc.player.getDistance(player) / 35)) * 20) : 0);
                double width = Math.max(Math.max(vec1[0], vec2[0]), Math.max(vec3[0], vec4[0])) - x;
                double height = vecH[1] - y;
                if (player.anim2dPosX == -1) {
                    player.anim2dPosX = (x);
                    player.anim2dPosY = (y);
                    player.anim2dPosW = (width);
                    player.anim2dPosH = (height);
                }
                player.anim2dPosX = MathHelper.lerp(partialTicks, player.anim2dPosX, x);
                player.anim2dPosY = MathHelper.lerp(partialTicks, player.anim2dPosY, y);
                player.anim2dPosW = MathHelper.lerp(partialTicks, player.anim2dPosW, width);
                player.anim2dPosH = MathHelper.lerp(partialTicks, player.anim2dPosH, height);
                x = player.anim2dPosX;
                y = player.anim2dPosY;
                width = player.anim2dPosW;
                height = player.anim2dPosH;
                {
                    float w = (float) width;
                    float h = (float) height;
                    boolean wEdit = w < 0;
                    boolean hEdit = h < 0;
                    w = w < 0 ? -w : w;
                    h = h < 0 ? -h : h;
                    float playerWidth = (float) (player.getWidth() * ((mw.getWidth() / mw.getScaleFactor()) / 4)) * (1.0F - mc.player.getDistance(player) / 102);
                    playerWidth = playerWidth > 120 ? 120 : playerWidth;
                    float finalW = w;
                    float finalH = h;
                    float xBB = (float) x;
                    float yBB = (float) y;
                    if (finalW > 1.0F && finalH > 1.0F && finalW < mw.getScaledWidth() * 2 && finalH < mw.getScaledHeight() * 2) {
                        if (glow) {
                            IAccess.BLOOM_RUNNABLES.add(() -> {
                                RenderUtil.roundedGradientRectangle((float) xBB, (float) yBB, finalW, finalH, Math.hypot(finalW, finalH) / 16, ColorUtil.interpolateTwoColors(15, 15, ClientUtil.getOneClientColor(), ClientUtil.getTwoClientColor()), ColorUtil.interpolateTwoColors(15, 15, ClientUtil.getTwoClientColor(), ClientUtil.getThreeClientColor()), ColorUtil.interpolateTwoColors(15, 15, ClientUtil.getThreeClientColor(), ClientUtil.getFourClientColor()), ColorUtil.interpolateTwoColors(15, 15, ClientUtil.getFourClientColor(), ClientUtil.getOneClientColor()));
                            });
                        }
                        if (outline) {
                            RenderUtil.roundedOutlineGradientRectangle((float) xBB, (float) yBB, finalW, finalH, Math.hypot(finalW, finalH) / 16, 1.0F, ColorUtil.interpolateTwoColors(15, 15, ClientUtil.getOneClientColor(), ClientUtil.getTwoClientColor()), ColorUtil.interpolateTwoColors(15, 15, ClientUtil.getTwoClientColor(), ClientUtil.getThreeClientColor()), ColorUtil.interpolateTwoColors(15, 15, ClientUtil.getThreeClientColor(), ClientUtil.getFourClientColor()), ColorUtil.interpolateTwoColors(15, 15, ClientUtil.getFourClientColor(), ClientUtil.getOneClientColor()));
                        }
                    }
                }
            }
        }
    };
    public static void render2D(Framebuffer framebuffer, float blurRadius, float blurExposure, boolean outline, boolean glass, boolean full) {
        render2D(framebuffer, blurRadius, blurExposure + ClientUI.getShadowCompress() / 4, blurExposure / 3.3F, outline, glass, full);
    }
    public static void render2D(Framebuffer framebuffer, float blurRadius, float blurExposure, float outlineExposure, boolean outline, boolean glass, boolean full) {
        render2DForHud(framebuffer, blurRadius, blurExposure, outlineExposure, outline && ESP.outline, ESP.glow, glass, full);
    }
    public static void render2DForHud(Framebuffer framebuffer, float blurRadius, float blurExposure, float outlineExposure, boolean doOutline, boolean doGlow, boolean glass, boolean full) {
        if (framebuffer != null) {
            outlineFrameBuffer = RenderUtil.createFrameBuffer(outlineFrameBuffer);
            outlineFrameBuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
            glowFrameBuffer = RenderUtil.createFrameBuffer(glowFrameBuffer);
            glowFrameBuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
            glassFrameBuffer = RenderUtil.createFrameBuffer(glassFrameBuffer);
            glassFrameBuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
            if (doGlow && blurRadius > 0.001F && blurExposure > 0.001F) {
                float rng = blurRadius;
                float exposure = blurExposure;
                RenderSystem.enableAlphaTest();
                RenderSystem.alphaFunc(516, 0.0F);
                RenderSystem.enableBlend();
                int dir1 = 0;
                float dir2 = ClientUI.getShadowCompress();
                RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                outlineFrameBuffer.framebufferClear();
                outlineFrameBuffer.bindFramebuffer(true);
                outlineShader.init();
                setupOutlineUniforms(dir1, dir2, rng);
                RenderUtil.bindTexture(framebuffer.framebufferTexture);
                ShaderUtil.drawScaledQuads();
                outlineShader.init();
                setupOutlineUniforms(dir2, dir1, rng);
                RenderUtil.bindTexture(framebuffer.framebufferTexture);
                ShaderUtil.drawScaledQuads();
                outlineShader.unload();
                outlineFrameBuffer.unbindFramebuffer();
                RenderSystem.color4f(1, 1, 1, 1);
                glowFrameBuffer.framebufferClear();
                glowFrameBuffer.bindFramebuffer(true);
                glowShader.init();
                setupGlowUniforms(dir1, dir2, rng, exposure, true, false);
                RenderUtil.bindTexture(full ? framebuffer.framebufferTexture : outlineFrameBuffer.framebufferTexture);
                ShaderUtil.drawScaledQuads();
                glowShader.unload();
                glowFrameBuffer.unbindFramebuffer();
                mc.getFramebuffer().bindFramebuffer(true);
                glowShader.init();
                setupGlowUniforms(dir2, dir1, rng, exposure, true, false);
                GL13.glActiveTexture(GL13.GL_TEXTURE16);
                RenderUtil.bindTexture(framebuffer.framebufferTexture);
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                RenderUtil.bindTexture(glowFrameBuffer.framebufferTexture);
                ShaderUtil.drawScaledQuads();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                glowShader.unload();
                GL13.glActiveTexture(GL13.GL_TEXTURE16);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
            }
            if (doOutline && outlineExposure > 0.001F) {
                float range = 0.55F;
                float exposure = outlineExposure;
                RenderSystem.enableAlphaTest();
                RenderSystem.alphaFunc(516, 0.0F);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                outlineFrameBuffer.framebufferClear();
                outlineFrameBuffer.bindFramebuffer(true);
                outlineShader.init();
                setupOutlineUniforms(0, 1, 1.8F);
                RenderSystem.bindTexture(framebuffer.framebufferTexture);
                ShaderUtil.drawScaledQuads();
                outlineShader.init();
                setupOutlineUniforms(1, 0, 1.8F);
                RenderSystem.bindTexture(framebuffer.framebufferTexture);
                ShaderUtil.drawScaledQuads();
                outlineShader.unload();
                outlineFrameBuffer.unbindFramebuffer();
                RenderSystem.color4f(1, 1, 1, 1);
                glowFrameBuffer.framebufferClear();
                glowFrameBuffer.bindFramebuffer(true);
                glowOutlineShader.init();
                setupGlowOutlineUniforms(0, 1, range, exposure, true, false);
                RenderSystem.bindTexture(outlineFrameBuffer.framebufferTexture);
                ShaderUtil.drawScaledQuads();
                glowOutlineShader.unload();
                glowFrameBuffer.unbindFramebuffer();
                mc.getFramebuffer().bindFramebuffer(true);
                glowOutlineShader.init();
                setupGlowOutlineUniforms(1, 0, range, exposure, true, false);
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                RenderUtil.bindTexture(glowFrameBuffer.framebufferTexture);
                ShaderUtil.drawScaledQuads();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                glowOutlineShader.unload();
            }
        }
    }
    public static final long detime = System.currentTimeMillis();
    private static final ClientUI ClientUI = Nova.getInstance().getClientUI();
    public static void setupGlowUniforms(float dir1, float dir2, float range, float exposure, boolean colorful, boolean customColor) {
        customColor = true;
        Color color = Colors.WHITE.getColor();
        glowShader.setUniformf("noColorfulColor", 0.12F, 0.12F, 0.12F);
        glowShader.setUniformi("colorful", nova.getClientUI().isColorful() && nova.getClientUI().isColorfulGlow() ? 1 : 0);
        glowShader.setUniformi("customColor", customColor ? 1 : 0);
        glowShader.setUniformi("u_other_sampler", 16);
        glowShader.setUniformi("u_diffuse_sampler", 0);
        glowShader.setUniformf("u_radius", range);
        glowShader.setUniformf("u_texel_size", 1.0F / mw.getFramebufferWidth(), 1.0F / mw.getFramebufferHeight());
        glowShader.setUniformf("resa", (float) (200 * mw.getScaleFactor()), (float) ((mw.getHeight() - (120 * mw.getScaleFactor())) - (200 * mw.getScaleFactor())));
        glowShader.setUniformf("u_direction", dir2, dir1);
        glowShader.setUniformf("color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);
        Color color1 = ClientUtil.getOneClientColor().brighter();
        Color color2 = ClientUtil.getTwoClientColor().brighter();
        Color color3 = ClientUtil.getThreeClientColor().brighter();
        Color color4 = ClientUtil.getFourClientColor().brighter();
        if (!ClientUI.isColorful()) {
            color1 = new Color(0);
            color2 = new Color(1842204);
        }
        if (!colorful) {
            color1 = color1.brighter().brighter().brighter().brighter();
            color2 = color2.brighter().brighter().brighter().brighter();
        }
        float r = color1.getRed() / 255.0F;
        float g = color1.getGreen() / 255.0F;
        float b = color1.getBlue() / 255.0F;
        float rr = color2.getRed() / 255.0F;
        float gg = color2.getGreen() / 255.0F;
        float bb = color2.getBlue() / 255.0F;
        float rrr = color3.getRed() / 255.0F;
        float ggg = color3.getGreen() / 255.0F;
        float bbb = color3.getBlue() / 255.0F;
        float rrrr = color4.getRed() / 255.0F;
        float gggg = color4.getGreen() / 255.0F;
        float bbbb = color4.getBlue() / 255.0F;
        glowShader.setUniformf("color1", r, g, b);
        glowShader.setUniformf("color2", rr, gg, bb);
        glowShader.setUniformf("color3", rrr, ggg, bbb);
        glowShader.setUniformf("color4", rrrr, gggg, bbbb);
        glowShader.setUniformf("exposure", exposure);
        glowShader.setUniformi("avoidTexture", 1);
        glowShader.setUniformf("time", (System.currentTimeMillis() - detime) / 1000.0F);
        glowShader.setUniformf("colorSpeed", ClientUI.getColorSpeed());
        glowShader.setUniformi("blurAgain", blurAgain ? 1 : 0);
        glowShader.setUniformi("rainbow", 0);
        glowShader.setUniformf("blurAgainRadius", blurRadius);
        glowShader.setUniformf("gradientRange", ClientUI.getGradientRange());
        final FloatBuffer buffer = BufferUtils.createFloatBuffer(128);
        int maxVals = 128;
        int count = 0;
        if (range > 1) {
            for (int i = 1; i <= range && count < maxVals; i++) {
                buffer.put(MathUtil.calculateGaussianValue(i, range / 2));
                count++;
            }
        } else {
            for (float i = 0.01F; i <= range && count < maxVals; i += 0.01F) {
                buffer.put(MathUtil.calculateGaussianValue(i, range / 2));
                count++;
            }
        }
        if (blurAgain) {
            for (float i = 0.1F; i <= range && count < maxVals; i += 0.1F) {
                buffer.put(MathUtil.calculateGaussianValue(i, range / 2));
                count++;
            }
        }
        while (count < maxVals) {
            buffer.put(0.0F);
            count++;
        }
        buffer.rewind();
        int uKernelGlow = glowShader.getUniform("u_kernel");
        if (uKernelGlow > -1) {
            glUniform1(uKernelGlow, buffer);
        }
        glowShader.setUniformf("force", 1.0F);
    }
    public static void setupGlowOutlineUniforms(float dir1, float dir2, float range, float exposure, boolean colorful, boolean customColor) {
        customColor = true;
        glowOutlineShader.setUniformf("noColorfulColor", 0.12F, 0.12F, 0.12F);
        glowOutlineShader.setUniformi("colorful", nova.getClientUI().isColorful() && nova.getClientUI().isColorfulGlow() ? 1 : 0);
        Color color = Colors.WHITE.getColor();
        glowOutlineShader.setUniformi("u_texture", 16);
        glowOutlineShader.setUniformf("u_radius", range);
        glowOutlineShader.setUniformf("u_texel_size", 1.0F / mw.getFramebufferWidth(), 1.0F / mw.getFramebufferHeight());
        glowOutlineShader.setUniformf("resa", (float) (200 * mw.getScaleFactor()), (float) ((mw.getHeight() - (120 * mw.getScaleFactor())) - (200 * mw.getScaleFactor())));
        glowOutlineShader.setUniformf("u_direction", dir2, dir1);
        glowOutlineShader.setUniformf("color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);
        Color color1 = ClientUtil.getOneClientColor();
        Color color2 = ClientUtil.getTwoClientColor();
        Color color3 = ClientUtil.getThreeClientColor();
        Color color4 = ClientUtil.getFourClientColor();
        if (!ClientUI.isColorful()) {
            color1 = new Color(0);
            color2 = new Color(1842204);
        }
        if (!colorful) {
            color1 = color1.brighter().brighter().brighter().brighter();
            color2 = color2.brighter().brighter().brighter().brighter();
        }
        float r = color1.getRed() / 255.0F;
        float g = color1.getGreen() / 255.0F;
        float b = color1.getBlue() / 255.0F;
        float rr = color2.getRed() / 255.0F;
        float gg = color2.getGreen() / 255.0F;
        float bb = color2.getBlue() / 255.0F;
        float rrr = color3.getRed() / 255.0F;
        float ggg = color3.getGreen() / 255.0F;
        float bbb = color3.getBlue() / 255.0F;
        float rrrr = color4.getRed() / 255.0F;
        float gggg = color4.getGreen() / 255.0F;
        float bbbb = color4.getBlue() / 255.0F;
        glowOutlineShader.setUniformf("color1", r, g, b);
        glowOutlineShader.setUniformf("color2", rr, gg, bb);
        glowOutlineShader.setUniformf("color3", rrr, ggg, bbb);
        glowOutlineShader.setUniformf("color4", rrrr, gggg, bbbb);
        glowOutlineShader.setUniformf("exposure", exposure);
        glowOutlineShader.setUniformi("avoidTexture", 1);
        glowOutlineShader.setUniformf("time", (System.currentTimeMillis() - detime) / 1000.0F);
        glowOutlineShader.setUniformf("colorSpeed", ClientUI.getColorSpeed());
        glowOutlineShader.setUniformi("blurAgain", 0);
        glowOutlineShader.setUniformi("rainbow", 0);
        glowOutlineShader.setUniformf("blurAgainRadius", range);
        glowOutlineShader.setUniformf("gradientRange", ClientUI.getGradientRange());
        final FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
        for (int i = 1; i <= range; i++) {
            buffer.put(MathUtil.calculateGaussianValue(i, range / 2));
        }
        buffer.rewind();
        int uKernelOutline = glowOutlineShader.getUniform("u_kernel");
        if (uKernelOutline > -1) {
            glUniform1(uKernelOutline, buffer);
        }
        glowOutlineShader.setUniformf("force", 1.0F);
    }
    public static void setupOutlineUniforms(float dir1, float dir2, float range) {
        float rng = blurRadius;
        Color color = Colors.WHITE.getColor();
        outlineShader.setUniformi("u_texture", 0);
        outlineShader.setUniformf("u_radius", range);
        outlineShader.setUniformf("u_texel_size", 1.0F / mw.getFramebufferWidth(), 1.0F / mw.getFramebufferHeight());
        outlineShader.setUniformf("direction", dir1, dir2);
        outlineShader.setUniformf("color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);
    }
    public void collectEntities() {
        entities.clear();
        assert mc.world != null;
        for (Entity entity : mc.world.getAllEntities()) {
            if (!isValid(entity)) continue;
            entities.add(entity);
        }
    }
    public void render2DEntities(float ticks, MatrixStack stack) {
        if (!entities.isEmpty()) {
            for (Entity entity : entities) {
                if (!(entity instanceof LivingEntity)) continue;
                EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
                double[] vec = RenderUtil.worldToScreen(entity.getPosX(), entity.getPosY(), entity.getPosZ());
                if (vec == null) continue;
                float entityWidth = entity.getWidth();
                float entityHeight = entity.getHeight();
                float scale = 50.0F;
                stack.push();
                stack.translate(vec[0] - entityWidth * scale / 2, vec[1] - entityHeight * scale / 2, 0);
                stack.scale(scale, scale, scale);
                stack.pop();
            }
        }
    }
    public static boolean isValid(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (entity == null) {
            return false;
        }
        if (!entity.isAlive()) {
            return false;
        }
        if (entity instanceof Mob && getEntityList().contains(EnumentityList.Other)) return true;
        if (entity instanceof Animal && getEntityList().contains(EnumentityList.Other)) {
            return true;
        }
        if (entity instanceof Player && getEntityList().contains(EnumentityList.Players)) {
            return true;
        }
        if (entity instanceof ItemEntity) return false;
        if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand) {
            return false;
        }
        if (entity instanceof Animal && !getEntityList().contains(EnumentityList.Other)) {
            return false;
        }
        if (entity instanceof net.minecraft.world.entity.decoration.ItemFrame) {
            return false;
        }
        if (entity instanceof Arrow || entity instanceof SpectralArrow) {
            return false;
        }
        if (entity instanceof Minecart) {
            return false;
        }
        if (entity instanceof net.minecraft.world.entity.vehicle.boat.Boat) {
            return false;
        }
        if (entity instanceof DragonFireball) {
            return false;
        }
        if (entity instanceof net.minecraft.world.entity.ExperienceOrb) {
            return false;
        }
        if (entity instanceof MinecartChest) {
            return false;
        }
        if (entity instanceof MinecartTNT) {
            return false;
        }
        if (entity instanceof net.minecraft.world.entity.item.PrimedTnt) {
            return false;
        }
        if (entity instanceof net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle) {
            return false;
        }
        if (entity instanceof LightningBolt) {
            return false;
        }
        if (entity instanceof AbstractThrownPotion) {
            return false;
        }
        if (!(!(entity instanceof Entity) || entity instanceof Animal || entity instanceof LivingEntity || entity instanceof ItemEntity)) {
            return false;
        }
        if ((entity instanceof Mob || entity instanceof Slime || entity instanceof EnderDragon || entity instanceof AbstractGolem) && !getEntityList().contains(EnumentityList.Other)) {
            return false;
        }
        return entity == mc.player;
    }
    @java.lang.SuppressWarnings("all")
    public Map<LivingEntity, double[]> getEntityPositions() {
        return this.entityPositions;
    }
    @java.lang.SuppressWarnings("all")
    public Runnable getPlayerHand() {
        return this.PlayerHand;
    }
    @java.lang.SuppressWarnings("all")
    public EnumEspMode getEspList() {
        return this.espList;
    }
    @java.lang.SuppressWarnings("all")
    public EnumSet<NameTagElements> getNameTagElements() {
        return this.nameTagElements;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isCutModel() {
        return this.cutModel;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isHand() {
        return this.hand;
    }
    @java.lang.SuppressWarnings("all")
    public Framebuffer getHandFramebuffer() {
        return this.handFramebuffer;
    }
    @java.lang.SuppressWarnings("all")
    public List<Entity> getEntities() {
        return this.entities;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<WorldChangeEvent> getOnChange() {
        return this.onChange;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<WorldRenderLastEvent> getOnLastWorldRender() {
        return this.onLastWorldRender;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<WorldRenderEvent> getOnWorldRender() {
        return this.onWorldRender;
    }
    @java.lang.SuppressWarnings("all")
    public Vector3d getPrevPlayerPos() {
        return this.prevPlayerPos;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<DrawHudPreEvent> getRender() {
        return this.render;
    }
    @java.lang.SuppressWarnings("all")
    public void setEntityPositions(final Map<LivingEntity, double[]> entityPositions) {
        this.entityPositions = entityPositions;
    }
    @java.lang.SuppressWarnings("all")
    public void setPlayerHand(final Runnable PlayerHand) {
        this.PlayerHand = PlayerHand;
    }
    @java.lang.SuppressWarnings("all")
    public void setEspList(final EnumEspMode espList) {
        this.espList = espList;
    }
    @java.lang.SuppressWarnings("all")
    public void setNameTagElements(final EnumSet<NameTagElements> nameTagElements) {
        this.nameTagElements = nameTagElements;
    }
    @java.lang.SuppressWarnings("all")
    public void setCutModel(final boolean cutModel) {
        this.cutModel = cutModel;
    }
    @java.lang.SuppressWarnings("all")
    public void setHand(final boolean hand) {
        this.hand = hand;
    }
    @java.lang.SuppressWarnings("all")
    public void setHandFramebuffer(final Framebuffer handFramebuffer) {
        this.handFramebuffer = handFramebuffer;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnChange(final Listener<WorldChangeEvent> onChange) {
        this.onChange = onChange;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnLastWorldRender(final Listener<WorldRenderLastEvent> onLastWorldRender) {
        this.onLastWorldRender = onLastWorldRender;
    }
    @java.lang.SuppressWarnings("all")
    public void setOnWorldRender(final Listener<WorldRenderEvent> onWorldRender) {
        this.onWorldRender = onWorldRender;
    }
    @java.lang.SuppressWarnings("all")
    public void setPrevPlayerPos(final Vector3d prevPlayerPos) {
        this.prevPlayerPos = prevPlayerPos;
    }
    @java.lang.SuppressWarnings("all")
    public void setRender(final Listener<DrawHudPreEvent> render) {
        this.render = render;
    }
    @java.lang.SuppressWarnings("all")
    public static EnumSet<EnumentityList> getEntityList() {
        return ESP.entityList;
    }
    @java.lang.SuppressWarnings("all")
    public static void setEntityList(final EnumSet<EnumentityList> entityList) {
        ESP.entityList = entityList;
    }
    @java.lang.SuppressWarnings("all")
    public static float getBlurRadius() {
        return ESP.blurRadius;
    }
    @java.lang.SuppressWarnings("all")
    public static void setBlurRadius(final float blurRadius) {
        ESP.blurRadius = blurRadius;
    }
    @java.lang.SuppressWarnings("all")
    public static float getNearMaxDistance() {
        return ESP.nearMaxDistance;
    }
    @java.lang.SuppressWarnings("all")
    public static void setNearMaxDistance(final float nearMaxDistance) {
        ESP.nearMaxDistance = nearMaxDistance;
    }
    @java.lang.SuppressWarnings("all")
    public static float getBlurExposure() {
        return ESP.blurExposure;
    }
    @java.lang.SuppressWarnings("all")
    public static void setBlurExposure(final float blurExposure) {
        ESP.blurExposure = blurExposure;
    }
    @java.lang.SuppressWarnings("all")
    public static float getGradientRange() {
        return ESP.gradientRange;
    }
    @java.lang.SuppressWarnings("all")
    public static void setGradientRange(final float gradientRange) {
        ESP.gradientRange = gradientRange;
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isGlow() {
        return ESP.glow;
    }
    @java.lang.SuppressWarnings("all")
    public static void setGlow(final boolean glow) {
        ESP.glow = glow;
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isRenderOnlyTarget() {
        return ESP.renderOnlyTarget;
    }
    @java.lang.SuppressWarnings("all")
    public static void setRenderOnlyTarget(final boolean renderOnlyTarget) {
        ESP.renderOnlyTarget = renderOnlyTarget;
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isOutline() {
        return ESP.outline;
    }
    @java.lang.SuppressWarnings("all")
    public static void setOutline(final boolean outline) {
        ESP.outline = outline;
    }
    @java.lang.SuppressWarnings("all")
    public static boolean isGlass() {
        return ESP.glass;
    }
    @java.lang.SuppressWarnings("all")
    public static void setGlass(final boolean glass) {
        ESP.glass = glass;
    }
    @java.lang.SuppressWarnings("all")
    public static float getDistortionLvl() {
        return ESP.distortionLvl;
    }
    @java.lang.SuppressWarnings("all")
    public static void setDistortionLvl(final float distortionLvl) {
        ESP.distortionLvl = distortionLvl;
    }
    @java.lang.SuppressWarnings("all")
    public static float getDistortionAmount() {
        return ESP.distortionAmount;
    }
    @java.lang.SuppressWarnings("all")
    public static void setDistortionAmount(final float distortionAmount) {
        ESP.distortionAmount = distortionAmount;
    }
}
