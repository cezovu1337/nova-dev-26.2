package dev.fesly.client.module.impl.misc;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.ChatReceiveEvent;
import dev.fesly.impl.event.impl.input.PacketReceiveEvent;
import dev.fesly.impl.util.chat.ChatUtil;
import dev.fesly.impl.util.player.BlinkUtils;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import dev.fesly.impl.value.secondValues.StringSetting;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
@ModuleInfo(name = "AutoAuth", category = Category.MISC, description = "Автоматический вход на сервер")
public class AutoAuth extends Module {
    private String password = "qwerty123";
    @Override
    public void init() {
        super.init();
        addSetting("password", new StringSetting("Password", this, this::getPassword, this::setPassword));
    }
    public List<String> lastMessages = new ArrayList<>();
    @EventLink
    private final Listener<PacketReceiveEvent> onPacket = event -> {
        if (mc.player == null) return;
        if (event.getPacket() instanceof SChatPacket wrapper) {
            if (lastMessages.size() > 10) lastMessages.remove(0);
            lastMessages.add(wrapper.getChatComponent().getString());
            List<String> acceptWords = List.of("reg", "register", "Зарегистрируйтесь", "/reg Пароль ПовторитеПароль");
            List<String> loginWords = List.of("login", "Авторизуйтесь", "/login Пароль", "/login <password>");
            boolean containsWords = false;
            for (String lastMessage : lastMessages) {
                for (String acceptWord : acceptWords) {
                    if (!containsWords) {
                        containsWords = lastMessage.contains(acceptWord);
                    }
                }
            }
            boolean containsLoginWords = false;
            for (String lastMessage : lastMessages) {
                for (String acceptWord : loginWords) {
                    if (!containsWords) {
                        containsLoginWords = lastMessage.contains(acceptWord);
                    }
                }
            }
            boolean containsRegister = containsWords;
            boolean containsLogin = containsLoginWords;
            String emptyField = "Вы не указали пароль, регистрация под паролем " + TextFormatting.GREEN + "qwerty123";
            String success = "Ваш аккаунт был успешно зарегистрирован";
            String password = this.password;
            if (containsLogin || containsRegister) {
                if (password == null || password.equals("")) {
                    password = "qwerty123";
                    assert mc.player != null;
                    if (containsRegister) {
                        if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                            ChatUtil.addText(emptyField);
                            mc.player.sendChatMessage("/reg " + password + " " + password);
                        }
                    }
                    if (containsLogin) {
                        if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                            mc.player.sendChatMessage("/l " + password);
                        }
                    }
                    lastMessages.clear();
                } else {
                    assert mc.player != null;
                    if (containsRegister) {
                        if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                            mc.player.sendChatMessage("/reg " + password + " " + password);
                        }
                    }
                    if (containsLogin) {
                        if (!lastMessages.contains(emptyField) && !lastMessages.contains(success)) {
                            mc.player.sendChatMessage("/l " + password);
                        }
                    }
                    lastMessages.clear();
                }
            }
        }
    };
    public static AutoAuth getInstance() {
        return nova.getModuleManager().get(AutoAuth.class);
    }
    @java.lang.SuppressWarnings("all")
    public String getPassword() {
        return this.password;
    }
    @java.lang.SuppressWarnings("all")
    public void setPassword(final String password) {
        this.password = password;
    }
}
