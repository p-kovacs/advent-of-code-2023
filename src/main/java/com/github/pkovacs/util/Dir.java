package com.github.pkovacs.util;

/**
 * Represents one of the 4 main directions (cardinal directions) in 2D space: N (north), E (east), S (south), W (west).
 * Provides methods to convert from/to characters and to rotate and mirror a direction.
 */
public enum Dir {

    N, E, S, W;

    /**
     * Returns the direction corresponding to the given string.
     *
     * @param the direction string. One of "NORTH", "EAST", "SOUTH", "WEST", and their lowercase variants, or
     *         one of the characters accepted by {@link #fromChar(char)}.
     */
    public static Dir fromString(String str) {
        return switch (str) {
            case "n", "N", "u", "U", "^", "NORTH", "north" -> N;
            case "e", "E", "r", "R", ">", "EAST", "east" -> E;
            case "s", "S", "d", "D", "v", "V", "SOUTH", "south" -> S;
            case "w", "W", "l", "L", "<", "WEST", "west" -> W;
            default -> throw new IllegalArgumentException("Unknown direction: '" + str + "'.");
        };
    }

    /**
     * Returns the direction corresponding to the given character.
     *
     * @param ch the direction character. One of 'N' (north), 'E' (east), 'S' (south), 'W' (west),
     *         'U' (up), 'R' (right), 'D' (down), 'L' (left), '^' (up), '>' (right), 'v' (down), '<' (left),
     *         and their lowercase variants.
     */
    public static Dir fromChar(char ch) {
        return switch (ch) {
            case 'n', 'N', 'u', 'U', '^' -> N;
            case 'e', 'E', 'r', 'R', '>' -> E;
            case 's', 'S', 'd', 'D', 'v', 'V' -> S;
            case 'w', 'W', 'l', 'L', '<' -> W;
            default -> throw new IllegalArgumentException("Unknown direction: '" + ch + "'.");
        };
    }

    /**
     * Returns the {@link Dir8} value corresponding to this direction.
     */
    public Dir8 toDir8() {
        return switch (this) {
            case N -> Dir8.N;
            case E -> Dir8.E;
            case S -> Dir8.S;
            case W -> Dir8.W;
        };
    }

    /**
     * Returns true if this direction is horizontal: EAST or WEST.
     */
    public boolean isHorizontal() {
        return this == E || this == W;
    }

    /**
     * Returns true if this direction is vertical: NORTH or SOUTH.
     */
    public boolean isVertical() {
        return this == N || this == S;
    }

    /**
     * Returns the opposite of this direction.
     */
    public Dir opposite() {
        return switch (this) {
            case N -> S;
            case E -> W;
            case S -> N;
            case W -> E;
        };
    }

    /**
     * Rotates this direction 90 degrees to the right (clockwise).
     */
    public Dir rotateRight() {
        return switch (this) {
            case N -> E;
            case E -> S;
            case S -> W;
            case W -> N;
        };
    }

    /**
     * Rotates this direction 90 degrees to the left (counter-clockwise).
     */
    public Dir rotateLeft() {
        return switch (this) {
            case N -> W;
            case E -> N;
            case S -> E;
            case W -> S;
        };
    }

    /**
     * Mirrors this direction horizontally. That is, swaps east and west.
     */
    public Dir mirrorHorizontally() {
        return switch (this) {
            case E -> W;
            case W -> E;
            default -> this;
        };
    }

    /**
     * Mirrors this direction vertically. That is, swaps north and south.
     */
    public Dir mirrorVertically() {
        return switch (this) {
            case N -> S;
            case S -> N;
            default -> this;
        };
    }

}
