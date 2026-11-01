package dev.fesly.impl.util.file;
import dev.fesly.Nova;
import dev.fesly.impl.interfaces.game.IMinecraft;
import java.io.File;
public class FileManager implements IMinecraft {
    public static final File DIRECTORY = new File(mc.gameDir, Nova.getInstance().getClientInfo().getNamespace());
    public void init() {
        if (!DIRECTORY.exists()) {
            DIRECTORY.mkdir();
        }
    }
}
