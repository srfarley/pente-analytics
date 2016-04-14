package us.pente.graph.game;

import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GameArchiveTest {
    @Test
    public void parseShouldReturnAllGamesInZipArchive() throws Exception {
        URI zipUri = getClass().getResource("/games.zip").toURI();
        ZipFile zipFile = new ZipFile(new File(zipUri));
        List<Game>  games = GameArchive.parse(zipFile).collect(Collectors.toList());
        zipFile.close();

        assertThat(games.size(), is(3));
        assertThat(games.get(0).id, is ("50000000105385"));
        assertThat(games.get(0).player1Name, is("batman"));
        assertThat(games.get(1).id, is ("50000000109107"));
        assertThat(games.get(1).player1Name, is("luke"));
        assertThat(games.get(2).id, is ("50000000109878"));
        assertThat(games.get(2).player1Name, is("woody"));
    }
}
