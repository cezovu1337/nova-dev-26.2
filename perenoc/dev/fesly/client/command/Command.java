package dev.fesly.client.command;

import dev.fesly.impl.interfaces.game.IMinecraft;
import dev.fesly.impl.util.chat.ChatUtil;
import lombok.Getter;

@Getter
public abstract class Command implements IMinecraft {

    private final String description;
    private final String[] expressions;

    public Command(final String description, final String... expressions) {
        this.description = description;
        this.expressions = expressions;
    }

    public abstract void execute(String[] args);

    protected final void error() {
        ChatUtil.addText("Неправильные аргументы в комманде..");
    }

    protected final void error(String usage) {
        ChatUtil.addText("Неправильные аргументы в комманде. " + usage);
    }
}