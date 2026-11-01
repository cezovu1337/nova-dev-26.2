package dev.fesly.client.component.draggable.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.Nova;
import dev.fesly.client.component.draggable.Draggable;
import dev.fesly.client.component.draggable.DraggableManager;
import dev.fesly.client.module.api.Module;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.animation.extended.Animation;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.DecelerateAnimation;
import dev.fesly.impl.util.animation.extended.impl.EaseBackIn;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.other.KeyUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.render.StencilUtil;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class KeyBinds  extends Draggable {
    public KeyBinds() {
        super("keybinds", 150, 40, 50, 30);
    }
    private static Font font = Fonts.ROBOTO_MEDIUM.get(15F);

    public float scale;
    public final Animation scaleAnimation = new EaseBackIn(350, 1,1);
    public final Animation lineAnimation = new DecelerateAnimation(750, 1);
    public final Animation animation =  new DecelerateAnimation(1000, 1);
    public String longest = "";
    double longestWidth = 0;

    @Override
    public boolean isVisible() {
        return true;
    }



    @Override
    public void drawPre(double mouseX, double mouseY, float partialTicks) {

        Runnable keybinds = () -> drawKeybinds(true,false);
        IAccess.addRunnableWithChecks(keybinds);
    }
    @Override
    public void draw(double mouseX, double mouseY, float partialTicks) {
        drawKeybinds(false,false);
    }
    private float widthTo = 50;

    public static List<Module> getToggledAndBindedModules(List<Module> modules) {
        return modules.stream().filter(module -> module.getKeyCode() != 0 && module.isEnabled()).collect(Collectors.toList());
    }
    private static final Comparator<Object> SORT_METHOD = Comparator.comparingDouble(m -> {
        Module module = (Module) m;
        String name = module.getName();
        return font.getWidth(name);
    }).reversed();

    private static final dev.fesly.impl.util.animation.compact.Animation tap = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 155);

    public static float getScaleAnim() {
        return Math.max(1f - tap.getNumberValue().floatValue(),0);
    }

    public void drawKeybinds(boolean blur,boolean plus) {
        boolean dragged = DraggableManager.dragged instanceof KeyBinds;

        tap.run((dragged && !DraggableManager.draggedTimer.hasReached(150)) ? 0.01f : 0);
        //ScaleUtil.scaleStart(x + width / 2f, y + height / 2f, getScaleAnim());
        //animation.setDuration(2550);
        List<Module> modules = new ArrayList<>(Nova.getInstance().getModuleManager().getAll());
        //modules.sort(SORT_METHOD);
        if (!getToggledAndBindedModules(modules).isEmpty()) {
            Module firstMod = getToggledAndBindedModules(modules).get(0);
            longest = firstMod.getName();
            longestWidth = font.getWidth(longest);
        }
        setWidth((int) widthTo);
        setHeight((int) animation.getOutput().floatValue() + 15);
        boolean empty =  Objects.requireNonNull(getToggledAndBindedModules(modules)).isEmpty();
        lineAnimation.setDuration(100);
        if (empty) {
            animation.setDuration(100);
            lineAnimation.setDirection(Direction.BACKWARDS);
            scaleAnimation.setDirection(Direction.BACKWARDS);
            //return;
        } else {
            //animation.setDuration(1000);
            lineAnimation.setDirection(Direction.FORWARDS);
            scaleAnimation.setDirection(Direction.FORWARDS);
        }


        Font namefont = Fonts.ROBOTO_REGULAR.get(15F);
        font = Fonts.ROBOTO_MEDIUM.get(13F);

        float o = plus ? 0.35f : 0f;
        float t = o * 2;
        ScaleUtil.scaleStart((float) (x + width / 2), (float) (y + animation.getOutput() / 2), (float) 1);
        if (blur) {
            RenderUtil.roundedRectangle(x + o, y + o, width - t, (float) height - t, 5, new Color(1f, 1f, 1f, 1f));
            ScaleUtil.scaleEnd();
            return;
        }

        RenderHelper.disableStandardItemLighting();

        namefont.drawCenterX("KeyBinds", x + width / 2f - 1f, y + namefont.getMiddleOfBox(15.5f) * 1.2f,-1);
        Color client = ClientUtil.getThreeClientColor();
        {
            float r = (Math.min((client.getRed() / 255f + 0.3f), 1f));
            float g = (Math.min(client.getGreen() / 255f + 0.3f, 1f));
            float b = (Math.min(client.getBlue() / 255f + 0.3f, 1f));
            float XX = x + 7;
            float YY = y + (namefont.getMiddleOfBox(14) * 1.1f) * 2f + namefont.getHeight();
            float WW = widthTo - 14;
            float HH = 0.5f;
            float center = XX + ( WW / 2F);
            float XXX = (float) (center - ((WW / 2) * lineAnimation.getOutput()));
            float XXXX = (float) (center + ((WW / 2) * lineAnimation.getOutput()));
            AbstractGui.fill(new MatrixStack(),  XXX,  YY,  XXXX,  (YY + HH), new Color(r, g, b, 0.4f).getRGB());
        }
        float heightRound = 14;
        float yOffset = 5;
        int idIr = 0;
        float width = 50;
        for (int id = 0; id < modules.size(); id++) {
            Module module = modules.get(id);
            if (module.keyAnimation.getDirection() == Direction.FORWARDS) {
                double one = font.getWidth(module.getName());

                boolean lowerCase = Nova.getInstance().getClientUI().getKeyBindsMode().equals(ClientUI.TextMode.LowerCase);
                boolean upperCase = Nova.getInstance().getClientUI().getKeyBindsMode().equals(ClientUI.TextMode.UpperCase);

                String bind = KeyUtil.getKeyboardKey(module.getKeyCode());
                if (lowerCase) bind = bind.toLowerCase();
                if (upperCase) bind = bind.toUpperCase();

                double two = font.getWidth("[" + bind + "]");
                if ((one + two + 15) > width) {
                    width = (float) (one + two +20);
                }
            }
        }
        for (int id = 0; id < modules.size(); id++) {
            Module module = modules.get(id);
            final Animation moduleAnimation = module.keyAnimation;
            if (module.isEnabled() && module.getKeyCode() != 0) {
                moduleAnimation.setDirection(Direction.FORWARDS);
                idIr++;
            }
            else {
                moduleAnimation.setDirection(Direction.BACKWARDS);
            }

            if (moduleAnimation.finished(Direction.BACKWARDS)) continue;
            double hd = moduleAnimation.getOutput();

            StencilUtil.initStencil();
            RenderUtil.roundedRectangle(x + o, y + o, widthTo - t, (float) height - t, 4, new Color(1f, 1f, 1f, 1f));
            StencilUtil.bindReadStencilBuffer(1);
            boolean lowerCase = Nova.getInstance().getClientUI().getKeyBindsMode().equals(ClientUI.TextMode.LowerCase);
            boolean upperCase = Nova.getInstance().getClientUI().getKeyBindsMode().equals(ClientUI.TextMode.UpperCase);

            String bind = KeyUtil.getKeyboardKey(module.getKeyCode());
            if (lowerCase) bind = bind.toLowerCase();
            if (upperCase) bind = bind.toUpperCase();

            font.draw(module.getName(), x + 6, y + 10 + yOffset + 3,new Color(0.85f,0.85f,0.85f, ((float) moduleAnimation.getOutput().floatValue())).getRGB());
            font.draw("[" + bind + "]", (long) (x + widthTo - 8f - font.getWidth(
                    "[" + bind + "]")),
                    y + 10 + yOffset + 3,new Color(0.85f,0.85f,0.85f, ((float) moduleAnimation.getOutput().floatValue())).getRGB());
            StencilUtil.uninitStencilBuffer();


            heightRound += hd * (font.getHeight() + 5);

            yOffset += hd * (font.getHeight() + 5);
        }
        if (idIr == 0) {
            heightRound = 15;
        }else {
            heightRound += 3;
        }
        float toAnim = heightRound - 15;
        if (idIr == 0) {
            animation.setDuration(100);
            animation.setDirection(Direction.BACKWARDS);
        }else{
            if (idIr == 1) {
                animation.setDuration(100);
            }else{
                animation.setDuration(100);
            }
            animation.setEndPoint(toAnim);
            animation.setDirection(Direction.FORWARDS);
        }

        widthTo = MathUtil.lerp(widthTo,width,0.15f);


        ScaleUtil.scaleEnd();
        //ScaleUtil.scaleEnd();
    }
}
