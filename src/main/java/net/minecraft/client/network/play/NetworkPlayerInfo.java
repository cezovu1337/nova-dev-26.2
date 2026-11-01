package net.minecraft.client.network.play;

import com.mojang.authlib.GameProfile;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.level.GameType;

public class NetworkPlayerInfo {
    protected GameProfile gameProfile;
    protected GameType gameType = GameType.SURVIVAL;
    protected ResourceLocation skin;

    public NetworkPlayerInfo() {}

    public NetworkPlayerInfo(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public net.minecraft.util.text.ITextComponent getDisplayName() {
        return new net.minecraft.util.text.StringTextComponent(this.gameProfile != null ? this.gameProfile.name() : "");
    }

    public ScorePlayerTeam getPlayerTeam() {
        return null;
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public ResourceLocation getLocationSkin() {
        return this.skin != null ? this.skin : new ResourceLocation("textures/entity/steve.png");
    }
}
