package us.pente.graph.game;

import com.google.common.base.Throwables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GameArchive {
    private static final Pattern GAME_FILE_PATTERN = Pattern.compile("^game/\\d+$");
    private GameArchive() {
    }

    public static Stream<Game> parse(ZipFile zipFile) {
        return zipFile.stream()
                .filter(zipEntry -> GAME_FILE_PATTERN.matcher(zipEntry.getName()).matches())
                .map(zipEntry -> parseGame(zipFile, zipEntry));
    }

    private static Game parseGame(ZipFile zipFile, ZipEntry zipEntry) {
        try (InputStream inputStream = zipFile.getInputStream(zipEntry);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String id = zipEntry.getName().split("/")[1];
            return Game.parse(id, reader.lines());
        } catch (IOException ex) {
            throw Throwables.propagate(ex);
        }
    }
}
