import java.awt.*;
import static java.awt.event.KeyEvent.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/** GUI allowing a human to play against a ScrabbleAI. To change the AI, edit the constructor. */
public class Scrabble {

    private static final Map<Character, Color> COLORS = new HashMap<>();

    private static final Font LETTER_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 20);

    private static final Font VALUE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

    private static final Font INTERFACE_FONT = new Font(Font.SERIF, Font.PLAIN, 18);

    private static final Font TYPING_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 18);

    private static final Color TABLE_COLOR = new Color(24, 64, 35);

    private static final Color TILE_COLOR = new Color(251, 224, 174);

    /** Keys that the user might press. */
    private static final List<Integer> KEYS = new ArrayList<>();

    /** This Scrabble is always in exactly one of these modes. */
    private enum Mode {
        BOARD, // Waiting for user to play a word on the board
        HAND, // Waiting for user to select tiles (if any) to exchange
        ILLEGAL_MOVE, // Waiting for user to acknowledge an illegal move
        AI_PLAYING, // Waiting for AI to play
        GAME_OVER} // Game over

    // A static block like this is called once when the class is loaded. It is useful for initializing complex
    // static fields.
    static {
        // Colors extracted from https://www.vecteezy.com/vector-art/90549-scrabble-board-free-vector
        COLORS.put(Board.NO_PREMIUM, new Color(226, 206, 177));
        COLORS.put(Board.DOUBLE_LETTER_SCORE, new Color(185, 202, 206));
        COLORS.put(Board.TRIPLE_LETTER_SCORE, new Color(67, 162, 198));
        COLORS.put(Board.DOUBLE_WORD_SCORE, new Color(205, 176, 180));
        COLORS.put(Board.TRIPLE_WORD_SCORE, new Color(200, 130, 142));
        for (char c = 'a'; c <= 'z'; c++) {
            COLORS.put(c, TILE_COLOR);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            COLORS.put(c, TILE_COLOR);
        }
        COLORS.put('_', TILE_COLOR);
        // Relevant keys
        for (char c = 'a'; c <= 'z'; c++) {
            KEYS.add((int)c);
        }
        KEYS.add(VK_SLASH);
        KEYS.add(VK_LEFT);
        KEYS.add(VK_RIGHT);
        KEYS.add(VK_UP);
        KEYS.add(VK_DOWN);
        for (char c = 'A'; c <= 'Z'; c++) {
            KEYS.add((int)c);
        }
        KEYS.add(VK_SPACE);
        KEYS.add(VK_BACK_SPACE);
        KEYS.add(VK_ENTER);
        KEYS.add(VK_ALT);
    }

    /** The logical game mode. */
    private Board board;

    /** Current GUI mode. */
    private Mode mode;

    /** Location of the cursor on the board. */
    private Location boardCursor;

    /** Direction (Location.HORIZONTAL or Location.VERTICAL) of cursor on the board. */
    private Location boardCursorDirection;

    /**
     * The word currently being constructed.
     *
     * @see Board
     */
    private String wordBeingConstructed;

    /** Location of the cursor in the user's hand, for selecting tiles to discard. */
    private int handCursor;

    /** Tiles marked for discarding. */
    private boolean[] tilesToDiscard;

    /** Opponent. */
    private ScrabbleAI ai;

    public Scrabble() {
        board = new Board();
        ai = new Incrementalist(); // Opponent
        ai.setGateKeeper(new GateKeeper(board, 0));
        mode = Mode.AI_PLAYING;
    }

    public static void main(String[] args) throws IllegalMoveException {
        new Scrabble().run();
    }

    /** Runs the game. Crashes if the AI opponent plays an illegal move. */
    private void run() throws IllegalMoveException {
        StdDraw.setCanvasSize(805, 525);
        StdDraw.setXscale(-1.5, 23.5);
        StdDraw.setYscale(-1.5, 15.5);
        StdDraw.enableDoubleBuffering();
        boardCursor = Location.CENTER;
        boardCursorDirection = Location.HORIZONTAL;
        draw();
        while (mode != Mode.GAME_OVER) {
            if (mode == Mode.AI_PLAYING) {
                draw();
                Location[] place = ai.chooseMove().play(board, 0);
                if (place != null) {
                    boardCursor = place[0];
                    boardCursorDirection = place[1];
                }
                if (board.gameIsOver()) {
                    mode = Mode.GAME_OVER;
                } else {
                    enterBoardMode();
                }
                draw();
            } else {
                handleKeyPress();
                draw();
            }
        }
    }

    /** Prepare for the user to select tiles (if any) to exchange. */
    private void enterHandMode() {
        mode = Mode.HAND;
        handCursor = 0;
        tilesToDiscard = new boolean[7];
    }

    /** Prepare for the user to play a word on the board. */
    private void enterBoardMode() {
        mode = Mode.BOARD;
        wordBeingConstructed = "";
    }

    /**
     * Handles a key pressed by the user.
     */
    private void handleKeyPress() {
        int c = getKeyPressed();
        if (mode == Mode.BOARD) {
            // Toggle board cursor direction
            if (c == '/') {
                boardCursorDirection = boardCursorDirection.opposite();
            }
            // Move board cursor
            Location next = null;
            if (c == VK_LEFT) {
                next = boardCursor.antineighbor(Location.HORIZONTAL);
            } else if (c == VK_RIGHT) {
                next = boardCursor.neighbor(Location.HORIZONTAL);
            } else if (c == VK_UP) {
                next = boardCursor.antineighbor(Location.VERTICAL);
            } else if (c == VK_DOWN) {
                next = boardCursor.neighbor(Location.VERTICAL);
            }
            if (next != null && next.isOnBoard()) {
                boardCursor = next;
            }
            // Type in word to be played
            if (isLetterOrSpace(c)) {
                wordBeingConstructed += (char) c;
            }
            if (c == VK_BACK_SPACE && !wordBeingConstructed.isEmpty()) {
                wordBeingConstructed = wordBeingConstructed.substring(0, wordBeingConstructed.length() - 1);
            }
            // Play word
            if (c == VK_ENTER) {
                try {
                    board.play(wordBeingConstructed, boardCursor, boardCursorDirection, board.getHand(board.getCurrentPlayer()));
                    if (board.gameIsOver()) {
                        mode = Mode.GAME_OVER;
                    } else {
                        mode = Mode.AI_PLAYING;
                    }
                } catch (IllegalMoveException e) {
                    mode = Mode.ILLEGAL_MOVE;
                }
            }
            // Switch to hand mode
            if (c == VK_ALT) {
                enterHandMode();
            }
        } else if (mode == Mode.HAND) {
            // Switch to board mode
            if (c == VK_ALT) {
                enterBoardMode();
            }
            // Move hand cursor
            if (c == VK_LEFT) {
                handCursor--;
                if (handCursor < 0) {
                    handCursor++;
                }
            } else if (c == VK_RIGHT) {
                handCursor++;
                if (handCursor >= board.getHand(1).size()) {
                    handCursor--;
                }
            }
            // Toggle letter
            if (c == VK_SPACE) {
                tilesToDiscard[handCursor] = !tilesToDiscard[handCursor];
            }
            // Exchange/pass
            if (c == VK_ENTER) {
                board.exchange(board.getHand(1), tilesToDiscard);
                if (board.gameIsOver()) {
                    mode = Mode.GAME_OVER;
                } else {
                    mode = Mode.AI_PLAYING;
                }
            }
        } else if (mode == Mode.ILLEGAL_MOVE) {
            if (c == VK_ENTER) {
                enterBoardMode();
            }
        }
    }

    /** Returns true if key is a letter or a space. */
    private boolean isLetterOrSpace(int key) {
        return (key >= 'a' && key <= 'z') || (key >= 'A' && key <= 'Z') || (key == VK_SPACE);
    }

    /** Returns the key that the user pressed. Shift modifies letter keys in the usual way. */
    private int getKeyPressed() {
        while (true) {
            for (int key : KEYS) {
                if (StdDraw.isKeyPressed(key)) {
                    int result = key;
                    if (key >= 'A' && key <= 'Z') {
                        if (!StdDraw.isKeyPressed(VK_SHIFT)) {
                            result = Character.toLowerCase(key);
                        }
                    }
                    while (StdDraw.isKeyPressed(key)) {
                        // Wait for key to be released
                    }
                    return result;
                }
            }
        }
    }

    /** Draws the current state of the game, including instructions. */
    private void draw() {
        StdDraw.clear(TABLE_COLOR);
        // Draw board
        // The unusual backward ordering here has to do with overlapping outlines;
        // they produce a nice shadow effect when done in this order
        for (int r = Board.WIDTH - 1; r >= 0; r--) {
            for (int c = Board.WIDTH - 1; c >= 0; c--) {
                // r and c are converted to x and y in this call
                drawSquare(c, 14 - r, board.getSquare(new Location(r, c)), false, false, false);
            }
        }
        // Draw hands
        ArrayList<Character> hand = board.getHand(0);
        for (int i = 0; i < hand.size(); i++) {
            drawSquare(16 + i, 14, hand.get(i), false, false, true);
        }
        hand = board.getHand(1);
        for (int i = 0; i < hand.size(); i++) {
            drawSquare(16 + i, 11, hand.get(i), mode == mode.HAND && handCursor == i,
                    mode == mode.HAND && tilesToDiscard[i], false);
        }
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(INTERFACE_FONT);
        StdDraw.text(19, 13, "Opponent: " + board.getScore(0));
        StdDraw.text(19, 10, "You: " + board.getScore(1));
        if (mode == Mode.BOARD) {
            // Draw cursor
            drawBoardCursor();
            // Draw word being constructed
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont(TYPING_FONT);
            StdDraw.text(19, 8, "[" + wordBeingConstructed + "]");
            // Draw instructions
            StdDraw.setFont(INTERFACE_FONT);
            StdDraw.text(19, 6, "Type a word and hit enter to play.");
            StdDraw.text(19, 5, "Use spaces for tiles aready on board,");
            StdDraw.text(19, 4, "an upper-case letter to play a blank.");
            StdDraw.text(19, 3, "Use arrow keys to move cursor, / to");
            StdDraw.text(19, 2, "toggle direction.");
            StdDraw.text(19, 1, "Use alt to exchange letters or pass.");
        } else if (mode == Mode.HAND) {
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont(INTERFACE_FONT);
            StdDraw.text(19, 6, "Use arrow keys to move in hand.");
            StdDraw.text(19, 5, "Space to mark/unmark tile.");
            StdDraw.text(19, 4, "Enter to exchange marked tiles.");
            StdDraw.text(19, 3, "Use alt to return to board.");
        } else if (mode == Mode.ILLEGAL_MOVE) {
            // Draw word being constructed
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont(TYPING_FONT);
            StdDraw.text(19, 8, "[" + wordBeingConstructed + "]");
            // Draw instructions
            StdDraw.setFont(INTERFACE_FONT);
            StdDraw.text(19, 6, "Illegal move.");
            StdDraw.text(19, 5, "Press enter to continue.");
        } else if (mode == Mode.GAME_OVER) {
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont(INTERFACE_FONT);
            StdDraw.text(19, 6, "Game over.");
        } else if (mode == Mode.AI_PLAYING) {
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont(INTERFACE_FONT);
            StdDraw.text(19, 6, "Opponent thinking...");
        }
        StdDraw.show();
    }

    /** Draws the board cursor. */
    private void drawBoardCursor() {
        int x = boardCursor.getColumn();
        int y = 14 - boardCursor.getRow();
        double[] xs;
        double[] ys;
        if (boardCursorDirection == Location.HORIZONTAL) {
            xs = new double[] {x - 0.4, x - 0.4, x + 0.4};
            ys = new double[] {y - 0.2, y + 0.2, y};
        } else {
            xs = new double[] {x - 0.2, x + 0.2, x};
            ys = new double[] {y + 0.4, y + 0.4, y - 0.4};
        }
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.filledPolygon(xs, ys);
        StdDraw.setPenColor(TABLE_COLOR);
        StdDraw.polygon(xs, ys);
    }

    /**
     * Draws one square or tile at position x, y. @see Board
     *
     * @param outlined True if this is the current tile in the hand when selecting tiles to exchange.
     * @param crossedOut True if this tile has been marked for exchange.
     * @param faceDown True if this is a tile in the opponent's hand.
     */
    private void drawSquare(int x, int y, char square, boolean outlined, boolean crossedOut, boolean faceDown) {
        // Draw background
        StdDraw.setPenColor(COLORS.get(square));
        StdDraw.filledSquare(x, y, 0.5);
        // Draw letter and value for regular tile
        if (!faceDown) {
            if (square >= 'a' && square <= 'z') {
                StdDraw.setPenColor(Color.BLACK);
                StdDraw.setFont(LETTER_FONT);
                StdDraw.text(x, y, ("" + square).toUpperCase());
                StdDraw.setFont(VALUE_FONT);
                StdDraw.text(x + 0.3, y - 0.3, "" + Board.TILE_VALUES.get(square));
            } else if (square >= 'A' && square <= 'Z') {
                StdDraw.setPenColor(Color.RED);
                StdDraw.setFont(LETTER_FONT);
                StdDraw.text(x, y, ("" + square).toUpperCase());
            }
        }
        // Draw outline
        if (outlined) {
            StdDraw.setPenColor(Color.WHITE);
        } else if (Character.isAlphabetic(square) || square == '_') {
            StdDraw.setPenColor(Color.BLACK);
        } else {
            StdDraw.setPenColor(Color.WHITE);
        }
        StdDraw.square(x, y, 0.5);
        // Draw slash
        if (crossedOut) {
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.line(x - 0.5, y - 0.5, x + 0.5, y + 0.5);
            StdDraw.line(x - 0.5, y + 0.5, x + 0.5, y - 0.5);
        }
    }

}
