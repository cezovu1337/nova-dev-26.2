package dev.fesly.impl.config;
import dev.fesly.Nova;
import dev.fesly.impl.util.file.FileManager;
import dev.fesly.impl.util.file.FileType;
import java.io.File;
import java.util.ArrayList;
public class ConfigManager extends ArrayList<ConfigFile> {
    public static final File CONFIG_DIRECTORY = new File(FileManager.DIRECTORY, "configs");
    public void init() {
        if (!CONFIG_DIRECTORY.exists()) {
            CONFIG_DIRECTORY.mkdir();
        }
        this.update();
    }
    public ConfigFile get(final String config, final boolean allowKey) {
        final File file = new File(ConfigManager.CONFIG_DIRECTORY, config + "." + Nova.getInstance().getClientInfo().getNamespace());
        final ConfigFile configFile = new ConfigFile(file, FileType.CONFIG);
        if (allowKey) configFile.allowKeyCodeLoading();
        return configFile;
    }
    public ConfigFile get(final String config) {
        final File file = new File(ConfigManager.CONFIG_DIRECTORY, config + "." + Nova.getInstance().getClientInfo().getNamespace());
        final ConfigFile configFile = new ConfigFile(file, FileType.CONFIG);
        configFile.allowKeyCodeLoading();
        return configFile;
    }
    public void set(final String config) {
        final File file = new File(CONFIG_DIRECTORY, config + "." + Nova.getInstance().getClientInfo().getNamespace());
        ConfigFile configFile = get(config);
        if (configFile == null) {
            configFile = new ConfigFile(file, FileType.CONFIG);
            add(configFile);
            System.out.println("Creating new config...");
        }
        configFile.write();
    }
    public boolean update() {
        clear();
        final File[] files = CONFIG_DIRECTORY.listFiles();
        if (files == null)
            return false;
        for (final File file : files) {
            if (file.getName().endsWith("." + Nova.getInstance().getClientInfo().getNamespace())) {
                add(new ConfigFile(file, FileType.CONFIG));
            }
        }
        return true;
    }
    public boolean delete(final String config) {
        final ConfigFile configFile = get(config);
        if (configFile == null)
            return false;
        remove(configFile);
        return configFile.getFile().delete();
    }
}
