package dev.fesly.client.module.impl.misc;

import com.google.common.collect.Lists;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.notification.Notification;
import dev.fesly.client.notification.NotificationManager;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.event.impl.render.WorldRenderEvent;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.color.ColorUtil;
import dev.fesly.impl.util.player.BlinkUtils;
import dev.fesly.impl.util.player.MoveUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.value.secondValues.MultiEnumSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.OreBlock;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@ModuleInfo(
        name = "XrayBypass",
        category = Category.MISC,
        description = "Байпасс Анти Иксрея на серверах с ним"
)

public class XrayBypass extends Module {

    @Getter
    @Setter
    public static boolean spawnGhost = true;
    @Getter
    @Setter
    public static float range = 16;
    @Getter
    @Setter
    public static float skipDistance = 2;
    @Getter
    @Setter
    public static EnumSet<OresEnum> oresEnum = EnumSet.of(OresEnum.Diamond,OresEnum.Iron,OresEnum.Netherite);
    @Getter
    @Setter
    public static float blocksPerTick = 10;
    @Getter
    @Setter
    public static float delay = 0;

    public enum OresEnum {
        Ores,
        Stone,
        Diamond,
        Redstone,
        Iron,
        Netherite,
        Everything
    }

    List<BlockPos> toScan = Lists.newArrayList();
    Vector3d startPos;
    List<BlockPos> renders = new ArrayList<>();
    int scanned = 0;
    int delayPassed = 0;

    @Override
    public void init() {
        super.init();
        //addSetting("spawnGhost",new BooleanSetting("Spawn Ghost", this, XrayBypass::isSpawnGhost, XrayBypass::setSpawnGhost));
        addSetting("skipDistance", new NumberSetting("Skip Distance ", this,XrayBypass::getRange, XrayBypass::setRange,"%.0f",2,64));
        addSetting("mode", new MultiEnumSetting<>("Mode", this,XrayBypass::getOresEnum, XrayBypass::setOresEnum,OresEnum.class));
        addSetting("blocksPerTick", new NumberSetting("BlocksPerTick", this,XrayBypass::getBlocksPerTick, XrayBypass::setBlocksPerTick,"%.0f",1,20));
        addSetting("delay", new NumberSetting("Delay", this,XrayBypass::getDelay, XrayBypass::setDelay,"%.0f",0,20));

    }

    private BlinkUtils blinkUtil = new BlinkUtils();


    boolean isBlockValid(Block b) {
        if (oresEnum.contains(OresEnum.Ores)) {
            return  b instanceof OreBlock;
        }
        else  if (oresEnum.contains(OresEnum.Stone)) {
            return  b == Blocks.STONE;
        }
        else  if (oresEnum.contains(OresEnum.Diamond)) {
            return  b == Blocks.STONE;
        }
        else if (oresEnum.contains(OresEnum.Redstone)) {
            return  b == Blocks.STONE;
        }
        else if (oresEnum.contains(OresEnum.Iron)) {
            return  b == Blocks.STONE;
        }else if (oresEnum.contains(OresEnum.Netherite)) {
            return  b == Blocks.STONE;
        }else {
            return false;
        }
    }

    @EventLink()
    public final Listener<UpdateEvent> onPlayerUpdate = event -> {
        if (mc.player == null || mc.getConnection() == null) return;
        if (toScan.size() == 0) {
            NotificationManager.sendNotification(new Notification("Trying"));
            toggle();
            return;
        }else {
            //NotificationManager.sendNotification(new Notification("АЛО НАХУЙ"));
        }
        if (delayPassed > delay) {
            delayPassed = 0;
        } else {
            delayPassed++;
            return;
        }
        renders.clear();
        for (int i = 0; i < blocksPerTick; i++) {
            if (toScan.size() == 0) break;
            BlockPos current = toScan.get(0);
            renders.add(current);
            toScan.remove(0);
            scanned++;

            BlockRayTraceResult result = new BlockRayTraceResult(new Vector3d(current.getX(),current.getY(),current.getZ()),Direction.DOWN,mc.player.getPosition(),false);

            CPlayerDiggingPacket p = new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.ABORT_DESTROY_BLOCK, current,
                    Direction.DOWN);

            mc.player.connection.sendPacket(p);
        }
        this.prefix = "" + scanned + "S; " + toScan.size() + "R";
    };

    @EventLink()
    public final Listener<WorldRenderEvent> onWorldRenderer = event -> {
        int id = 1;
        for (BlockPos latestScan : renders.toArray(new BlockPos[0])) {
            Color c = ColorUtil.interpolateTwoColors(15, id * 16, ClientUtil.getOneClientColor(), ClientUtil.getTwoClientColor());

            RenderUtil.drawFilledBox
                    (new AxisAlignedBB(latestScan.getX(), latestScan.getY() + 0.4f, latestScan.getZ(),
                                    latestScan.getX() - 1, latestScan.getY() - 1 + 0.4f, latestScan.getZ() - 1),
                            c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f);

            id++;
        }
       double mid = this.range / 2;
        Vector3d s = new Vector3d(mid, mid, mid);
        Vector3d ppOrigin = startPos;
        Vector3d boxOrigin = ppOrigin.subtract(s);
        Color c = ClientUtil.getOneClientColor();

        RenderUtil.drawOutlineBox(new AxisAlignedBB(boxOrigin.getX() - 1, boxOrigin.getY() - 1, boxOrigin.getZ() - 1,
                s.mul(4).getX(), s.mul(4).getY(), s.mul(4).getZ()),
                c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f);

        //RenderHelper.renderOutline(boxOrigin, s.mul(2), ClientUtil.getOneClientColor(), event.getMatrixStack());
    };

    @Override
    protected void onEnable() {
        super.onEnable();
        if (mc.player == null || mc.world == null) return;
        toScan.clear();
        scanned = 0;
        startPos = mc.player.getPositionVec();
        BlockPos ppos = mc.player.getPosition();
        Vector3d lastPos = Vector3d.ZERO;
        int rangeMid = (int) (range / 2);

        for (int y = -rangeMid; y <= rangeMid; y++) {
            for (int x = -rangeMid; x <= rangeMid; x++) {
                for (int z = -rangeMid; z <= rangeMid; z++) {
                    BlockPos current = ppos.add(x, y, z);
                    BlockState bs = mc.world.getBlockState(current);
                    {
                        if (!bs.isAir()){
                            //NotificationManager.sendNotification(new Notification("Cпасибо ( Post )"));
                            toScan.add(current);
                            lastPos = Vector3d.copyCentered(current);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        blinkUtil.stopBlinking();
        MoveUtil.setSpeed(0.1f);
        Nova.getInstance().getEventBus().unregister(blinkUtil);
    }



    public static XrayBypass getInstance() {
        return nova.getModuleManager().get(XrayBypass.class);
    }
}
