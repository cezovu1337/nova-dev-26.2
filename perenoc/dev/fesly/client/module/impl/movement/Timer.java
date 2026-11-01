package dev.fesly.client.module.impl.movement;

import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.client.notification.Notification;
import dev.fesly.client.notification.NotificationManager;
import dev.fesly.font.Fonts;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.motion.MotionEvent;
import dev.fesly.impl.event.impl.render.DrawHudPostEvent;
import dev.fesly.impl.event.impl.render.DrawHudPreEvent;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.extended.impl.EaseBackIn;
import dev.fesly.impl.util.animation.extended.impl.EaseOutSine;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.NumberSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.status.client.CServerQueryPacket;

import java.awt.*;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

@ModuleInfo(
        name = "Timer",
        description = "",
        category = Category.MOVEMENT
)
@Getter
@Setter
public class Timer extends Module {

    private boolean grimBypass = true;
    private float speed = 1.1f;
    private boolean smart = true;

    private int bindButton = -1;
    @Override
    public void init() {
        super.init();
        addSetting("grimBypass", new BooleanSetting("Grim Bypass", this, this::isGrimBypass,this::setGrimBypass));
        addSetting("speed",new NumberSetting("Timer Speed",this,this::getSpeed,this::setSpeed,"%.2f",0,10));
        addSetting("smart", new BooleanSetting("Smart Hud",this,this::isGrimBypass, this::isSmart,this::setSmart));

    }

    private Queue<IPacket< ? >> packets = new LinkedList<>();

    public EaseOutSine openAnim = new EaseOutSine(650,1);


    @Override public void onDisable()
    {
        if (grimBypass) {
            mc.timer.timerSpeed = 1f;

            packets.clear();
        }else {
            mc.timer.timerSpeed = 1f;
        }
        openAnim.setDirection(false);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        //mc.timer.timerSpeed = speed * MathUtil.randomizeFloat(1,1.03f);
    }

    public String getInfo()
    {
        return String.valueOf( packets.size() );
    }

    @EventLink
    public Listener<DrawHudPreEvent> onHudPre = event -> {
        float center = mw.getScaledWidth() / 2f;
        float y = mw.getScaledHeight() / 6f;

        if (smart && isGrimBypass()) {
            openAnim.setDirection(true);
            IAccess.addRunnableWithChecks(
                    () -> RenderUtil.roundedRectangle(center - 50, y - 5 - (250 - (openAnim.getOutput().floatValue() * 250)), 100, 10, 3, new Color(-1)));

        }
    };

    @EventLink
    public Listener<DrawHudPostEvent> onHudPost = event -> {
        float center = mw.getScaledWidth() / 2f;
        float y = mw.getScaledHeight() / 6f;

        if (smart && isGrimBypass()) {
            RenderUtil.roundedRectangle(center - 50, y - 5 - (250 - (openAnim.getOutput().floatValue() * 250)), packets.size() / 100f * 20, 10, 3, ClientUtil.getOneClientColor());
        }
        };

    @EventLink
    public Listener<MotionEvent> onMotionUpdate = event -> {
        if( nullCheck() ) return;
        {
            if((!grimBypass || !packets.isEmpty()) && isMovingExtended() )
            {
                mc.timer.timerSpeed = this.speed;
                if (grimBypass)
                    mc.player.connection.sendPacket(Objects.requireNonNull(packets.poll()));
            }
            else
                mc.timer.timerSpeed = 1f;
        }
    };


    @EventLink
    public Listener<PacketReceiveEvent> packetReceiveEventListener = event -> {

    };

    @EventLink
    public Listener<PacketSendEvent> packetSendEventListener = event -> {
        if( nullCheck()  || !grimBypass) return;

        if( event.getPacket() instanceof CConfirmTransactionPacket packet )
        {
            if( !isMovingExtended() && packets.size() < 500 )
            {
                event.setCancelled();
                packets.add(packet);
            }


            if( !packets.isEmpty() )
                event.setCancelled();
        }
    };

    private boolean isMovingExtended()
    {
        return
                mc.player.movementInput.rightKeyDown || mc.player.movementInput.leftKeyDown ||
                        mc.player.movementInput.backKeyDown || mc.player.movementInput.forwardKeyDown ||
                        ( ( mc.player.getPosX() - mc.player.prevPosX ) != 0 ) ||
                        ( ( mc.player.getPosY() - mc.player.prevPosY ) != 0 ) ||
                        ( ( mc.player.getPosZ() - mc.player.prevPosZ ) != 0 );
    }

    public boolean nullCheck() {
        return mc.player == null || mc.world == null;
    }
}
