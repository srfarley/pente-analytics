package us.pente.graph.model;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GameArchiveTest {
    @Test
    public void parseShouldReturnAllGamesInFile() throws Exception {
        List<Game> games;
        try (ZipFile zipFile = new ZipFile(zipFile())) {
            games = GameArchive.parse(zipFile).collect(Collectors.toList());
        }
        assertExpectedGames(games);
    }

    @Test
    public void parseShouldReturnAllGamesInInputStream() throws Exception {
        List<Game> games;
        try (InputStream inputStream = new FileInputStream(zipFile())) {
            games = GameArchive.parse(inputStream).collect(Collectors.toList());
        }
        assertExpectedGames(games);
    }

    private File zipFile() throws URISyntaxException {
        return new File(getClass().getResource("/games.zip").toURI());
    }

    private void assertExpectedGames(List<Game> games) {
        assertThat(games.size(), is(3));
        assertThat(games.get(0).id, is("50000000105385"));
        assertThat(games.get(0).player1Name, is("batman"));
        assertThat(games.get(1).id, is("50000000109107"));
        assertThat(games.get(1).player1Name, is("luke"));
        assertThat(games.get(2).id, is("50000000109878"));
        assertThat(games.get(2).player1Name, is("woody"));
    }
}
