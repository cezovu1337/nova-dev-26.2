package dev.fesly.impl.util.file;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
public abstract class File {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final java.io.File file;
    private final FileType fileType;
    public abstract boolean read();
    public abstract boolean write();
    @java.lang.SuppressWarnings("all")
    public java.io.File getFile() {
        return this.file;
    }
    @java.lang.SuppressWarnings("all")
    public FileType getFileType() {
        return this.fileType;
    }
    @java.lang.SuppressWarnings("all")
    public File(final java.io.File file, final FileType fileType) {
        this.file = file;
        this.fileType = fileType;
    }
}
