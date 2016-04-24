package us.pente.graph.model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class GameTest {
    private static final String GAME_FILE =
            "[Game \"Pente\"]\n" +
            "[Site \"Pente.org\"]\n" +
            "[Event \"Turn-based Game\"]\n" +
            "[Round \"2\"]\n" +
            "[Section \"3\"]\n" +
            "[Date \"04/08/2016\"]\n" +
            "[Time \"01:22:33\"]\n" +
            "[TimeControl \"7\"]\n" +
            "[Rated \"Y\"]\n" +
            "[Player 1 Name \"batman\"]\n" +
            "[Player 2 Name \"superman\"]\n" +
            "[Player 1 Rating \"1390\"]\n" +
            "[Player 2 Rating \"1456\"]\n" +
            "[Player 1 Type \"Human\"]\n" +
            "[Player 2 Type \"Computer\"]\n" +
            "[Result \"0-1\"]\n" +
            "\n" +
            "1. K10 L10 2. K7 L6 3. L8 J6 4. M9 K6 5. N10 O11 6. M6 H6 7. G6 K8 8. J7 H7 9.\n" +
            "H5 L7 10. L5 K7 11. K5 J5 12. G8 M5 13. K5 G7 14. F8 K4 15. L3 J7 16. H7 G5 17.\n" +
            "L7 F9 18. L5 N7 19. J6 L4 0-1\n";
    private static final String GAME_ID = "ID";
    private Stream<String> lines;

    @Before
    public void before() throws Exception {
        lines = Arrays.stream(GAME_FILE.split("\\n"));
    }

    @Test
    public void parseShouldReturnGameWithAllValuesPopulated() {
        Game game = Game.parse(GAME_ID, lines);

        assertThat(game.name, is("Pente"));
        assertThat(game.site, is("Pente.org"));
        assertThat(game.event, is("Turn-based Game"));
        assertThat(game.round, is("2"));
        assertThat(game.section, is("3"));
        assertThat(game.date, is(LocalDate.of(2016, 4, 8)));
        assertThat(game.time, is(LocalTime.of(1, 22, 33)));
        assertThat(game.timeControl, is("7"));
        assertThat(game.rated, is(true));
        assertThat(game.player1Name, is("batman"));
        assertThat(game.player2Name, is("superman"));
        assertThat(game.player1Rating, is(1390));
        assertThat(game.player2Rating, is(1456));
        assertThat(game.player1Type, is(PlayerType.HUMAN));
        assertThat(game.player2Type, is(PlayerType.COMPUTER));
        assertThat(game.result, is("0-1"));
        assertThat(game.winner, is("superman"));
        assertThat(game.loser, is("batman"));
        List<Move> moves = game.moves;
        assertThat(moves.size(), is(19));
        Move move1 = moves.get(0);
        assertThat(move1.number, is(1));
        assertThat(move1.player1, is("K10"));
        assertThat(move1.player2, is("L10"));
        Move move19 = moves.get(18);
        assertThat(move19.player1, is("J6"));
        assertThat(move19.player2, is("L4"));
    }

    @Test
    public void isWinnerAndisLoserShouldReturnCorrectResults() {
        Game game = Game.parse(GAME_ID, lines);
        assertThat(game.isWinner("superman"), is(true));
        assertThat(game.isWinner("batman"), is(false));
        assertThat(game.isLoser("superman"), is(false));
        assertThat(game.isLoser("batman"), is(true));
    }

    @Test
    public void parseShouldSetEmptyOrDashPropertiesToNull() {
        Stream<String> lines2 = lines
                .map(line -> line.startsWith("[Round") ? "[Round \"-\"]" : line)
                .map(line -> line.startsWith("[Section") ? "[Section \"\"]" : line);

        Game game = Game.parse(GAME_ID, lines2);

        assertThat(game.round, nullValue());
        assertThat(game.section, nullValue());
    }

    @Test
    public void toMapShouldReturnMapOfGame() {
        Game game = Game.parse(GAME_ID, lines);

        Map<String, Object> map = game.toMap();

        assertThat(map.size(), is(20));
        assertThat(map.get("gameName"), is("Pente"));
        assertThat(map.get("player1Name"), is("batman"));
        assertThat(map.get("player2Name"), is("superman"));
        assertThat(map.get("winner"), is("superman"));
        assertThat(map.get("loser"), is("batman"));
    }
}
