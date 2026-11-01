package dev.fesly.impl.client.screen.music;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
public final class MusicParser {
    public static List<Music> getAllMusicForFolder(String folderPath) {
        List<Music> musicList = new ArrayList<>();
        ClassLoader classLoader = MusicParser.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(folderPath);
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.replace(".mp3", "").split("_");
                    if (parts.length == 3) {
                        String name = parts[0];
                        String time = parts[1];
                        String author = parts[2];
                        musicList.add(new Music(name, time, author));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return musicList;
    }
    @java.lang.SuppressWarnings("all")
    private MusicParser() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
