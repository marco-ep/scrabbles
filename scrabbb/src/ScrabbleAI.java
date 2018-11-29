/**
 * Player that can choose moves for Scrabble.
 */
public interface ScrabbleAI {

    /**
     * Sets the GateKeeper for the next move. Note that, in a tournament, this may be from a completely new game;
     * each move should be generated independently.
     */
    public void setGateKeeper(GateKeeper gateKeeper);

    /**
     * Returns a good move given the state of the game (accessed through the GateKeeper).
     */
    public ScrabbleMove chooseMove();

}
