package dev.fesly.client.command.impl;
import dev.fesly.Nova;
import dev.fesly.client.command.Command;
public final class Panic extends Command {
    public Panic() {
        super("Выключает все модули", "panic", "p");
    }
    @Override
    public void execute(final String[] args) {
        Nova.getInstance().getModuleManager().getAll().stream().filter(module ->
                !module.getModuleInfo().autoEnabled()).forEach(module -> module.setEnabled(false));
    }
}
