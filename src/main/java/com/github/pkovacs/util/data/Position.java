package com.github.pkovacs.util.data;

/**
 * Common interface of 2D position classes {@link Point} and {@link Cell}.
 */
public interface Position {

    int x();

    int y();

    /**
     * Returns true if the given position is a neighbor of this position.
     */
    default boolean isNeighbor(Position other) {
        return dist1(other) == 1;
    }

    /**
     * Returns true if the given position is an "extended" neighbor of this position, also including the diagonal ones.
     */
    default boolean isExtendedNeighbor(Position other) {
        return distMax(other) == 1;
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Taxicab_geometry">Manhattan distance</a>
     * (aka. L1 distance or "taxicab" distance) between this position and (0, 0).
     */
    default int dist1() {
        return dist1(ORIGIN);
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Taxicab_geometry">Manhattan distance</a>
     * (aka. L1 distance or "taxicab" distance) between this position and the given position.
     */
    default int dist1(Position other) {
        return Math.abs(other.x() - x()) + Math.abs(other.y() - y());
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Chebyshev_distance">"maximum" distance</a>
     * (aka. L∞ distance or Chebyshev distance) between this position and (0, 0).
     */
    default int distMax() {
        return distMax(ORIGIN);
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Chebyshev_distance">"maximum" distance</a>
     * (aka. L∞ distance or Chebyshev distance) between this position and the given position.
     */
    default int distMax(Position other) {
        return Math.max(Math.abs(other.x() - x()), Math.abs(other.y() - y()));
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance#Squared_Euclidean_distance">squared
     * Eucledian distance</a> between this position and (0, 0).
     * <p>
     * Warning: this distance does not satisfy the triangle inequality.
     */
    default int distSq() {
        return distSq(ORIGIN);
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance#Squared_Euclidean_distance">squared
     * Eucledian distance</a> between this position and the given position.
     * <p>
     * Warning: this distance does not satisfy the triangle inequality.
     */
    default int distSq(Position other) {
        int dx = other.x() - x();
        int dy = other.y() - y();
        return dx * dx + dy * dy;
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Eucledian distance</a>
     * (aka. L2 distance) between this position and (0, 0).
     */
    default double dist2() {
        return dist2(ORIGIN);
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Eucledian distance</a>
     * (aka. L2 distance) between this position and the given position.
     */
    default double dist2(Position other) {
        return Math.sqrt(distSq(other));
    }

    /** The origin position: (0, 0). */
    Position ORIGIN = new Position() {

        @Override
        public int x() {
            return 0;
        }

        @Override
        public int y() {
            return 0;
        }

    };

}
