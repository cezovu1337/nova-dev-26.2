package dev.fesly.client.command.impl;
import dev.fesly.client.command.Command;
import dev.fesly.impl.util.chat.ChatUtil;
import dev.fesly.impl.util.player.BlockUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.math.NumberUtils;
public final class Clip extends Command {
    public Clip() {
        super("Вы можете клипнуться в любую сторону в 3д пространстве", "clip", "vclip", "hclip");
    }
    private double getScanUp() {
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < 80; i++) {
            if (BlockUtils.getBlock(mc.player.getPosition().up(i)) != Blocks.AIR
                    && BlockUtils.getBlock(mc.player.getPosition().up(i + 1)) == Blocks.AIR
                    && BlockUtils.getBlock(mc.player.getPosition().up(i + 2)) == Blocks.AIR) {
                return i + 1;
            }
        }
        return 0;
    }
    private double getScanDown() {
        Minecraft mc = Minecraft.getInstance();
        for (int i = 2; i < 80; i++) {
            if (BlockUtils.getBlock(mc.player.getPosition().down(i)) != Blocks.AIR
                    && BlockUtils.getBlock(mc.player.getPosition().down(i - 1)) == Blocks.AIR
                    && BlockUtils.getBlock(mc.player.getPosition().down(i - 2)) == Blocks.AIR) {
                return i - 1;
            }
        }
        return 0;
    }
    @Override
    public void execute(final String[] args) {
        if (args.length <= 1 || args[1].isEmpty()) {
            error();
            return;
        }
        switch (args[0].toLowerCase()) {
            case "vclip" -> {
                if (NumberUtils.isNumber(args[1])) {
                    final double amount = Double.parseDouble(args[1]);
                    mc.player.setPosition(mc.player.getPositionVec().x, mc.player.getPositionVec().y + amount, mc.player.getPositionVec().z);
                    ChatUtil.addText("Вы клипнуты " + (amount > 0 ? "вверх" : "вниз") + " на " + Math.abs(amount) + " блоков.");
                }else {
                    if (args[1].equals("up")) {
                        double scan = getScanUp();
                        if (scan == 1 || scan == 0) {
                            ChatUtil.addText("Не подходящая территория для клипа вверх");
                            return;
                        }
                        clip(scan);
                    }else if (args[1].equals("down")) {
                        double scan = -getScanDown();
                        if (scan == -1 || scan == 0) {
                            ChatUtil.addText("Не подходящая территория для клипа вниз");
                            return;
                        }
                        clip(scan);
                    }
                }
            }
            case "hclip" -> {
                final double amount = Double.parseDouble(args[1]);
                final double yaw = Math.toRadians(mc.player.rotationYaw);
                final double x = Math.sin(yaw) * amount;
                final double z = Math.cos(yaw) * amount;
                mc.player.setPosition(mc.player.getPositionVec().x - x, mc.player.getPositionVec().y, mc.player.getPositionVec().z + z);
                ChatUtil.addText("Вы клипнуты  " + (amount > 0 ? "forward" : "back") + " на" + Math.abs(amount) + " blocks.");
            }
            case "clip" -> {
                if (args.length == 2 || args[2].isEmpty()) {
                    error();
                    return;
                }
                switch (args[1]) {
                    case "upward", "upwards", "up" -> {
                        final double amount = Double.parseDouble(args[2]);
                        mc.player.setPosition(mc.player.getPositionVec().x, mc.player.getPositionVec().y + amount, mc.player.getPositionVec().z);
                        ChatUtil.addText("Вы клипнуты вверх на " + amount + " блоков.");
                    }
                    case "downward", "downwards", "down" -> {
                        final double amount = Double.parseDouble(args[2]);
                        mc.player.setPosition(mc.player.getPositionVec().x, mc.player.getPositionVec().y - amount, mc.player.getPositionVec().z);
                        ChatUtil.addText("Вы клипнуты вниз на " + amount + " блоков.");
                    }
                    case "forwards", "forward" -> {
                        final double amount = Double.parseDouble(args[2]);
                        final double yaw = Math.toRadians(mc.player.rotationYaw);
                        final double x = Math.sin(yaw) * amount;
                        final double z = Math.cos(yaw) * amount;
                        mc.player.setPosition(mc.player.getPositionVec().x - x, mc.player.getPositionVec().y, mc.player.getPositionVec().z + z);
                        ChatUtil.addText("Вы клипнуты вперед на " + amount + " блоков.");
                    }
                    case "backwards", "backward", "back" -> {
                        final double amount = Double.parseDouble(args[2]);
                        final double yaw = Math.toRadians(mc.player.rotationYaw);
                        final double x = Math.sin(yaw) * amount;
                        final double z = Math.cos(yaw) * amount;
                        mc.player.setPosition(mc.player.getPositionVec().x + x, mc.player.getPositionVec().y, mc.player.getPositionVec().z - z);
                        ChatUtil.addText("Вы клипнуты назад на " + amount + " блоков.");
                    }
                    case "left" -> {
                        final double amount = Double.parseDouble(args[2]);
                        final double yaw = Math.toRadians(mc.player.rotationYaw - 90);
                        final double x = Math.sin(yaw) * amount;
                        final double z = Math.cos(yaw) * amount;
                        mc.player.setPosition(mc.player.getPositionVec().x - x, mc.player.getPositionVec().y, mc.player.getPositionVec().z + z);
                        ChatUtil.addText("Вы клипнуты влево на " + amount + " блоков.");
                    }
                    case "right" -> {
                        final double amount = Double.parseDouble(args[2]);
                        final double yaw = Math.toRadians(mc.player.rotationYaw + 90);
                        final double x = Math.sin(yaw) * amount;
                        final double z = Math.cos(yaw) * amount;
                        mc.player.setPosition(mc.player.getPositionVec().x - x, mc.player.getPositionVec().y, mc.player.getPositionVec().z + z);
                        ChatUtil.addText("Вы клипнуты вправо на " + amount + " блоков.");
                    }
                    default -> error();
                }
            }
            default -> error();
        }
    }
    private void clip(double range) {
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < 10; i++) {
            mc.player.connection
                    .sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), false));
        }
        for (int i = 0; i < 10; i++) {
            mc.player.connection.sendPacket(
                    new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + range, mc.player.getPosZ(), false));
        }
        mc.player.setPosition(mc.player.getPosX(), mc.player.getPosY() + range, mc.player.getPosZ());
        ChatUtil.addText(TextFormatting.WHITE + "Вы клипнуты на " + TextFormatting.GREEN + (int) range + " блоков");
    }
}
