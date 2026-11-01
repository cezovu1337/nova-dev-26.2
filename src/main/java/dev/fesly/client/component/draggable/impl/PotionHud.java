package dev.fesly.client.component.draggable.impl;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.Nova;
import dev.fesly.client.component.draggable.Draggable;
import dev.fesly.client.component.draggable.DraggableManager;
import dev.fesly.client.module.api.Module;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.DecelerateAnimation;
import dev.fesly.impl.util.animation.extended.impl.EaseBackIn;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.render.StencilUtil;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
public class PotionHud extends Draggable {
    public PotionHud() {
        super("potionHud", 150, 40, 100, 30);
    }
    private static Font font = Fonts.ROBOTO_MEDIUM.get(15.0F);
    public float scale;
    public final Animation scaleAnimation = new EaseBackIn(350, 1, 1);
    public final Animation lineAnimation = new DecelerateAnimation(750, 1);
    public final Animation animation = new DecelerateAnimation(1000, 1);
    public String longest = "";
    double longestWidth = 0;
    @Override
    public boolean isVisible() {
        return true;
    }
    private static final dev.fesly.impl.util.animation.compact.Animation tap = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 155);
    @Override
    public void drawPre(double mouseX, double mouseY, float partialTicks) {
        Runnable keybinds = () -> drawKeybinds(true, false);
        IAccess.addRunnableWithChecks(keybinds);
    }
    @Override
    public void draw(double mouseX, double mouseY, float partialTicks) {
        drawKeybinds(false, false);
    }
    private float widthTo = 50;
    public static List<PotionConstructor> getAllPotions() {
        return potions;
    }
    private static final Comparator<Object> SORT_METHOD = Comparator.comparingDouble(m -> {
        Module module = (Module) m;
        String name = module.getName();
        return font.getWidth(name);
    }).reversed();
    public static List<PotionConstructor> potions = new ArrayList<>();
    public static float getScaleAnim() {
        return Math.max(1.0F - tap.getNumberValue().floatValue(), 0);
    }
    public void drawKeybinds(boolean blur, boolean plus) {
        boolean dragged = DraggableManager.dragged instanceof PotionHud;
        tap.run((dragged && !DraggableManager.draggedTimer.hasReached(150)) ? 0.01F : 0);
        assert mc.player != null;
        Collection<EffectInstance> potionsCollection = mc.player.getActivePotionEffects();
        List<EffectInstance> potionList = potionsCollection.stream().toList();
        List<PotionConstructor> potions = potionList.stream().map(PotionConstructor::new).collect(Collectors.toList());
        setWidth((int) widthTo);
        setHeight((int) animation.getOutput().floatValue() + 15);
        boolean empty = Objects.requireNonNull(potions).isEmpty();
        lineAnimation.setDuration(100);
        if (empty) {
            animation.setDuration(100);
            lineAnimation.setDirection(Direction.BACKWARDS);
            scaleAnimation.setDirection(Direction.BACKWARDS);
        } else
        {
            lineAnimation.setDirection(Direction.FORWARDS);
            scaleAnimation.setDirection(Direction.FORWARDS);
        }
        Font namefont = Fonts.ROBOTO_REGULAR.get(15.0F);
        font = Fonts.ROBOTO_MEDIUM.get(13.0F);
        float o = plus ? 0.35F : 0.0F;
        float t = o * 2;
        ScaleUtil.scaleStart((float) (x + width / 2), (float) (y + animation.getOutput() / 2), (float) 1);
        if (blur) {
            RenderUtil.roundedRectangle(x + o, y + o, width - t, (float) height - t, 5, new Color(1.0F, 1.0F, 1.0F, 1.0F));
            ScaleUtil.scaleEnd();
            return;
        }
        RenderHelper.disableStandardItemLighting();
        namefont.drawCenterX("Potions", x + width / 2.0F - 1.0F, y + namefont.getMiddleOfBox(15.5F) * 1.2F, -1);
        Color client = ClientUtil.getThreeClientColor();
        {
            float r = (Math.min((client.getRed() / 255.0F + 0.3F), 1.0F));
            float g = (Math.min(client.getGreen() / 255.0F + 0.3F, 1.0F));
            float b = (Math.min(client.getBlue() / 255.0F + 0.3F, 1.0F));
            float XX = x + 7;
            float YY = y + (namefont.getMiddleOfBox(14) * 1.1F) * 2.0F + namefont.getHeight();
            float WW = widthTo - 14;
            float HH = 0.5F;
            float center = XX + (WW / 2.0F);
            float XXX = (float) (center - ((WW / 2) * lineAnimation.getOutput()));
            float XXXX = (float) (center + ((WW / 2) * lineAnimation.getOutput()));
            AbstractGui.fill(new MatrixStack(), XXX, YY, XXXX, (YY + HH), new Color(r, g, b, 0.4F).getRGB());
        }
        float heightRound = 14;
        float yOffset = 5;
        int idIr = 0;
        float width = 50;
        for (int id = 0; id < potions.size(); id++) {
            PotionConstructor potion = potions.get(id);
            if (potion.animation.getDirection() == Direction.FORWARDS) {
                double one = font.getWidth(potion.potion.getPotion().getDisplayName().getString() + (potion.potion.getAmplifier() > 0 ? TextFormatting.RED + " " + (potion.potion.getAmplifier() + 1) : ""));
                double two = font.getWidth(potion.formatDuration());
                if ((one + two + 15) > width) {
                    width = (float) (one + two + 20);
                }
            }
        }
        for (int id = 0; id < potions.size(); id++) {
            PotionConstructor potion = potions.get(id);
            final Animation moduleAnimation = potion.animation;
            {
                moduleAnimation.setDirection(Direction.FORWARDS);
                idIr++;
            }
            if (moduleAnimation.finished(Direction.BACKWARDS)) continue;
            double hd = moduleAnimation.getOutput();
            font.draw(potion.potion.getPotion().getDisplayName().getString() + (potion.potion.getAmplifier() > 0 ? TextFormatting.RED + " " + (potion.potion.getAmplifier() + 1) : ""), x + 6, y + 10 + yOffset + 3, new Color(0.85F, 0.85F, 0.85F, ((float) 1.0F)).getRGB());
            font.draw(String.valueOf(potion.formatDuration()), (long) (x + widthTo - 8.0F - font.getWidth(String.valueOf(potion.formatDuration()))), y + 10 + yOffset + 3, new Color(0.85F, 0.85F, 0.85F, ((float) 1.0F)).getRGB());
            heightRound += (font.getHeight() + 5);
            yOffset += (font.getHeight() + 5);
        }
        if (idIr == 0) {
            heightRound = 15;
        } else {
            heightRound += 3;
        }
        float toAnim = heightRound - 15;
        if (idIr == 0) {
            animation.setDuration(100);
            animation.setDirection(Direction.BACKWARDS);
        } else {
            if (idIr == 1) {
                animation.setDuration(100);
            } else {
                animation.setDuration(100);
            }
            animation.setEndPoint(toAnim);
            animation.setDirection(Direction.FORWARDS);
        }
        widthTo = MathUtil.lerp(widthTo, width, 0.15F);
        ScaleUtil.scaleEnd();
    }
    public static class PotionConstructor {
        public final EffectInstance potion;
        public final Animation animation = new DecelerateAnimation(120, 1);
        private String formatDuration() {
            if (potion.getIsPotionDurationMax()) {
                return "**:**";
            } else {
                int i = MathHelper.floor((float) potion.getDuration());
                return StringUtils.ticksToElapsedTime(i);
            }
        }
        @java.lang.SuppressWarnings("all")
        public PotionConstructor(final EffectInstance potion) {
            this.potion = potion;
        }
    }
}
