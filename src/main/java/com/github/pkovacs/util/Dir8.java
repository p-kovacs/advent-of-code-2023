package com.github.pkovacs.util;

/**
 * Represents one of the 8 directions (cardinal and intercardinal directions) in 2D space.
 */
public enum Dir8 {

    N, NE, E, SE, S, SW, W, NW;

    /**
     * Returns the direction corresponding to the given string representation.
     */
    public static Dir8 fromString(String str) {
        return switch (str) {
            case "N", "n" -> N;
            case "NE", "ne" -> NE;
            case "E", "e" -> E;
            case "SE", "se" -> SE;
            case "S", "s" -> S;
            case "SW", "sw" -> SW;
            case "W", "w" -> W;
            case "NW", "nw" -> NW;
            default -> throw new IllegalArgumentException("Unknown direction: '" + str + "'.");
        };
    }

    /**
     * Returns the opposite of this direction.
     */
    public Dir8 opposite() {
        return switch (this) {
            case N -> S;
            case NE -> SW;
            case E -> W;
            case SE -> NW;
            case S -> N;
            case SW -> NE;
            case W -> E;
            case NW -> SE;
        };
    }

    /**
     * Returns the "next" direction according to clockwise order. That is, the direction to the right of this one.
     */
    public Dir8 next() {
        return switch (this) {
            case N -> NE;
            case NE -> E;
            case E -> SE;
            case SE -> S;
            case S -> SW;
            case SW -> W;
            case W -> NW;
            case NW -> N;
        };
    }

    /**
     * Returns the "prev" direction according to clockwise order. That is, the direction to the left of this one.
     */
    public Dir8 prev() {
        return switch (this) {
            case N -> NW;
            case NE -> N;
            case E -> NE;
            case SE -> E;
            case S -> SE;
            case SW -> S;
            case W -> SW;
            case NW -> W;
        };
    }

    /**
     * Mirrors this direction horizontally. That is, swaps east and west.
     */
    public Dir8 mirrorHorizontally() {
        return switch (this) {
            case NE -> NW;
            case E -> W;
            case SE -> SW;
            case SW -> SE;
            case W -> E;
            case NW -> NE;
            default -> this;
        };
    }

    /**
     * Mirrors this direction vertically. That is, swaps north and south.
     */
    public Dir8 mirrorVertically() {
        return switch (this) {
            case N -> S;
            case NE -> SE;
            case SE -> NE;
            case S -> N;
            case SW -> NW;
            case NW -> SW;
            default -> this;
        };
    }

}
