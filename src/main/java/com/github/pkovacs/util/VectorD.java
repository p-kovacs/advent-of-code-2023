package com.github.pkovacs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Represents an immutable position vector in D-dimensional coordinate space with integer precision. It is represented
 * as an array of {@code long} coordinates. Provides various useful methods and supports lexicographical ordering.
 * <p>
 * This class is the D-dimensional generalization of {@link Pos} and {@link Vector}.
 *
 * @see Pos
 * @see Vector
 */
public final class VectorD implements Comparable<VectorD> {

    private final long[] coords;

    /**
     * Creates a D-dimensional vector with the given coordinates.
     *
     * @throws IllegalArgumentException if less than two coordinates are given
     */
    public VectorD(long... coords) {
        if (coords.length < 2) {
            throw new IllegalArgumentException("At least two coordinates are required.");
        }
        this.coords = coords.clone();
    }

    /**
     * Creates a D-dimensional vector with the given coordinates.
     *
     * @throws IllegalArgumentException if less than two coordinates are given
     */
    public VectorD(List<Long> coords) {
        if (coords.size() < 2) {
            throw new IllegalArgumentException("At least two coordinates are required.");
        }
        this.coords = coords.stream().mapToLong(i -> i).toArray();
    }

    /**
     * Returns the origin vector with the given dimension.
     *
     * @throws IllegalArgumentException if the dimension is less than two
     */
    public static VectorD origin(int dim) {
        return new VectorD(new long[dim]);
    }

    /**
     * Returns the dimension of this vector.
     */
    public int dim() {
        return coords.length;
    }

    /**
     * Returns the k-th coordinate of this vector.
     *
     * @throws IndexOutOfBoundsException if {@code k >= dim()}
     */
    public long get(int k) {
        return coords[k];
    }

    /**
     * Creates a new vector by changing the k-th coordinate of this vector.
     *
     * @throws IndexOutOfBoundsException if {@code k >= dim()}
     */
    public VectorD with(int k, long value) {
        long[] newCoords = coords.clone();
        newCoords[k] = value;
        return new VectorD(newCoords);
    }

    /**
     * Returns true if the given other vector is one of the main neighbors of this vector
     * (that is, {@code this.dist1(other) == 1}).
     */
    public boolean isNeighbor(VectorD other) {
        return dist1(other) == 1;
    }

    /**
     * Returns true if the given other vector is one of the "extended" neighbors of this vector
     * (that is, {@code this.distMax(other) == 1}).
     */
    public boolean isExtendedNeighbor(VectorD other) {
        return distMax(other) == 1;
    }

    /**
     * Returns a lexicographically sorted stream of the main neighbors of this vector.
     * The stream contains {@code 2 * dim()} vectors, and for each returned vector {@code v},
     * {@code this.dist1(v) == 1}.
     */
    public Stream<VectorD> neighbors() {
        return neighborsAndSelf().filter(p -> p != this);
    }

    /**
     * Returns a lexicographically sorted stream of this vector and its main neighbors.
     * The stream contains {@code 2 * dim() + 1} vectors, and for each returned vector {@code v},
     * {@code this.dist1(v) <= 1}.
     */
    public Stream<VectorD> neighborsAndSelf() {
        var list = new ArrayList<VectorD>();
        for (int k = 0; k < dim(); k++) {
            list.add(with(k, coords[k] - 1));
        }
        list.add(this);
        for (int k = dim() - 1; k >= 0; k--) {
            list.add(with(k, coords[k] + 1));
        }
        return list.stream();
    }

    /**
     * Returns a lexicographically sorted stream of the "extended" neighbors of this vector.
     * The stream contains {@code 3^dim() - 1} vectors, and for each returned vector {@code v},
     * {@code this.distMax(v) == 1}.
     */
    public Stream<VectorD> extendedNeighbors() {
        return extendedNeighborsAndSelf().filter(p -> p != this);
    }

    /**
     * Returns a lexicographically sorted stream of this vector and its "extended" neighbors.
     * The stream contains {@code 3^dim()} vectors, and for each returned vector {@code v},
     * {@code this.distMax(v) <= 1}.
     */
    public Stream<VectorD> extendedNeighborsAndSelf() {
        var list = List.of(this);
        for (int i = 0; i < dim(); i++) {
            int k = i;
            list = list.stream()
                    .flatMap(v -> Stream.of(v.with(k, v.get(k) - 1), v, v.with(k, v.get(k) + 1)))
                    .toList();
        }
        return list.stream();
    }

