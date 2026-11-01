package dev.fesly.client.command.impl;
import dev.fesly.Nova;
import dev.fesly.client.command.Command;
import dev.fesly.impl.util.chat.ChatUtil;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
public final class Help extends Command {
    public Help() {
        super("Отправляет все комманды", "help", "?");
    }
    @Override
    public void execute(final String[] args) {
        Nova.getInstance().getCommandManager()
                .forEach(command -> ChatUtil.addText(StringUtils.capitalize(command.getExpressions()[0]) + " " + Arrays.toString(command.getExpressions()) + " \2478» \2477" + command.getDescription()));
    }
}
