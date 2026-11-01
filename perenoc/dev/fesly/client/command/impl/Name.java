package dev.fesly.client.command.impl;

import dev.fesly.client.command.Command;
import dev.fesly.impl.client.screen.mainmenu.AltManager;
import dev.fesly.impl.util.chat.ChatUtil;

public final class Name extends Command {

    public Name() {
        super("Копирует ник в буфер обмена", "name", "ign", "username", "nick", "nickname");
    }

    @Override
    public void execute(final String[] args) {
        if (args.length == 1) {
            final String name = mc.player.getNameClear();

            mc.keyboardListener.setClipboardString(name);
            ChatUtil.addText("Ник скопирован (" + name +")");
        } else {
            error();
        }
    }
}
