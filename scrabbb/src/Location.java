import java.util.Objects;

/**
 * A location on the Scrabble board, with row and column coordinates. Immutable. Row and column are 0-based from top
 * left.
 */
public class Location {

    /** Direction for horizontal words. */
    public static final Location HORIZONTAL = new Location(0, 1);

    /** Direction for vertical words. */
    public static final Location VERTICAL = new Location(1, 0);

    /** The center square (which the first move must contain. */
    public static final Location CENTER = new Location(7, 7);

    private final int row;

    private final int column;

    public Location(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    /**
     * Returns a new Location which is offset from this by direction. For example, a.neighbor(HORIZONTAL) is the
     * location to the right of a.
     *
     * @param direction HORIZONTAL or VERTICAL.
     */
    public Location neighbor(Location direction) {
        return new Location(row + direction.row, column + direction.column);
    }

    /**
     * Returns a new Location which is offset from this by the opposite of direction. For example,
     * a.neighbor(HORIZONTAL) is the location to the left of a.
     *
     * @param direction HORIZONTAL or VERTICAL.
     */
    public Location antineighbor(Location direction) {
        return new Location(row - direction.row, column - direction.column);
    }

    /**
     * Returns the opposite of this direction. HORIZONTAL and VERTICAL are opposites.
     */
    public Location opposite() {
        if (this == HORIZONTAL) {
            return VERTICAL;
        }
        return HORIZONTAL;
    }

    /** Returns true if this Location is on the board. */
    public boolean isOnBoard() {
        return row >= 0 && row < 15 && column >= 0 && column < 15;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Location location = (Location) o;
        return row == location.row &&
                column == location.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "Location{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }

}
