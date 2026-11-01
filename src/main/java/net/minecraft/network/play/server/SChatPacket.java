package net.minecraft.network.play.server;

import net.minecraft.network.IPacket;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SChatPacket implements IPacket {
    private ITextComponent chatComponent = new StringTextComponent("");

    public ITextComponent getChatComponent() {
        return chatComponent;
    }

    @Override
    public PacketType type() {
        return null;
    }

    @Override
    public void handle(net.minecraft.network.PacketListener listener) {
    }
}
