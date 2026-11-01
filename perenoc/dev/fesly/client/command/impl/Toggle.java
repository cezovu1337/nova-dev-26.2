package dev.fesly.client.command.impl;

import dev.fesly.Nova;
import dev.fesly.client.command.Command;
import dev.fesly.client.module.api.Module;
import dev.fesly.impl.util.chat.ChatUtil;
import net.minecraft.util.text.TextFormatting;

public final class Toggle extends Command {

    public Toggle() {
        super("При помощи этой комманды вы можете переключить модуль", "toggle", "t");
    }

    @Override
    public void execute(final String[] args) {
        if (args.length == 2) {
            final Module module = Nova.getInstance().getModuleManager().get(args[1].replace(" " , ""));

            if (module == null) {
                ChatUtil.addText("Такого модуля нет в клиенте, или вы неправильно ввели название.");
                return;
            }

            module.toggle();
            ChatUtil.addText("Модуль " + (module.getModuleInfo().name() + " Переключен " + (module.isEnabled() ? TextFormatting.GREEN + "On" : TextFormatting.RED +  "Off")));
        } else {
            error();
        }
    }
}