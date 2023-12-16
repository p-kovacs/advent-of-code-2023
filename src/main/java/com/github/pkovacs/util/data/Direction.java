package com.github.pkovacs.util.data;

/**
 * Represents a direction in 2D space.
 * Provides methods to convert from/to characters and to rotate a direction.
 */
public enum Direction {

    NORTH, EAST, SOUTH, WEST;

    /**
     * Returns the uppercase character corresponding to this direction.
     *
     * @return 'N', 'E', 'S', or 'W'.
     */
    public char toChar() {
        return switch (this) {
            case NORTH -> 'N';
            case EAST -> 'E';
            case SOUTH -> 'S';
            case WEST -> 'W';
        };
    }

    /**
     * Returns the lowercase character corresponding to this direction.
     *
     * @return 'n', 'e', 's', or 'w'.
     */
    public char toLowerCaseChar() {
        return switch (this) {
            case NORTH -> 'n';
            case EAST -> 'e';
            case SOUTH -> 's';
            case WEST -> 'w';
        };
    }

    /**
     * Returns the direction corresponding to the given character.
     *
     * @param ch the direction character. One of 'N' (north), 'E' (east), 'S' (south), 'W' (west),
     *         'U' (up), 'R' (right), 'D' (down), 'L' (left), and their lowercase variants.
     */
    public static Direction fromChar(char ch) {
        return switch (ch) {
            case 'n', 'N', 'u', 'U' -> NORTH;
            case 'e', 'E', 'r', 'R' -> EAST;
            case 's', 'S', 'd', 'D' -> SOUTH;
            case 'w', 'W', 'l', 'L' -> WEST;
            default -> throw new IllegalArgumentException("Unknown direction: '" + ch + "'.");
        };
    }

    /**
     * Returns true if this direction is horizontal: EAST or WEST.
     */
    public boolean isHorizontal() {
        return this == EAST || this == WEST;
    }

    /**
     * Returns true if this direction is vertical: NORTH or SOUTH.
     */
    public boolean isVertical() {
        return this == NORTH || this == SOUTH;
    }

    /**
     * Returns the opposite of this direction.
     */
    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case EAST -> WEST;
            case SOUTH -> NORTH;
            case WEST -> EAST;
        };
    }

    /**
     * Rotates this direction 90 degrees to the left.
     */
    public Direction rotateLeft() {
        return switch (this) {
            case NORTH -> WEST;
            case EAST -> NORTH;
            case SOUTH -> EAST;
            case WEST -> SOUTH;
        };
    }

    /**
     * Rotates this direction 90 degrees to the right.
     */
    public Direction rotateRight() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }

    /**
     * Mirrors this direction horizontally.
     */
    public Direction mirrorHorizontally() {
        return switch (this) {
            case EAST -> WEST;
            case WEST -> EAST;
            default -> this;
        };
    }

    /**
     * Mirrors this direction vertically.
     */
    public Direction mirrorVertically() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            default -> this;
        };
    }

}
