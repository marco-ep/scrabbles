import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
    }

    /** Returns the characters in s as an ArrayList of Characters. */
    private ArrayList<Character> asCharList(String s) {
        ArrayList<Character> result = new ArrayList<>();
        for (char c : s.toCharArray()) {
            result.add(c);
        }
        return result;
    }

    @Test
    public void bonusSquaresAreProperlyLaidOut() {
        assertArrayEquals(Board.LAYOUT, board.toString().split("\n"));
    }

    @Test
    public void validLettersCanBePlayedFromHand() {
        assertTrue(board.canBeDrawnFromHand("cheese", asCharList("eecchse")));
    }

    @Test
    public void handTilesCannotBeReusedInSameWord() {
        assertFalse(board.canBeDrawnFromHand("spouse", asCharList("poseur_")));
    }

    @Test
    public void blanksCanBeUsedFromHand() {
        assertTrue(board.canBeDrawnFromHand("beaST", asCharList("ab_cd_e")));
    }

    @Test
    public void initialWordCanBePlaced() {
        assertTrue(board.canBePlacedOnBoard("horn", Location.CENTER, Location.HORIZONTAL));
    }

    @Test
    public void wordCannotOverlapTilesOnBoard() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertFalse(board.canBePlacedOnBoard("tank", new Location(6, 8), Location.VERTICAL));
    }

    @Test
    public void wordCannotContainGaps() {
        assertFalse(board.canBePlacedOnBoard("p nt", Location.CENTER, Location.HORIZONTAL));
    }

    @Test
    public void wordCanUseExistingLetters() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertTrue(board.canBePlacedOnBoard("fa m", new Location(5, 9), Location.VERTICAL));
    }

    @Test
    public void wordCannotHaveExistingTileBeforeOrAfter() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertFalse(board.canBePlacedOnBoard("xx", new Location(5, 7), Location.VERTICAL));
        assertFalse(board.canBePlacedOnBoard("xx", new Location(8, 7), Location.VERTICAL));
    }

    @Test
    public void wordCannotExtendBeyondBoard() {
        assertFalse(board.canBePlacedOnBoard("beyond", new Location(2, 10), Location.HORIZONTAL));
    }

    @Test
    public void initialWordCountsAsConnected() {
        assertTrue(board.wouldBeConnected("horn", new Location(7, 5), Location.HORIZONTAL));
    }

    @Test
    public void wordUsingExistingTileCountsAsConnected() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertTrue(board.wouldBeConnected("fa m", new Location(5, 9), Location.VERTICAL));
    }

    @Test
    public void wordNextToExistingTileCountsAsConnected() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertTrue(board.wouldBeConnected("apple", new Location(6, 9), Location.HORIZONTAL));
    }

    @Test
    public void unconnectedWordDoesNotCountAsConnected() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertFalse(board.wouldBeConnected("trounce", new Location(0, 0), Location.HORIZONTAL));
    }

    @Test
    public void acceptsValidDictionaryWord() {
        assertTrue(board.isValidWord("horn", Location.CENTER, Location.HORIZONTAL));
    }

    @Test
    public void rejectsInvalidDictionaryWord() {
        assertFalse(board.isValidWord("gqnx", Location.CENTER, Location.HORIZONTAL));
    }

    @Test
    public void acceptsPlayThatCreatesLegalWords() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertTrue(board.wouldCreateOnlyLegalWords("pan", new Location(6, 6), Location.HORIZONTAL));
    }

    @Test
    public void rejectsPlayThatCreatesIllegalWord() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertFalse(board.wouldCreateOnlyLegalWords("and", new Location(6, 7), Location.HORIZONTAL));
    }

    @Test
    public void scoresSingleInitialWord() {
        assertEquals(14, board.score("horn", Location.CENTER, Location.HORIZONTAL));
    }

    @Test
    public void scoresMultipleWords() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertEquals(5 + 3 + 3, board.score("an", new Location(6, 7), Location.HORIZONTAL));
    }

    @Test
    public void doesNotScoreUnmodifiedCrossWords() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertEquals(5, board.score("a ", new Location(6, 7), Location.VERTICAL));
    }

    @Test
    public void scoresBingo() {
        assertEquals(76, board.score("finalLy", Location.CENTER, Location.HORIZONTAL));    }

    @Test
    public void doesNotScoreBingoForSevenLetterWordUsingTilesOnBoard() {
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertEquals(18, board.score("Fi ally", new Location(5, 10), Location.VERTICAL));
    }

    @Test
    public void verifyLegalityRejectsOneLetterWord() {
        ArrayList<Character> hand = new ArrayList<>();
        hand.add('a');
        assertThrows(IllegalMoveException.class, () -> board.verifyLegality("a", Location.CENTER, Location.HORIZONTAL, hand));
    }

    @Test
    public void verifyLegalityRejectsTilesNotInHand() {
        ArrayList<Character> hand = new ArrayList<>();
        for (char c : "abcdefg".toCharArray()) {
            hand.add(c);
        }
        assertThrows(IllegalMoveException.class, () -> board.verifyLegality("cat", Location.CENTER, Location.HORIZONTAL, hand));
    }

    @Test
    public void verifyLegalityRejectsWordOverlappingExistingTile() {
        ArrayList<Character> hand = new ArrayList<>();
        for (char c : "abcdefg".toCharArray()) {
            hand.add(c);
        }
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertThrows(IllegalMoveException.class, () -> board.verifyLegality("bag", new Location(6, 8), Location.VERTICAL, hand));
    }

    @Test
    public void verifyLegalityRejectsGapInMiddleOfWord() {
        ArrayList<Character> hand = new ArrayList<>();
        for (char c : "abcdefg".toCharArray()) {
            hand.add(c);
        }
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertThrows(IllegalMoveException.class, () -> board.verifyLegality("a c", new Location(6, 6), Location.HORIZONTAL, hand));
    }

    @Test
    public void verifyLegalityRejectsWordAbuttingExistingTiles() {
        ArrayList<Character> hand = new ArrayList<>();
        for (char c : "abcdefg".toCharArray()) {
            hand.add(c);
        }
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertThrows(IllegalMoveException.class, () -> board.verifyLegality("bag", new Location(7, 9), Location.VERTICAL, hand));
    }

    @Test
    public void verifyLegalityRejectsWordOBeyondEdgeOfBoard() {
        ArrayList<Character> hand = new ArrayList<>();
        for (char c : "absolve".toCharArray()) {
            hand.add(c);
        }
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertThrows(IllegalMoveException.class, () -> board.verifyLegality("absolve", new Location(6, 10), Location.HORIZONTAL, hand));
    }

    @Test
    public void verifyLegalityRejectsIsolatedWord() {
        ArrayList<Character> hand = new ArrayList<>();
        for (char c : "abcdefg".toCharArray()) {
            hand.add(c);
        }
        board.placeWord("horn", Location.CENTER, Location.HORIZONTAL);
        assertThrows(IllegalMoveException.class, () -> board.verifyLegality("bag", new Location(5, 8), Location.HORIZONTAL, hand));
    }

    @Test
    public void verifyLegalityRejectsMisspelledWord() {
        ArrayList<Character> hand = new ArrayList<>();
        for (char c : "drowze_".toCharArray()) {
            hand.add(c);
        }
        assertThrows(IllegalMoveException.class, () -> board.verifyLegality("drowzEe", new Location(6, 7), Location.VERTICAL, hand));
    }

    @Test
    public void verifyLegalityAcceptsValidWords() throws IllegalMoveException {
        ArrayList<Character> hand = new ArrayList<>();
        // Turn one (from traditional rules example)
        for (char c : "horn".toCharArray()) {
            hand.add(c);
        }
        board.verifyLegality("horn", new Location(7, 4), Location.HORIZONTAL, hand);
        board.placeWord("horn", new Location(7, 4), Location.HORIZONTAL);
        // Turn two
        hand.clear();
        for (char c : "fam".toCharArray()) {
            hand.add(c);
        }
        board.verifyLegality("fa m", new Location(5, 6), Location.VERTICAL, hand);
        board.placeWord("fa m", new Location(5, 6), Location.VERTICAL);
        // Turn three
        hand.clear();
        for (char c : "paste".toCharArray()) {
            hand.add(c);
        }
        board.verifyLegality("paste", new Location(9, 4), Location.HORIZONTAL, hand);
        board.placeWord("paste", new Location(9, 4), Location.HORIZONTAL);
        // Turn four
        hand.clear();
        for (char c : "ob".toCharArray()) {
            hand.add(c);
        }
        board.verifyLegality(" ob", new Location(8, 6), Location.HORIZONTAL, hand);
        board.placeWord(" ob", new Location(8, 6), Location.HORIZONTAL);
        // Turn five
        hand.clear();
        for (char c : "it_".toCharArray()) {
            hand.add(c);
        }
        board.verifyLegality("Bit", new Location(10, 3), Location.HORIZONTAL, hand);
        board.placeWord("Bit", new Location(10, 3), Location.HORIZONTAL);
    }

    @Test
    public void removeTilesRemovesCorrectTiles() {
        ArrayList<Character> hand = new ArrayList<>();
        // Turn one (from traditional rules example)
        for (char c : "ca__bda".toCharArray()) {
            hand.add(c);
        }
        board.removeTiles("Ba ", hand);
        assertEquals("[c, _, b, d, a]", hand.toString());
    }

}