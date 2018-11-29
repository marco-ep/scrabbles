/** Exchanging 0 or more tiles. */
public class ExchangeTiles implements ScrabbleMove {

    /**
     * An array of seven booleans, indicating which tiles in the hand to exchange. Any entries beyond the length of
     * the hand are ignored.
     */
    private final boolean[] tilesToExchange;

    /**
     * @param tilesToExchange An array of seven booleans, indicating which tiles in the hand to exchange. Any entries
     *                        beyond the length of the hand are ignored.
     */
    public ExchangeTiles(boolean[] tilesToExchange) {
        this.tilesToExchange = tilesToExchange;
    }

    @Override
    public Location[] play(Board board, int playerNumber) throws IllegalMoveException {
        board.exchange(board.getHand(playerNumber), tilesToExchange);
        return null;
    }

}
