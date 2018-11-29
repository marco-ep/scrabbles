/** A tournament between ScrabbleAIs. Edit the constructor to change the contestants. */
public class ScrabbleTournament {

    /** Contestants. */
    private ScrabbleAI[] players;

    public ScrabbleTournament() {
        // List contestants here
        players = new ScrabbleAI[] {
          new Incrementalist(),
          new Incrementalist()
        };
    }

    public static void main(String[] args) throws IllegalMoveException {
        new ScrabbleTournament().run();
    }

    /**
     * Plays two games between each pair of contestants, one with each going first. Prints the number of wins for
     * each contestant (including 0.5 wins for each tie).
     */
    public void run() throws IllegalMoveException {
        double[] scores = new double[players.length];
        for (int i = 0; i < players.length; i++) {
            for (int j = 0; j < players.length; j++) {
                if (i != j) {
                    double[] result1 = playGame(players[i], players[j]);
                    double[] result2 = playGame(players[j], players[i]);
                    scores[i] += result1[0] + result2[1];
                    scores[j] += result1[1] + result2[0];
                }
            }
        }
        for (int i = 0; i < players.length; i++) {
            StdOut.println(players[i].toString() + ": " + scores[i]);
        }
    }

    /**
     * Plays a game between a (going first) and b. Returns their tournament scores, either {1, 0} (if a wins),
     * {0, 1}, or {0.5, 0.5}.
     */
    public double[] playGame(ScrabbleAI a, ScrabbleAI b) throws IllegalMoveException {
        StdOut.println(a + " vs " + b + ":");
        Board board = new Board();
        a.setGateKeeper(new GateKeeper(board, 0));
        b.setGateKeeper(new GateKeeper(board, 1));
        while (!board.gameIsOver()) {
            playMove(board, a, 0);
            if (!board.gameIsOver()) {
                playMove(board, b, 1);
            }
        }
        int s0 = board.getScore(0);
        int s1 = board.getScore(1);
        StdOut.print(board);
        StdOut.println("Final score: " + a + " " + s0 + ", b " + s1);
        StdOut.println();
        if (s0 > s1) {
            return new double[] {1, 0};
        } else if (s0 < s1) {
            return new double[] {0, 1};
        }
        // Tie -- half credit to each player.
        return new double[] {0.5, 0.5};
    }

    /**
     * Asks player for a move and plays it on board.
     * @param playerNumber Player's place in the game turn order (0 or 1).
     */
    public void playMove(Board board, ScrabbleAI player, int playerNumber) throws IllegalMoveException {
        player.chooseMove().play(board, playerNumber);
    }

}
