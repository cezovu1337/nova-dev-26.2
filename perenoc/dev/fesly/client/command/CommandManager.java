package dev.fesly.client.command;

import dev.fesly.Nova;
import dev.fesly.client.command.impl.*;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.input.ChatInputEvent;
import dev.fesly.impl.util.chat.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CommandManager extends ArrayList<Command> {

    /**
     * Called on client start
     */
    public void init() {
        add(new Bind());
        add(new Friend());
        add(new Clip());
        add(new Config());
        add(new DeveloperReload());
        add(new Help());
        add(new Login());
        add(new Name());
        add(new Panic());
        add(new Toggle());

        Nova.getInstance().getEventBus().register(this);
    }

    public <T extends Command> T get(final String name) {
        return (T) this.stream()
                .filter(command -> Arrays.stream(command.getExpressions())
                        .anyMatch(expression -> expression.equalsIgnoreCase(name))
                ).findAny().orElse(null);
    }

    public <T extends Command> T get(final Class<? extends Command> clazz) {
        return (T) this.stream()
                .filter(command -> command.getClass() == clazz)
                .findAny().orElse(null);
    }

    @EventLink()
    public final Listener<ChatInputEvent> onChatInput = event -> {

        String message = event.getMessage();

        if (!message.startsWith(".")) return;

        message = message.substring(1);
        final String[] args = message.split(" ");

        final AtomicBoolean commandFound = new AtomicBoolean(false);

        try {
            this.stream()
                    .filter(command ->
                            Arrays.stream(command.getExpressions())
                                    .anyMatch(expression ->
                                            expression.equalsIgnoreCase(args[0])))
                    .forEach(command -> {
                        commandFound.set(true);
                        command.execute(args);
                    });
        } catch (final Exception ignored) {
        }

        if (!commandFound.get()) {
            ChatUtil.addText("Unknown command! Try .help if you're lost");
        }

        event.setCancelled(true);
    };
}