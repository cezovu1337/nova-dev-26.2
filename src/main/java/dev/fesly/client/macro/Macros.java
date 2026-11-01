package dev.fesly.client.macro;
import net.minecraft.client.util.InputMappings;
import java.util.Objects;
public class Macros {
    public String name;
    public String button;
    public String command;
    public int keyboardButton;
    public Macros(String name, String button, String command) {
        this.name = name;
        this.button = button;
        this.command = command;
        this.keyboardButton = InputMappings.getInputByName(button).getKeyCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Macros) {
            Macros m = (Macros) obj;
            return m.name.equals(this.name) && m.button.equals(this.button) && m.command.equals(this.command);
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, button, command);
    }
}
