package dev.fesly;
import com.google.common.base.MoreObjects;
import com.google.gson.JsonObject;
import dev.fesly.client.bot.BotManager;
import dev.fesly.client.command.CommandManager;
import dev.fesly.client.component.ComponentManager;
import dev.fesly.client.component.draggable.DraggableManager;
import dev.fesly.client.friend.FriendManager;
import dev.fesly.client.module.api.ModuleManager;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.client.notification.NotificationManager;
import dev.fesly.client.screen.clientmenu.ClientMenu;
import dev.fesly.client.theme.ThemeManager;
import dev.fesly.impl.client.ClientInfo;
import dev.fesly.impl.client.Release;
import dev.fesly.impl.client.User;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.client.clientui.ClientUiScreen;
import dev.fesly.impl.client.screen.clickui.BaseScreen;
import dev.fesly.impl.config.ConfigFile;
import dev.fesly.impl.config.ConfigManager;
import dev.fesly.impl.discord.Discord;
import dev.fesly.impl.event.Event;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.bus.impl.EventBus;
import dev.fesly.impl.event.impl.input.PacketSendEvent;
import dev.fesly.impl.event.impl.motion.UpdateEvent;
import dev.fesly.impl.event.impl.player.PositionSendEvent;
import dev.fesly.impl.util.file.FileManager;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPlayerPacket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.awt.*;
import java.net.URI;

public class Nova {
    @java.lang.SuppressWarnings("all")
    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(Nova.class);
    private static final Nova instance = new Nova();
    private final ClientInfo clientInfo = new ClientInfo("Nova", "nova", "1.0", "ЗАВТРА", Release.DEV);
    public static final boolean DEBUG = getInstance().getClientInfo().getRelease().equals(Release.DEV);
    private User user;
    private EventBus<Event> eventBus;
    private ModuleManager moduleManager;
    private ComponentManager componentManager;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private FileManager fileManager;
    private BotManager botManager;
    public static FriendManager friendManager;
    private ThemeManager themeManager;
    private Discord discord;
    private BaseScreen clickGui;
    private ClientUiScreen clientScreen;
    private ClientUI clientUI;
    private ClientMenu clientMenu;
    private DraggableManager draggableManager;
    public NotificationManager notificationManager;
    public void register() {
        openurl("https://t.me/oldhouse1337");
        log.info("[t.me/OldHouse1337] Starting initialize " + getClientInfo().getName() + " client" + "...");
        String mcUser = Minecraft.getInstance().session.getUsername();
        boolean check = (mcUser.equalsIgnoreCase("1ntave") || mcUser.equalsIgnoreCase("secretname") || mcUser.equalsIgnoreCase("IntaveDev") || mcUser.equalsIgnoreCase("Intave"));
        user = new User(check ? "SecretName" : "HaramDev", "1", "developer");
        eventBus = new EventBus<>();
        moduleManager = new ModuleManager();
        componentManager = new ComponentManager();
        commandManager = new CommandManager();
        configManager = new ConfigManager();
        fileManager = new FileManager();
        friendManager = new FriendManager();
        themeManager = new ThemeManager();
        botManager = new BotManager();
        discord = new Discord();
        clickGui = new BaseScreen();
        clientUI = new ClientUI();
        clientScreen = new ClientUiScreen();
        clientMenu = new ClientMenu();
        draggableManager = new DraggableManager();
        notificationManager = new NotificationManager();
        draggableManager.register();
        clientUI.init();
        moduleManager.init();
        componentManager.init();
        commandManager.init();
        configManager.init();
        fileManager.init();
        friendManager.init();
        discord.init();
        notificationManager.register();
        eventBus.register(this);
        config();
        log.info(getClientInfo().getName() + " initialized.");
    }

