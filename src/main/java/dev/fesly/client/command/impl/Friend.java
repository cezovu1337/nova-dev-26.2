package dev.fesly.client.command.impl;
import dev.fesly.Nova;
import dev.fesly.client.command.Command;
import dev.fesly.impl.util.chat.ChatUtil;
public class Friend extends Command {
    public Friend() {
        super("Менеджер друзей, можете добавить или убрать их", "friend","friends");
    }
    @Override
    public void execute(String[] args) {
        System.out.println(args.length);
        if (args.length < 2) {
            ChatUtil.addText("Использование:");
            ChatUtil.addText(".friends add <friend name>");
            ChatUtil.addText(".friends add <friend name> <friend name> <friend name>");
            ChatUtil.addText(".friends remove <friend name>");
            ChatUtil.addText(".friends delete <friend name>");
            ChatUtil.addText(".friends list");
            return;
        }
        if (args[1].equalsIgnoreCase("list")) {
            if (Nova.friendManager.isEmpty()) {
                ChatUtil.addText("У вас нет друзей :(");
            }
            else{
                ChatUtil.addText("Ваши друзья/тиммейты: ");
            }
            Nova.friendManager.forEach(friend -> ChatUtil.addText(friend.getName()));
        }
        if (args[1].equalsIgnoreCase("clear")) {
            Nova.friendManager.clear();
            ChatUtil.addText("Теперь у вас нет друзей. Можно и в окно.");
        }
        if (args[1].equalsIgnoreCase("add")) {
            try {
                Nova.friendManager.addFriend(args[2]);
                ChatUtil.addText("Успешно добавлен друг '" + args[2] + "'");
            } catch (Exception ignored) {
            }
        }
        if (args[1].equalsIgnoreCase("add")) {
            try {
                if (args.length > 3) {
                    for (int i = 2 ; i < args.length ; i++) {
                        Nova.friendManager.addFriend(args[i]);
                        ChatUtil.addText("Успешно добавлен друг '" + args[i] + "'");
                    }
                }
            } catch (Exception ignored) {
                ChatUtil.addText("Не удалось добавить друзей! '");
            }
        }
        if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("remove")) {
            try {
                Nova.friendManager.removeFriend(args[2]);
                ChatUtil.addText("Игрок '" + args[2] + " Больше вам не друг'");
            } catch (Exception ignored) {
                ChatUtil.addText("Игрок  '" + args[2] + "' не найден!");
            }
        }
    }
}
