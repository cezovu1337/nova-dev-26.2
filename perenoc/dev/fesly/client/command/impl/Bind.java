package dev.fesly.client.command.impl;


import dev.fesly.Nova;
import dev.fesly.client.command.Command;
import dev.fesly.client.module.api.Module;
import dev.fesly.impl.util.chat.ChatUtil;

import java.awt.event.KeyEvent;

public final class Bind extends Command {

    public Bind() {
        super("Вы можете забиндить модуль на любую желаемую кнопку", "bind", "keybind");
    }

    @Override
    public void execute(final String[] args) {
        if (args.length == 2) {
            if (args[1].equals("clear")) {
                boolean cleared = false;
                for (Module module : Nova.getInstance().getModuleManager().getAll()) {
                    if (module.getKeyCode() != 0 && !cleared) cleared = true;
                    module.setKeyCode(0);
                }
                ChatUtil.addText(cleared ? "Бинды очищены." : "Ничего не забинджено. Нечего чистить :D");
            }
        }else if (args.length == 3) {
            final Module module = Nova.getInstance().getModuleManager().get(args[1].replace(" " , ""));

            if (module == null) {
                ChatUtil.addText("Такого модуля нет в клиенте, или вы неправильно ввели название.");
                return;
            }

            final String inputCharacter = args[2].toUpperCase();
            if (inputCharacter.equalsIgnoreCase("NONE")) {
                ChatUtil.addText(module.getName() + "'s Бинд очищен");
                module.setKeyCode(0);
                return;
            }
            try {
                module.setKeyCode(KeyEvent.class.getField("VK_" + inputCharacter.toUpperCase()).getInt(null));
                if (KeyEvent.class.getField("VK_" + inputCharacter.toUpperCase()).getInt(null) == 0) {
                    ChatUtil.addText("Введен несуществующий ключ, бинд очищен");
                } else {
                    ChatUtil.addText(module.getName() + " установлен на " + inputCharacter);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        } else {
            error();
        }
    }
}