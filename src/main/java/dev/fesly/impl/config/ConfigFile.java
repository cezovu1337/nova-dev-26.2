package dev.fesly.impl.config;
import com.google.gson.JsonObject;
import dev.fesly.Nova;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleManager;
import dev.fesly.client.module.impl.render.ClickGUI;
import dev.fesly.client.notification.NotificationManager;
import dev.fesly.client.theme.Themes;
import dev.fesly.impl.util.file.FileType;
import dev.fesly.impl.value.defaultValues.Value;
import dev.fesly.impl.value.defaultValues.impl.*;
import org.joml.Vector2d;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
public class ConfigFile extends dev.fesly.impl.util.file.File {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
    private boolean loadKeyCodes;
    public ConfigFile(final File file, final FileType fileType) {
        super(file, fileType);
    }
    @Override
    public boolean read() {
        if (!this.getFile().exists()) {
            return false;
        }
        try {
            final FileReader fileReader = new FileReader(getFile());
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            final JsonObject jsonObject = GSON.fromJson(bufferedReader, JsonObject.class);
            bufferedReader.close();
            fileReader.close();
            if (jsonObject == null) {
                return false;
            }
            if (jsonObject.has("theme")) {
                Optional<Themes> theme = Arrays.stream(Themes.values())
                        .filter(x -> x.name().equalsIgnoreCase(jsonObject.get("theme").getAsString()))
                        .findFirst();
                theme.ifPresent(configTheme -> Nova.getInstance().getThemeManager().setTheme(configTheme));
            }
            Nova.getInstance().getDraggableManager().load(jsonObject.getAsJsonObject("draggables"));
            Nova.getInstance().getModuleManager().load(jsonObject.getAsJsonObject("modules"));
            Nova.getInstance().getClientUI().load(jsonObject.getAsJsonObject("clientUi"));
        } catch (final IOException ignored) {
            return false;
        }
        return true;
    }
    @Override
    public boolean write() {
        try {
            if (!getFile().exists())
                this.getFile().createNewFile();
            final JsonObject jsonObject = new JsonObject();
            final JsonObject metadataJsonObject = new JsonObject();
            metadataJsonObject.addProperty("version", Math.random());
            metadataJsonObject.addProperty("creationDate", DATE_FORMATTER.format(new Date()));
            jsonObject.add("Metadata", metadataJsonObject);
            jsonObject.add("draggables", Nova.getInstance().getDraggableManager().save());
            jsonObject.add("clientUi", Nova.getInstance().getClientUI().save());
            jsonObject.add("modules", Nova.getInstance().getModuleManager().save());
            jsonObject.addProperty("theme", Nova.getInstance().getThemeManager().getTheme().name());
            final FileWriter fileWriter = new FileWriter(getFile());
            final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            GSON.toJson(jsonObject, bufferedWriter);
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.flush();
            fileWriter.close();
        } catch (final IOException exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }
    public void allowKeyCodeLoading() {
        this.loadKeyCodes = true;
    }
}
