package dev.fesly.client.command.impl;

import dev.fesly.Nova;
import dev.fesly.client.command.Command;
import dev.fesly.impl.config.ConfigFile;
import dev.fesly.impl.config.ConfigManager;
import dev.fesly.impl.util.chat.ChatUtil;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public final class Config extends Command {

    public Config() {
        super("Сохраняет/Загружает Конфиги", "config", "cfg");
    }

    @Override
    public void execute(final String[] args) {
        final ConfigManager configManager = Nova.getInstance().getConfigManager();
        final String command = args[1].toLowerCase();

        switch (args.length) {
            case 3 -> {
                final String name = args[2];
                switch (command) {
                    case "load" -> {
                        configManager.update();
                        final ConfigFile config = configManager.get(name, true);
                        if (config != null) {
                            CompletableFuture.runAsync(() -> {
                                if (config.read()) {
                                    configManager.set("default");
                                    ChatUtil.addText("Конфиг загружен -> " + name);
                                } else {
                                    ChatUtil.addText("Конфиг " + name + " не найден");
                                }
                            });
                        } else {
                            ChatUtil.addText("Конфиг  " + name + " не найден");
                        }
                    }
                    case "save", "create" -> {
                        if (name.equalsIgnoreCase("latest")) {
                            ChatUtil.addText("Конфиг перезагружен");
                            return;
                        }
                        CompletableFuture.runAsync(() -> {
                            configManager.set(name);
                            configManager.set("default");

                            ChatUtil.addText("Конфиг сохранен");
                        });
                    }
                    default -> ChatUtil.addText("");
                }
            }
            case 2 -> {
                switch (command) {
                    case "list" -> {
                        configManager.set("default");
                        ChatUtil.addText("Ваши Конфиги:");
                        configManager.update();
                        configManager.forEach(configFile -> {
                            final String configName = configFile.getFile().getName().replace("." + Nova.getInstance().getClientInfo().getNamespace(), "");
                            final String configCommand = ".config load " + configName;
                            final String color = "color in config.java";

                            final StringTextComponent chatText = new StringTextComponent(color + "> " + configName);
                            final StringTextComponent hoverText = new StringTextComponent(" (hover) " + configName);

                            chatText.setStyle(Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, configCommand)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));

                            mc.ingameGUI.getChatGUI().printChatMessage(chatText);
                        });
                    }
                    case "folder", "dir" -> {
                        try {
                            Desktop desktop = Desktop.getDesktop();
                            File dirToOpen = new File(String.valueOf(ConfigManager.CONFIG_DIRECTORY));
                            desktop.open(dirToOpen);
                            configManager.set("default");
                            ChatUtil.addText("Открыта папка с конфигами.");
                        } catch (IllegalArgumentException | IOException iae) {
                            ChatUtil.addText("Конфиги не найдены");
                        }
                    }
                    default -> {
                        configManager.set("default");
                        ChatUtil.addText("");
                    }
                }
            }
            default -> {
                configManager.set("default");
                ChatUtil.addText("");
            }
        }
    }
}
