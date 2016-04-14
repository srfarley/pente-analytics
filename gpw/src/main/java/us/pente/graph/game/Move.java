package us.pente.graph.game;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Move {
    private static final Pattern PATTERN = Pattern.compile("(\\d+?)\\.\\s+(.+?)\\s+(.+?)\\s+", Pattern.MULTILINE);

    public int number;
    public String player1;
    public String player2;

    public static List<Move> parse(List<String> lines) {
        String movesLine = String.join(" ", lines);
        List<Move> moves = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(movesLine);
        while (matcher.find()) {
            Move move = new Move();
            move.number = Integer.parseInt(matcher.group(1));
            move.player1 = matcher.group(2);
            move.player2 = matcher.group(3);
            moves.add(move);
        }
        return moves;
    }
}
