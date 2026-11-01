package dev.fesly.client.component.draggable.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.client.component.draggable.Draggable;
import dev.fesly.client.component.draggable.DraggableManager;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
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
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StaffList extends Draggable {
    public StaffList() {
        super("staffList", 150, 100, 100, 30);
    }
    private static Font font = Fonts.ROBOTO_MEDIUM.get(15F);

    public float scale;
    public final Animation scaleAnimation = new EaseBackIn(350, 1,1);
    public final Animation lineAnimation = new DecelerateAnimation(750, 1);
    public final Animation animation =  new DecelerateAnimation(1000, 1);
    public String longest = "";
    double longestWidth = 0;

    private static final Pattern validUserPattern = Pattern.compile("^\\w{3,16}$");

    List<String> players = new ArrayList<>();
    List<String> notSpec = new ArrayList<>();


    @Override
    public boolean isVisible() {
        return true;
    }


    private static final dev.fesly.impl.util.animation.compact.Animation tap = new dev.fesly.impl.util.animation.compact.Animation(Easing.EASE_OUT_CUBIC, 155);

    @EventLink
    public Listener<UpdateEvent> eventListener = event -> {
        if (mc.player.ticksExisted % 10 == 0) {
            players = getVanish();
            notSpec = getOnlinePlayerD();
            players.sort(String::compareTo);
            notSpec.sort(String::compareTo);
        }
    };
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

    public static List<StaffConstructor> getAllPotions() {
        return staffList;
    }

    public List<NetworkPlayerInfo> getSortedPlayers() {
        List<String> sortStrings = List.of("Helper", "Moder", "Admin", "Хелпер", "Модер", "Куратор", "youtube", "YouTube", "сотрудник", "Porfumaster");
        List<NetworkPlayerInfo> sortedPlayers = new ArrayList<>();

        if (mc.player != null) {
            List<? extends NetworkPlayerInfo> players = mc.player.connection.getPlayerInfoMap().stream().toList();
            players.stream()
                    .filter(player ->
                            sortStrings.stream().anyMatch(s ->
                                    player.getDisplayName() != null && player.getDisplayName().getString().contains(s)))
                    .sorted(Comparator.comparing(player -> player.getDisplayName().getString()))
                    .forEach(sortedPlayers::add);


        }

        return sortedPlayers;
    }


    public static List<StaffConstructor> staffList = new ArrayList<>();

    public static float getScaleAnim() {
        return Math.max(1f - tap.getNumberValue().floatValue(),0);
    }

    public void drawKeybinds(boolean blur,boolean plus) {
        font = Fonts.ROBOTO_MEDIUM.get(13F);

       // if (players.isEmpty() || notSpec.isEmpty()) return;
        //animation.setDuration(2550);

        boolean dragged = DraggableManager.dragged instanceof StaffList;

        tap.run((dragged && !DraggableManager.draggedTimer.hasReached(150)) ? 0.01f : 0);
        //ScaleUtil.scaleStart(x + width / 2f, y + height / 2f, getScaleAnim());
        assert mc.player != null;

        List<String> all = new ArrayList<>();
        all.addAll(players);
        all.addAll(notSpec);

        font = Fonts.ROBOTO_MEDIUM.get(12F);


       /* List<StaffConstructor> staffList = playersList.stream()
                .map(StaffConstructor::new)
                .collect(Collectors.toList());*/

        boolean empty =  Objects.requireNonNull(staffList).isEmpty();
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

        float o = plus ? 0.35f : 0f;
        float t = o * 2;
        ScaleUtil.scaleStart((float) (x + width / 2), (float) (y + animation.getOutput() / 2), (float) 1);
        if (blur) {
            RenderUtil.roundedRectangle(x + o, y + o, width - t, (float) height - t, 5, new Color(1f, 1f, 1f, 1f));

            ScaleUtil.scaleEnd();
            return;
        }


        //RenderHelper.disableStandardItemLighting();

        namefont.drawCenterX("StaffList", x + width / 2f - 1f, y + namefont.getMiddleOfBox(15.5f) * 1.2f,-1);

        if (all.isEmpty()) {
            setWidth((int) 50);
            setHeight((int) 15);
            ScaleUtil.scaleEnd();
            return;
        }

        float width = all.stream().max(Comparator.comparingDouble
                (font::getWidth)).map(
                font::getWidth).get().floatValue()  + 7;
        float height2 = 10 + (notSpec.size() + players.size()) * 13;
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
            float XXX = (float) (center - ((WW / 2)));
            float XXXX = (float) (center + ((WW / 2)));
            AbstractGui.fill(new MatrixStack(),  XXX,  YY,  XXXX,  (YY + HH), new Color(r, g, b, 0.4f).getRGB());
        }
        float heightRound = 14;
        float yOffset = 5;
        int idIr = 0;
        //float width = 50;
        for (int id = 0; id < all.size(); id++) {
            String player = all.get(id);
            {
                idIr++;
            }
            try {
                String[] allM = player.split(":");
                String donate = allM[0];
                String namestaff = allM[1];

                if (donate.toLowerCase().contains("helper")) {
                    donate = TextFormatting.GOLD + donate;
                } else if (donate.toLowerCase().contains("moder")) {
                    donate = TextFormatting.BLUE + donate;
                } else if (donate.toLowerCase().contains("yt")) {
                    donate = TextFormatting.RED + String.valueOf( donate.toCharArray()[0]) + TextFormatting.WHITE + String.valueOf(donate.toCharArray()[1]) + " ";
                } else if (donate.toLowerCase().contains("admin")) {
                    donate = TextFormatting.DARK_RED + donate;
                } else if (donate.toLowerCase().contains("мл.сотрудник")) {
                    donate = TextFormatting.YELLOW + donate;
                } else if (donate.toLowerCase().contains("сотрудник")) {
                    donate = TextFormatting.DARK_RED + donate;
                }else if (donate.toLowerCase().contains("админ")) {
                    donate = TextFormatting.DARK_RED + donate;
                }

                /*StencilUtil.initStencil();
                RenderUtil.roundedRectangle(x + o, y + o, widthTo - t, (float) height - t, 4, new Color(1f, 1f, 1f, 1f));
                StencilUtil.bindReadStencilBuffer(1);
                assert player != null;*/
                font.draw( donate + TextFormatting.WHITE + namestaff, x + 6, y + 10 + yOffset + 3, new Color(0.85f, 0.85f, 0.85f, ((float) 1f)).getRGB());

                font.draw(player.split(":")[2]
                        , (long) (x + widthTo - 8f - font.getWidth(player.split(":")[2])),
                        y + 10 + yOffset + 3, new Color(0.85f, 0.85f, 0.85f, ((float) 1f)).getRGB());
              /*  StencilUtil.uninitStencilBuffer();*/
            }catch (Exception ex) {
                height2 = 20;
            }

            heightRound += (font.getHeight() + 5);

            yOffset +=  (font.getHeight() + 5);
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

        widthTo = MathUtil.lerp(widthTo,width + 15,0.15f);

        setHeight(height2 + 6);
        setWidth(widthTo);
        ScaleUtil.scaleEnd();
        //ScaleUtil.scaleEnd();
    }

    @RequiredArgsConstructor
    public static class StaffConstructor {
        public final NetworkPlayerInfo player;
        public final Animation animation = new DecelerateAnimation(120, 1);

        private String status() {
            assert player.getGameType() != null;
            if (player.getGameType().equals(GameType.SPECTATOR)) {
                return "[Spec]";
            }
            return "[Not spec]";
        }
    }

    public static List<String> getOnlinePlayer() {
        return mc.player.connection.getPlayerInfoMap().stream().map(NetworkPlayerInfo::getGameProfile).map(GameProfile::getName).filter(profileName -> validUserPattern.matcher(profileName).matches()).collect(Collectors.toList());
    }
    public static List<String> getOnlinePlayerD() {
        List<String> S = new ArrayList<>();
        for (NetworkPlayerInfo player : mc.player.connection.getPlayerInfoMap()) {
            if (mc.isSingleplayer() || player.getPlayerTeam() == null) break;
            String prefix = player.getPlayerTeam().getColor() +player.getPlayerTeam().getPrefix().getString();

            if (check(TextFormatting.getTextWithoutFormattingCodes(prefix).toLowerCase())
                    || player.getGameProfile().getName().toLowerCase().contains("1danil_mansoru1") || player.getPlayerTeam().getPrefix().getString().contains("YT")) {
                String name = Arrays.asList(player.getPlayerTeam().getMembershipCollection().stream().toArray()).toString().replace("[", "").replace("]", "");

                if (player.getGameType() == GameType.SPECTATOR) {
                    S.add(player.getPlayerTeam().getPrefix().getString() + TextFormatting.RESET + ":"+ name +":"+ TextFormatting.BLUE + "Vanish");
                    continue;
                }
                S.add(player.getPlayerTeam().getPrefix().getString()+  TextFormatting.RESET + ":"+ name +":"+TextFormatting.GREEN + "Active");
            }
        }
        return S;
    }
    public List<String> getVanish() {
        List<String> list = new ArrayList<>();
        for (ScorePlayerTeam s : mc.world.getScoreboard().getTeams()) {
            if (s.getPrefix().getString().length() == 0 || mc.isSingleplayer()) continue;
            String name = Arrays.asList(s.getMembershipCollection().stream().toArray()).toString().replace("[", "").replace("]", "");

            if (getOnlinePlayer().contains(name) || name.isEmpty())
                continue;
            if (check(s.getPrefix().getString().toLowerCase()) || name.toLowerCase().contains("1danil_mansoru1") || s.getPrefix().getString().contains("YT"))
                list.add(s.getPrefix().getString() +  TextFormatting.RESET + ":"+ name +":"+ TextFormatting.BLUE + "Vanish");
        }
        return list;
    }
    public static boolean check(String name) {
        return name.contains("helper") ||
                name.contains("yt") ||
                name.contains("DEVELOPER") ||
                name.contains("moder") ||
                name.contains("admin") ||
                name.contains("owner") ||
                name.contains("хелпер") ||
                name.contains("модер") ||
                name.contains("админ") ||
                name.contains("сотрудник") ||
                name.contains("мл.сотрудник") ||
                name.contains("модератор");
    }
}
