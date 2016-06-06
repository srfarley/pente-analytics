package us.pente.graph.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GameArchive {
    private static final Pattern GAME_FILE_PATTERN = Pattern.compile("^game/\\d+$");
    private GameArchive() {
    }

    public static Stream<Game> parse(ZipFile zipFile) {
        return zipFile.stream()
                .filter(GameArchive::isGame)
                .map(zipEntry -> parseGame(zipFile, zipEntry));
    }

    public static Stream<Game> parse(InputStream inputStream) {
        Path zipPath = null;
        try {
            zipPath = File.createTempFile(GameArchive.class.getSimpleName(), ".zip").toPath();
            Files.copy(inputStream, zipPath, StandardCopyOption.REPLACE_EXISTING);
            return parse(new ZipFile(zipPath.toFile()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (zipPath != null) {
                zipPath.toFile().delete();
            }
        }
    }

    public static List<Game> toList(Stream<Game> games) {
        return games.collect(Collectors.toList());
    }

    private static Game parseGame(ZipFile zipFile, ZipEntry zipEntry) {
        try (InputStream inputStream = zipFile.getInputStream(zipEntry);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return parseGame(zipEntry, reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Game parseGame(ZipEntry zipEntry, BufferedReader reader) {
        String id = zipEntry.getName().split("/")[1];
        return Game.parse(id, reader.lines());
    }

    private static boolean isGame(ZipEntry zipEntry) {
        return GAME_FILE_PATTERN.matcher(zipEntry.getName()).matches();
    }
}
