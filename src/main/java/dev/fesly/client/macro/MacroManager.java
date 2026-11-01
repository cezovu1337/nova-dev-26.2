package dev.fesly.client.macro;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
public class MacroManager {
    public List<Macros> macroList = new ArrayList<>();
    public void addMacro(Macros macro) {
        macroList.add(macro);
    }
    public JsonObject save() {
        JsonObject json = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (Macros macro : macroList) {
            JsonObject macroJson = new JsonObject();
            macroJson.addProperty("name", macro.name);
            macroJson.addProperty("button", macro.button);
            macroJson.addProperty("command", macro.command);
            jsonArray.add(macroJson);
        }
        json.add("macroList", jsonArray);
        return json;
    }
    public void load(JsonObject json) {
        try {
            macroList.clear();
            JsonArray jsonArray = json.getAsJsonArray("macroList");
            for (JsonElement element : jsonArray) {
                JsonObject macroJson = element.getAsJsonObject();
                Macros macro = new Macros(macroJson.get("name").getAsString(),
                        macroJson.get("button").getAsString(),
                        macroJson.get("command").getAsString());
                macroList.add(macro);
            }
        } catch (Exception ignored) {
        }
    }
}
