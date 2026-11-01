package dev.fesly.client.module.impl.misc;

import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.ChatInputEvent;
import dev.fesly.impl.event.impl.input.ChatReceiveEvent;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.play.server.SChatPacket;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(
        name = "AutoTpAccept",
        category = Category.MISC,
        description = "Какая то хуета под ногами"
)
public class AutoTpAccept extends Module {

    @Getter
    @Setter
    public boolean checkFriend = true;

    @Override
    public void init() {
        super.init();

        addSetting("checkFriend",new BooleanSetting("Check Friend",this,this::isCheckFriend,this::setCheckFriend));
    }

    public List<String> lastMessages = new ArrayList<>();

    @EventLink
    private final Listener<PacketReceiveEvent> onPacket = event -> {
        if (event.getPacket() instanceof SChatPacket wrapper) {
            if (lastMessages.size() > 10) lastMessages.remove(0);
            lastMessages.add(wrapper.getChatComponent().getString());


            List<String> acceptWords = List.of("просит телепортироваться", "Заявка буудет автоматически отменена через"
                    , "просит телепортироваться к Вам.", "120 секунд", "has requested to teleport", "teleport to you", "This request will timeout after", "120 seconds");

      /*  for (String word : acceptWords) {
            if (event.getMessage().contains(word)) {
                System.out.println(event.getMessage().toUpperCase());
            }
        }*/

            boolean containsFriends = false;
            boolean containsWords = false;

            for (String lastMessage : lastMessages) {
                for (String acceptWord : acceptWords) {
                    if (!containsWords) {
                        containsWords = lastMessage.contains(acceptWord);
                    }
                }
                containsFriends = Nova.getFriendManager().stream().anyMatch(friend -> lastMessage.contains(friend.getName()));
            }
            //System.out.println("words : " + containsWords + " friends :" + containsFriends);

            if ((!checkFriend || containsFriends) && containsWords && !lastMessages.contains("Teleport request accepted.")) {
                assert mc.player != null;
                mc.player.sendChatMessage("/tpaccept");
                lastMessages.clear();
            }
        }
    };

    public static AutoTpAccept getInstance() {
        return nova.getModuleManager().get(AutoTpAccept.class);
    }
}
