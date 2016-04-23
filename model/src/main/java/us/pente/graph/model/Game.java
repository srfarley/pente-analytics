package us.pente.graph.model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/*
    [Game "Pente"]
    [Site "Pente.org"]
    [Event "Turn-based Game"]
    [Round "-"]
    [Section "-"]
    [Date "04/08/2016"]
    [Time "01:22:33"]
    [TimeControl "7"]
    [Rated "Y"]
    [Player 1 Name "batman"]
    [Player 2 Name "superman"]
    [Player 1 Rating "1390"]
    [Player 2 Rating "1456"]
    [Player 1 Type "Human"]
    [Player 2 Type "Human"]
    [Result "0-1"]

    1. K10 L10 2. K7 L6 3. L8 J6 4. M9 K6 5. N10 O11 6. M6 H6 7. G6 K8 8. J7 H7 9.
    H5 L7 10. L5 K7 11. K5 J5 12. G8 M5 13. K5 G7 14. F8 K4 15. L3 J7 16. H7 G5 17.
    L7 F9 18. L5 N7 19. J6 L4 0-1
 */
public class Game implements Mappable {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public String id;
    @SerializedName("gameName")
    public String name;
    public String site;
    public String event;
    public String round;
    public String section;
    public LocalDate date;
    public LocalTime time;
    public String timeControl;
    public boolean rated;
    public String player1Name;
    public String player2Name;
    public int player1Rating;
    public int player2Rating;
    public PlayerType player1Type;
    public PlayerType player2Type;
    public String result;
    public List<Move> moves;

    public Game() {
    }

    public static Game parse(String id, Stream<String> lines) {
        Game game = new Game();
        game.id = id;
        AtomicBoolean atMoves = new AtomicBoolean(false);
        List<String> moveLines = new ArrayList<>();
        lines.forEach(line -> {
            if (atMoves.get()) {
                moveLines.add(line);
            } else if (line.trim().length() == 0) {
                atMoves.set(true);
            } else {
                setProperty(game, Property.parse(line));
            }
        });
        game.moves = Move.parse(moveLines);
        return game;
    }

    private static void setProperty(Game game, Property property) {
        if (property != Property.NO_VALUE) {
            switch (property.name) {
                case "Game":
                    game.name = property.value;
                    break;
                case "Site":
                    game.site = property.value;
                    break;
                case "Event":
                    game.event = property.value;
                    break;
                case "Round":
                    game.round = property.value;
                    break;
                case "Section":
                    game.section = property.value;
                    break;
                case "Date":
                    game.date = LocalDate.parse(property.value, DATE_FORMATTER);
                    break;
                case "Time":
                    game.time = LocalTime.parse(property.value);
                    break;
                case "TimeControl":
                    game.timeControl = property.value;
                    break;
                case "Rated":
                    game.rated = property.value.equals("Y");
                    break;
                case "Player 1 Name":
                    game.player1Name = property.value;
                    break;
                case "Player 2 Name":
                    game.player2Name = property.value;
                    break;
                case "Player 1 Rating":
                    game.player1Rating = Integer.parseInt(property.value);
                    break;
                case "Player 2 Rating":
                    game.player2Rating = Integer.parseInt(property.value);
                    break;
                case "Player 1 Type":
                    game.player1Type = PlayerType.valueOf(property.value.toUpperCase());
                    break;
                case "Player 2 Type":
                    game.player2Type = PlayerType.valueOf(property.value.toUpperCase());
                    break;
                case "Result":
                    game.result = property.value;
                    break;
            }
        }
    }

    private static class Property {
        private static final Pattern PATTERN = Pattern.compile("^\\[(.+)\"(.*)\"\\]$");

        static final Property NO_VALUE = new Property();

        String name;
        String value;

        static Property parse(String line) {
            Matcher matcher = PATTERN.matcher(line);
            if (matcher.matches()) {
                Property property = new Property();
                property.name = matcher.group(1).trim();
                property.value = matcher.group(2);
                return property.value.equals("-") || property.value.length() == 0 ? NO_VALUE : property;
            } else {
                throw new RuntimeException("expected a property line, but got " + line);
            }
        }
    }
}
