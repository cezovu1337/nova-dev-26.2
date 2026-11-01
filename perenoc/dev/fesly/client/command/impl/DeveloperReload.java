package dev.fesly.client.command.impl;

import dev.fesly.Nova;
import dev.fesly.client.command.Command;
import dev.fesly.impl.util.chat.ChatUtil;

public final class DeveloperReload extends Command {

    public DeveloperReload() {
        super("Перезагружает клиент", "developerreload", "dr");
    }

    @Override
    public void execute(final String[] args) {
        if (Nova.DEBUG) {
            Nova.getInstance().register();
            ChatUtil.addText("Reloaded " + Nova.getInstance().getClientInfo().getName());
        }
    }
}