package dev.fesly.client.command.impl;

import dev.fesly.client.command.Command;
import dev.fesly.impl.util.chat.ChatUtil;

/**
 * Code by Intave
 **/

public final class Login extends Command {

    public Login() {
        super("Вы можете залогинится с любого аккаунта в любое время", "login");
    }

    @Override
    public void execute(final String[] args) {
        if (args.length == 2) {
            mc.session = new net.minecraft.util.Session(args[1], "", "", "mojang");
            ChatUtil.addText("Login with cracked account - &7[&c&l" + args[1] + "&7]");
        } else {
            error();
        }
    }
}