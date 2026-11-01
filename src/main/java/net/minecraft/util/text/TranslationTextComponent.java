package net.minecraft.util.text;

import java.util.HashMap;
import java.util.Map;

public class TranslationTextComponent implements ITextComponent {
    private static final Map<String, String> TRANSLATIONS = new HashMap<>();

    static {
        TRANSLATIONS.put("menu.singleplayer", "Одиночная игра");
        TRANSLATIONS.put("menu.multiplayer", "Сетевая игра");
        TRANSLATIONS.put("menu.options", "Настройки");
        TRANSLATIONS.put("menu.quit", "Выйти");
        TRANSLATIONS.put("menu.altmanager", "Аккаунты");
        TRANSLATIONS.put("menu.online", "Realms");
        TRANSLATIONS.put("menu.playdemo", "Играть в демо");
        TRANSLATIONS.put("menu.resetdemo", "Сбросить демо-мир");
        TRANSLATIONS.put("title.multiplayer.disabled", "Сетевая игра недоступна");
        TRANSLATIONS.put("title.credits", "Copyright Mojang AB. Не распространять!");
        TRANSLATIONS.put("narrator.screen.title", "Главное меню");
        TRANSLATIONS.put("selectWorld.deleteQuestion", "Вы действительно хотите удалить этот мир?");
        TRANSLATIONS.put("selectWorld.deleteWarning", "Мир \"%s\" будет удалён навсегда!");
        TRANSLATIONS.put("selectWorld.deleteButton", "Удалить");
        TRANSLATIONS.put("gui.cancel", "Отмена");
        TRANSLATIONS.put("gui.done", "Готово");
        TRANSLATIONS.put("gui.back", "Назад");
    }

    private final String key;
    private final Object[] args;

    public TranslationTextComponent(String key, Object... args) {
        this.key = key;
        this.args = args;
    }

    @Override
    public String getString() {
        String text = TRANSLATIONS.getOrDefault(key, key);
        return args != null && args.length > 0 ? String.format(text, args) : text;
    }

    @Override
    public String toString() {
        return getString();
    }
}
