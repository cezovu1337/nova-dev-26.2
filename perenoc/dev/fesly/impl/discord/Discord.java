package dev.fesly.impl.discord;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import dev.fesly.Nova;

public class Discord {

    private static final String APPLICATION_ID = "1111141091345969262";
    private static final DiscordRichPresence PRESENCE = new DiscordRichPresence();
    private static final DiscordRPC INSTANCE = DiscordRPC.INSTANCE;

    public void init() {
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) -> System.out.println("Discord Rich Presence initialized.");
        INSTANCE.Discord_Initialize(APPLICATION_ID, handlers, true, null);
        PRESENCE.startTimestamp = (System.currentTimeMillis() / 1000); // epoch second
        PRESENCE.details = "ID: " + Nova.getInstance().getUser().uid();
        PRESENCE.largeImageKey = Nova.getInstance().getUser().getAvatar();
        PRESENCE.largeImageText = "Build: " + Nova.getInstance().getClientInfo().getBuild();
        INSTANCE.Discord_UpdatePresence(PRESENCE);
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                INSTANCE.Discord_RunCallbacks();
            }
        }, "Discord-RPC").start();

    }

    public static void stopRPC() {
        INSTANCE.Discord_Shutdown();
    }

}