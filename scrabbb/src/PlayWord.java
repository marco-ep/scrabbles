/** Playing one or more tiles on the board. */
public class PlayWord implements ScrabbleMove {

    /**
     * The word to be played.
     *
     * @see Board
     */
    private final String word;

    /** The location of the first tile in the word (new or already on the board. */
    private final Location location;

    /** The direction in which this word is to be played: Location.HORIZONTAL or Location.VERTICAL. */
    private final Location direction;

    public PlayWord(String word, Location location, Location direction) {
        this.word = word;
        this.location = location;
        this.direction = direction;
    }

    @Override
    public Location[] play(Board board, int playerNumber) throws IllegalMoveException {
        board.play(word, location, direction, board.getHand(playerNumber));
        return new Location[] {location, direction};
    }

}