    /**
     * Creates a new vector by adding the given other vector to this one.
     *
     * @throws IllegalArgumentException if the vectors have different dimensions
     */
    public VectorD plus(VectorD other) {
        return newInstance(checkDimensions(this, other), i -> coords[i] + other.coords[i]);
    }

    /**
     * Creates a new vector by subtracting the given other vector from this one.
     *
     * @throws IllegalArgumentException if the vectors have different dimensions
     */
    public VectorD minus(VectorD other) {
        return newInstance(checkDimensions(this, other), i -> coords[i] - other.coords[i]);
    }

    /**
     * Creates a new vector that is the opposite of this vector.
     */
    public VectorD opposite() {
        return newInstance(dim(), i -> -coords[i]);
    }

    /**
     * Creates a new vector by multiplying each coordinate of this vector by the given scalar factor.
     */
    public VectorD multiply(long factor) {
        return newInstance(dim(), i -> factor * coords[i]);
    }

    private static int checkDimensions(VectorD a, VectorD b) {
        if (a.coords.length != b.coords.length) {
            throw new IllegalArgumentException("Vectors have different dimensions.");
        }
        return a.coords.length;
    }

    private static VectorD newInstance(int dim, Function<Integer, Long> function) {
        long[] newCoords = new long[dim];
        for (int i = 0; i < newCoords.length; i++) {
            newCoords[i] = function.apply(i);
        }
        return new VectorD(newCoords);
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Taxicab_geometry">"taxicab" distance</a>
     * (aka. L1 distance or Manhattan distance) between this vector and the {@link #origin(int) origin}.
     */
    public long dist1() {
        return Arrays.stream(coords).map(Utils::abs).sum();
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Taxicab_geometry">"taxicab" distance</a>
     * (aka. L1 distance or Manhattan distance) between this vector and the given other vector.
     *
     * @throws IllegalArgumentException if the vectors have different dimensions
     */
    public long dist1(VectorD other) {
        return other.minus(this).dist1();
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Chebyshev_distance">"maximum" distance</a>
     * (aka. L∞ distance or Chebyshev distance) between this vector and the {@link #origin(int) origin}.
     */
    public long distMax() {
        return Arrays.stream(coords).map(Utils::abs).max().orElseThrow();
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Chebyshev_distance">"maximum" distance</a>
     * (aka. L∞ distance or Chebyshev distance) between this vector and the given other vector.
     *
     * @throws IllegalArgumentException if the vectors have different dimensions
     */
    public long distMax(VectorD other) {
        return other.minus(this).distMax();
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance#Squared_Euclidean_distance">squared
     * Eucledian distance</a> between this vector and the {@link #origin(int) origin}.
     * <p>
     * Warning: this distance metric does not satisfy the triangle inequality.
     */
    public long distSq() {
        return Arrays.stream(coords).map(i -> i * i).sum();
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance#Squared_Euclidean_distance">squared
     * Eucledian distance</a> between this vector and the given other vector.
     * <p>
     * Warning: this distance metric does not satisfy the triangle inequality.
     *
     * @throws IllegalArgumentException if the vectors have different dimensions
     */
    public long distSq(VectorD other) {
        return other.minus(this).distSq();
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Eucledian distance</a>
     * (aka. L2 distance) between this vector and the {@link #origin(int) origin}.
     */
    public double dist2() {
        return Math.sqrt(distSq());
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Eucledian distance</a>
     * (aka. L2 distance) between this vector and the given other vector.
     *
     * @throws IllegalArgumentException if the vectors have different dimensions
     */
    public double dist2(VectorD other) {
        return other.minus(this).dist2();
    }

    @Override
    public String toString() {
        return "(" + Arrays.stream(coords).mapToObj(String::valueOf).collect(joining(",")) + ")";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof VectorD v && Arrays.equals(coords, v.coords));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coords);
    }

    @Override
    public int compareTo(VectorD other) {
        if (coords.length != other.coords.length) {
            return Integer.compare(coords.length, other.coords.length);
        }
        for (int i = 0; i < coords.length; i++) {
            int c = Long.compare(coords[i], other.coords[i]);
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

}
