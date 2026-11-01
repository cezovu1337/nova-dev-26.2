package dev.fesly.client.component.draggable.impl;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.fesly.Nova;
import dev.fesly.client.component.draggable.Draggable;
import dev.fesly.client.component.draggable.DraggableManager;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.EaseBackIn;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.util.time.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.util.Collections;
import java.util.Objects;
public class TargetHud extends Draggable {
    public TargetHud() {
        super("targetHud", (int) (mw.getScaledWidth() / 2.25f), (int) (mw.getScaledHeight() / 2.25f), 100, 36);
    }
    @Override
    public boolean isVisible() {
        return false;
    }
    public static final EaseBackIn animation = new EaseBackIn(300, 1, 1.5f, Direction.BACKWARDS);
    private static final dev.fesly.impl.util.animation.compact.Animation tap = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 155);
    private static final dev.fesly.impl.util.animation.compact.Animation healthAnimation = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 85);
    private static final dev.fesly.impl.util.animation.compact.Animation BBA = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 55);
    public static boolean translated;
    private static LivingEntity targeted = null;
    public static float getScaleAnim() {
        return Math.max(animation.getOutput().floatValue() - tap.getNumberValue().floatValue(), 0);
    }
    @Override
    public void drawPre(double mouseX, double mouseY, float partialTicks) {
        boolean targetHas = Aura.target != null && Aura.target.isAlive();
        boolean chat = mc.currentScreen instanceof ChatScreen;
        animation.setDirection(chat || targetHas);
        boolean dragged = DraggableManager.dragged instanceof TargetHud;
        tap.run((dragged && !DraggableManager.draggedTimer.hasReached(150)) ? 0.04f : 0);
        if (!(mc.currentScreen instanceof ChatScreen)) {
            if (Aura.target != null && (Aura.target.isAlive())) {
                targeted = Aura.target;
            }
        } else {
            if (mc.player != null) {
                targeted = mc.player;
            }
        }
        if (!translated) {
            IAccess.addRunnableWithChecks(() -> {
                draw(new Vector4f(x, y, width, height), true, false);
            });
            IAccess.POST_RUNNABLES.add(() -> {
                draw(new Vector4f(x, y, width, height), false, false);
            });
        }
    }
    public static void draw(Vector4f coord, boolean blur, boolean bloom) {
        if (targeted == null) return;
        if (animation.finished(Direction.BACKWARDS) && animation.getDirection().backwards()) return;
        float x = translated ? 0 : coord.getX();
        float y = translated ? 0 : coord.getY();
        float width = coord.getZ();
        float height = coord.getW();
        if (blur) {
            ScaleUtil.scaleStart(x + width / 2f, y + height / 2f, getScaleAnim());
            RenderUtil.roundedRectangle(x, y, width, height, 5, Color.white);
            ScaleUtil.scaleEnd();
        } else if (bloom) {
        } else {
            float zz = MathUtil.normalize(targeted.getHealth(), 0, targeted.getMaxHealth());
            healthAnimation.run(zz);
            {
                ScaleUtil.scaleStart(x + width / 2f, y + height / 2f, getScaleAnim());
                float xFor = x + 4;
                float yFor = y + height - 10f;
                float heightFor = 7f;
                float widthFor = width - 8;
                float widthForHp = healthAnimation.getNumberValue().floatValue() * widthFor;
                Color c = ClientUtil.getThreeClientColor();
                RenderUtil.roundedRectangle(xFor, yFor, widthFor, heightFor, 1.7f, new Color(c.getRed(), c.getGreen(), c.getBlue(), 150).darker());
                Color color2 = ClientUtil.getFourClientColor() != ClientUtil.getTwoClientColor() ? ClientUtil.getOneClientColor() : c;
                ScaleUtil.scaleEnd();
                RenderUtil.applyBloom(3, 1, () -> {
                        ScaleUtil.scaleStart(x + width / 2f, y + height / 2f, getScaleAnim());
                        float bs = BBA.isFinished() ? BBA.getNumberValue().floatValue() < 6f ? 0f : 8f : BBA.getNumberValue().floatValue();
                        RenderUtil.roundedGradientRectangle(
                                xFor + bs,
                                yFor + bs,
                                widthForHp - bs * 2,
                                heightFor - bs * 2, 1.7f, c.darker(), c.darker(), color2.darker(), color2.darker());
                        ScaleUtil.scaleEnd();
                    });
                {
                    float bs = BBA.isFinished() ? BBA.getNumberValue().floatValue() < 6f ? 0f : 8f : BBA.getNumberValue().floatValue();
                    Color c1 = new Color(c.getRed(),c.getGreen(),c.getBlue(),60);
                    Color c2 = new Color(color2.getRed(),color2.getGreen(),color2.getBlue(),60);
                    ScaleUtil.scaleStart(x + width / 2f, y + height / 2f, getScaleAnim());RenderUtil.roundedGradientRectangle(xFor - 1, yFor - 1, widthForHp + 2, heightFor + 2,
                        1.7f,
                        c1
                        , c1,
                        c2, c2);
                    RenderUtil.roundedGradientRectangle(xFor, yFor, widthForHp, heightFor, 1.7f, c, c, color2, color2);
                }
                Font font = Fonts.ROBOTO_MEDIUM.get(15);
                Font font2 = Fonts.ROBOTO_MEDIUM.get(13);
                font.draw(targeted.getName().getString(), x + 25, y + 2, -1);
                Nova.getInstance().getDraggableManager().getDraggable(TargetHud.class).setWidth(
                        Math.max(font.getWidth(targeted.getName().getString()) + 30, 100)
                );
                if ((font.getWidth(targeted.getName().getUnformattedComponentText() + 30) > width)) {
                    Nova.getInstance().getDraggableManager().getDraggable(TargetHud.class).width =
                            font.getWidth(targeted.getName().getUnformattedComponentText() + 30);
                }
                Fonts.ROBOTO_MEDIUM.get(13).drawCenterXY("" + TextFormatting.BOLD + (int) (((healthAnimation.getNumberValue().floatValue() * (int) (targeted.getMaxHealth())) * 100) / targeted.getMaxHealth()) + "%",
                        xFor + widthFor / 2f - 2, yFor + heightFor - font2.getHeight() / 2f + 0.5f, -1);
                ScaleUtil.scaleEnd();
                float widthA = 20.5f;
                float heightA = 20;
                ScaleUtil.scaleStart(x + width / 2f, y + height / 2f, getScaleAnim());
                StencilUtil.initStencil();
                RenderUtil.roundedRectangle(x + 4 + BBA.getNumberValue().floatValue(),
                        y + 3 + BBA.getNumberValue().floatValue(),
                        widthA - BBA.getNumberValue().floatValue() * 2,
                        widthA - BBA.getNumberValue().floatValue() * 2,
                        widthA / 2f, c.brighter().brighter());
                StencilUtil.bindReadStencilBuffer(1);
                drawPlayerHead(x + 4, y + heightA + 3, widthA, heightA, 8, 8, 8, 8, mc.player);
                drawPlayerHead(x + 4, y + heightA + 3, widthA, heightA, 8, 8, 8, 8, targeted);
                StencilUtil.uninitStencilBuffer();
                ScaleUtil.scaleEnd();
                {
                    BBA.run(0.5f);
                    if (false)
                        RenderUtil.applyBloom(3, 1, () -> {
                            ScaleUtil.scaleStart(x + width / 2f, y + height / 2f, getScaleAnim());
                            RenderUtil.roundedRectangle(x + 4 + BBA.getNumberValue().floatValue(),
                                    y + 3 + BBA.getNumberValue().floatValue(),
                                    widthA - BBA.getNumberValue().floatValue() * 2,
                                    widthA - BBA.getNumberValue().floatValue() * 2,
                                    widthA / 2f, color2.brighter().brighter());
                            ScaleUtil.scaleEnd();
                        });
                }
                ScaleUtil.scaleStart(x + width / 2f, y + height / 2f, getScaleAnim());
                final java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
                stacks.add(targeted.getHeldItemMainhand());
                stacks.add(targeted.getHeldItemOffhand());
                targeted.getArmorInventoryList().forEach(stacks::add);
                stacks.removeIf(ItemStack::isEmpty);
                int totalSize = stacks.size() * 10;
                int iterable = 0;
                float offset = 0;
                RenderHelper.disableStandardItemLighting();
                Collections.reverse(stacks);
                for (ItemStack stack : stacks) {
                    drawItemStack(stack, x + 23 + offset, y + 10f);
                    offset += 12;
                }
                ScaleUtil.scaleEnd();
            }
        }
    }
    public static void drawItemStack(ItemStack stack, double x, double y) {
        ScaleUtil.scaleStart((float) (x + 8), (float) (y + 8), 0.7f);
        RenderUtil.roundedRectangle(x, y, 16, 16, 6, new Color(31, 31, 31, 80));
        GL11.glPushMatrix();
        GL11.glScalef(-0.01f * -200, -0.01f * -200, -0.01f * -200);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
        GlStateManager.scaled(0.5, 0.5, 0.5);
        mc.getItemRenderer().renderItemIntoGUI(stack, (int) x, (int) y);
        String count = "";
        if (stack.getCount() > 1) {
            count = stack.getCount() + "";
        }
        mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, stack, (int) x, (int) y, count);
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        ScaleUtil.scaleEnd();
    }
    public static void drawPlayerHead(double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight, LivingEntity player) {
        if (Minecraft.getInstance().getConnection() != null) {
            net.minecraft.client.multiplayer.PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(player.getUniqueID());
            if (playerInfo != null) {
                RenderUtil.drawTexture(new net.minecraft.util.ResourceLocation(playerInfo.getSkin().body().texturePath().toString()), x, y, width, height, texX, texY, texWidth, texHeight);
            }
        }
    }
    @Override
    public void draw(double mouseX, double mouseY, float partialTicks) {
    }
}
