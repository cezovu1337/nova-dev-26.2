package dev.fesly.impl.util.color;
import dev.fesly.Nova;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.interfaces.client.IAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.compress.utils.IOUtils;
import org.w3c.dom.Text;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
public final class ClientUtil implements IAccess {
    public static double calculateBPS() {
        Minecraft mc = Minecraft.getInstance();
        double bps = (((Math.hypot(mc.player.getPosX() - mc.player.prevPosX, mc.player.getPosZ() - mc.player.prevPosZ))) * mc.timer.timerSpeed) * 20;
        return Math.round(bps * 100.0) / 100.0;
    }
    public static boolean isThirdPerson() {
        double xqw = mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX) * (double) mc.getRenderPartialTicks();
        double zqw = mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ) * (double) mc.getRenderPartialTicks();
        return mc.getRenderManager().info.getProjectedView().getX() == xqw || mc.getRenderManager().info.getProjectedView().getZ() == zqw;
    }
    public static void startPlaySound(ResourceLocation wavLocation) {
        Runnable playSoundTask = () -> {
            try {
                InputStream inputStream = Minecraft.getInstance().getResourceManager().getResource(wavLocation).getInputStream();
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        };
        Thread soundThread = new Thread(playSoundTask);
        soundThread.start();
    }
    public static void startPlaySound(ResourceLocation wavLocation, float volume) {
        Runnable playSoundTask = () -> {
            try {
                InputStream inputStream = Minecraft.getInstance().getResourceManager().getResource(wavLocation).getInputStream();
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
                AudioFormat format = audioInputStream.getFormat();
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);
                clip.start();
            } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        };
        Thread soundThread = new Thread(playSoundTask);
        soundThread.start();
    }
    public static Color getOneClientColor() {
        if (Nova.getInstance().getClientUI().getColorEnum() == ClientUI.ColorEnum.Exquisite_Blue) return Colors.My_Blue.getColor();
         else if (Nova.getInstance().getClientUI().getColorEnum() == ClientUI.ColorEnum.Exquisite_Purple) return Colors.Client_Purple.getColor();
         else if (Nova.getInstance().getClientUI().getColorEnum() == ClientUI.ColorEnum.White) return Colors.WHITE.getColor();
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Astolfo)) return ColorUtil.skyRainbow((int) Nova.getInstance().getClientUI().getColorSpeed(), (int) (System.currentTimeMillis() / System.nanoTime()));
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Rainbow)) return Colors.RNB_ONE.getColor();
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Orange)) return Colors.My_Orange.getColor();
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Blue_Purple)) {
            return Colors.Client_Blue.getColor();
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Lime)) {
            return Colors.My_Lime.getColor();
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Orange_White)) {
            return Colors.WHITE.getColor();
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Custom)) {
            return Nova.getInstance().getClientUI().getCustomColor1();
        }
        return Color.WHITE;
    }
    public static Color getTwoClientColor() {
        if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Blue)) return Colors.My_Two_Blue.getColor().darker().darker().darker().darker().brighter().brighter().brighter().brighter();
         else if (Nova.getInstance().getClientUI().getColorEnum() == ClientUI.ColorEnum.Exquisite_Purple) return Colors.Exquisite_Purple.getColor();
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.White)) return Colors.WHITE.getColor();
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Astolfo)) {
            return ColorUtil.skyRainbow((int) Nova.getInstance().getClientUI().getColorSpeed(), (int) Math.sin(System.currentTimeMillis() - System.nanoTime()) / 2 + 230);
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Rainbow)) return Colors.RNB_TWO.getColor();
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Orange)) {
            return Colors.My_Two_Orange.getColor().brighter();
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Blue_Purple)) {
            return Colors.Client_Purple.getColor();
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Lime)) {
            return Colors.My_Two_Lime.getColor().brighter();
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Orange_White)) {
            return Colors.My_Two_Lime.getColor();
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Custom)) {
            return Nova.getInstance().getClientUI().getCustomColor2();
        }
        return Color.WHITE;
    }
    public static TextFormatting getClientFormatting() {
        if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Blue)) return TextFormatting.AQUA;
         else if (Nova.getInstance().getClientUI().getColorEnum() == ClientUI.ColorEnum.Exquisite_Purple) return TextFormatting.DARK_PURPLE;
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Astolfo)) {
            return TextFormatting.AQUA;
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Rainbow)) return TextFormatting.RED;
         else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Orange)) {
            return TextFormatting.YELLOW;
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Blue_Purple)) {
            return TextFormatting.DARK_PURPLE;
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Exquisite_Lime)) {
            return TextFormatting.GREEN;
        } else if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Orange_White)) {
            return TextFormatting.WHITE;
        }
        return TextFormatting.WHITE;
    }
    public static Color getThreeClientColor() {
        if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Rainbow)) return Colors.RNB_THREE.getColor();
        return getOneClientColor();
    }
    public static Color getFourClientColor() {
        if (Nova.getInstance().getClientUI().getColorEnum() == (ClientUI.ColorEnum.Rainbow)) return Colors.RNB_FOUR.getColor();
        return getTwoClientColor();
    }
    public static String obfuscateSection(String text, float T) {
        StringBuilder returned = new StringBuilder();
        int toObfuscaleLength = text.length() * ((int) (T));
        for (int i = 0; i < text.length(); i++) {
            if (i <= toObfuscaleLength) {
                returned.append(TextFormatting.OBFUSCATED).append(text.charAt(i)).append(TextFormatting.RESET);
            }
            returned.append(text.charAt(i));
        }
        return returned.toString();
    }
    @java.lang.SuppressWarnings("all")
    private ClientUtil() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
