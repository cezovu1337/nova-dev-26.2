package net.minecraft.client.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;

public class RemoteLocalPlayer extends RemoteClientPlayerEntity {
    public RemoteLocalPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }
}