       static void openurl(String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                } else {
                    // пошел нахуй
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventLink
    public final Listener<UpdateEvent> onUpdate = event -> {
        Minecraft mc = Minecraft.getInstance();
        if (Aura.target != null || mc.player == null) return;
        mc.player.prevRotationPitchHead = mc.player.prevRotationPitch;
        mc.player.rotationPitchHead = mc.player.rotationPitch;
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (event.getPacket() instanceof CPlayerPacket) {
            CPlayerPacket packet = (CPlayerPacket) event.getPacket();
            PositionSendEvent evt = (new PositionSendEvent(packet.getX(Double.NaN), packet.getY(Double.NaN), packet.getZ(Double.NaN), packet.getYaw(Float.NaN), packet.getPitch(Float.NaN), packet.isOnGround()));
            eventBus.handle(evt);
            if (evt.isCanceled()) {
                event.setCancelled(true);
                return;
            }
            if (Double.isFinite(evt.getX()) && Double.isFinite(evt.getY()) && Double.isFinite(evt.getZ())) {
                if (Float.isFinite(evt.getYaw()) && Float.isFinite(evt.getPitch())) {
                    event.setPacket(new CPlayerPacket.PositionRotationPacket(evt.getX(), evt.getY(), evt.getZ(), evt.getYaw(), evt.getPitch(), evt.isOnGround()));
                } else {
                    event.setPacket(new CPlayerPacket.PositionPacket(evt.getX(), evt.getY(), evt.getZ(), evt.isOnGround()));
                }
            } else if (Float.isFinite(evt.getYaw()) && Float.isFinite(evt.getPitch())) {
                event.setPacket(new CPlayerPacket.RotationPacket(evt.getYaw(), evt.getPitch(), evt.isOnGround()));
            }
        }
    };
    public void config() {
        configManager.update();
        final ConfigFile config = configManager.get("default", true);
        if (config != null) {
            CompletableFuture.runAsync(() -> {
                if (config.read()) configManager.set("default");
            });
        } else CompletableFuture.runAsync(() -> configManager.set("default"));
    }
    public void loadJson(JsonObject json) {
    }
    @java.lang.SuppressWarnings("all")
    public ClientInfo getClientInfo() {
        return this.clientInfo;
    }
    @java.lang.SuppressWarnings("all")
    public User getUser() {
        return this.user;
    }
    @java.lang.SuppressWarnings("all")
    public EventBus<Event> getEventBus() {
        return this.eventBus;
    }
    @java.lang.SuppressWarnings("all")
    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }
    @java.lang.SuppressWarnings("all")
    public ComponentManager getComponentManager() {
        return this.componentManager;
    }
    @java.lang.SuppressWarnings("all")
    public CommandManager getCommandManager() {
        return this.commandManager;
    }
    @java.lang.SuppressWarnings("all")
    public ConfigManager getConfigManager() {
        return this.configManager;
    }
    @java.lang.SuppressWarnings("all")
    public FileManager getFileManager() {
        return this.fileManager;
    }
    @java.lang.SuppressWarnings("all")
    public BotManager getBotManager() {
        return this.botManager;
    }
    @java.lang.SuppressWarnings("all")
    public ThemeManager getThemeManager() {
        return this.themeManager;
    }
    @java.lang.SuppressWarnings("all")
    public Discord getDiscord() {
        return this.discord;
    }
    @java.lang.SuppressWarnings("all")
    public BaseScreen getClickGui() {
        return this.clickGui;
    }
    @java.lang.SuppressWarnings("all")
    public ClientUiScreen getClientScreen() {
        return this.clientScreen;
    }
    @java.lang.SuppressWarnings("all")
    public ClientUI getClientUI() {
        return this.clientUI;
    }
    @java.lang.SuppressWarnings("all")
    public ClientMenu getClientMenu() {
        return this.clientMenu;
    }
    @java.lang.SuppressWarnings("all")
    public DraggableManager getDraggableManager() {
        return this.draggableManager;
    }
    @java.lang.SuppressWarnings("all")
    public NotificationManager getNotificationManager() {
        return this.notificationManager;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<UpdateEvent> getOnUpdate() {
        return this.onUpdate;
    }
    @java.lang.SuppressWarnings("all")
    public Listener<PacketSendEvent> getOnPacketSend() {
        return this.onPacketSend;
    }
    @java.lang.SuppressWarnings("all")
    public static Nova getInstance() {
        return Nova.instance;
    }
    @java.lang.SuppressWarnings("all")
    public static FriendManager getFriendManager() {
        return Nova.friendManager;
    }
}
