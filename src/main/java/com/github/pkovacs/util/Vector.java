package com.github.pkovacs.util;

import java.util.stream.Stream;

/**
 * Represents an immutable position vector in 3D coordinate space with integer precision. It is represented as
 * three {@code long} coordinates: {@code (x,y,z)}. Provides various useful methods and supports lexicographical
 * ordering (first by {@code x}, then by {@code y}, finally by {@code z}).
 * <p>
 * This class is the 3D counterpart of {@link Pos}.
 *
 * @apiNote This class is not a record in order to provide easier access to {@link #x}, {@link #y}, and
 *         {@link #z} as public final fields.
 * @see Pos
 * @see VectorBox
 * @see VectorD
 */
public final class Vector implements Comparable<Vector> {

    /** The origin in 3D space: {@code (0,0,0)}. */
    public static final Vector ORIGIN = new Vector(0, 0, 0);

    /**
     * The x coordinate.
     *
     * @apiNote This field is made public to simplify the usage of this class.
     */
    public final long x;

    /**
     * The y coordinate.
     *
     * @apiNote This field is made public to simplify the usage of this class.
     */
    public final long y;

    /**
     * The z coordinate.
     *
     * @apiNote This field is made public to simplify the usage of this class.
     */
    public final long z;

    /**
     * Constructs a new vector with the given coordinates.
     */
    public Vector(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns the x coordinate.
     *
     * @apiNote You can also use the public final field {@link #x} directly, but this method is practical when
     *         used as a method reference: {@code Vector::x}.
     */
    public long x() {
        return x;
    }

    /**
     * Returns the y coordinate.
     *
     * @apiNote You can also use the public final field {@link #y} directly, but this method is practical when
     *         used as a method reference: {@code Vector::y}.
     */
    public long y() {
        return y;
    }

    /**
     * Returns the z coordinate.
     *
     * @apiNote You can also use the public final field {@link #z} directly, but this method is practical when
     *         used as a method reference: {@code Vector::z}.
     */
    public long z() {
        return z;
    }

    /**
     * Returns the x coordinate as an {@code int} value.
     *
     * @throws ArithmeticException if the x coordinate overflows an int
     */
    public int xInt() {
        return Math.toIntExact(x);
    }

    /**
     * Returns the y coordinate as an {@code int} value.
     *
     * @throws ArithmeticException if the y coordinate overflows an int
     */
    public int yInt() {
        return Math.toIntExact(y);
    }

    /**
     * Returns the z coordinate as an {@code int} value.
     *
     * @throws ArithmeticException if the z coordinate overflows an int
     */
    public int zInt() {
        return Math.toIntExact(z);
    }

    /**
     * Returns a new vector with the given x coordinate and the original y and z coordinates of this vector.
     */
    public Vector withX(long newX) {
        return new Vector(newX, y, z);
    }

    /**
     * Returns a new vector with the given y coordinate and the original x and z coordinates of this vector.
     */
    public Vector withY(long newY) {
        return new Vector(x, newY, z);
    }

    /**
     * Returns a new vector with the given z coordinate and the original x and y coordinates of this vector.
     */
    public Vector withZ(long newZ) {
        return new Vector(x, y, newZ);
    }

    /**
     * Returns true if the given other vector is one of the 6 main neighbors of this vector
     * (that is, {@code this.dist1(other) == 1}).
     */
    public boolean isNeighbor(Vector other) {
        return dist1(other) == 1;
    }

    /**
     * Returns true if the given other vector is one of the 26 "extended" neighbors of this vector
     * (that is, {@code this.distMax(other) == 1}).
     */
    public boolean isExtendedNeighbor(Vector other) {
        return distMax(other) == 1;
    }

    /**
     * Returns a lexicographically sorted stream of the 6 main neighbors of this vector.
     * For each returned vector {@code v}, {@code this.dist1(v) == 1}.
     */
    public Stream<Vector> neighbors() {
        return Stream.of(
                new Vector(x - 1, y, z),
                new Vector(x, y - 1, z),
                new Vector(x, y, z - 1),
                new Vector(x, y, z + 1),
                new Vector(x, y + 1, z),
                new Vector(x + 1, y, z)
        );
    }

    /**
     * Returns a lexicographically sorted stream of this vector and its 6 main neighbors.
     * For each returned vector {@code v}, {@code this.dist1(v) == 1}.
     */
    public Stream<Vector> neighborsAndSelf() {
        return Stream.of(
                new Vector(x - 1, y, z),
                new Vector(x, y - 1, z),
                new Vector(x, y, z - 1),
                this,
                new Vector(x, y, z + 1),
                new Vector(x, y + 1, z),
                new Vector(x + 1, y, z)
        );
    }

    /**
     * Returns a lexicographically sorted stream of the 26 "extended" neighbors of this vector.
     * For each returned vector {@code v}, {@code this.distMax(v) == 1}.
     */
    public Stream<Vector> extendedNeighbors() {
        return extendedNeighborsAndSelf().filter(p -> p != this);
    }

    /**
     * Returns a lexicographically sorted stream of this vector and its 26 "extended" neighbors.
     * For each returned vector {@code v}, {@code this.distMax(v) <= 1}.
     */
    public Stream<Vector> extendedNeighborsAndSelf() {
        return Stream.of(
                new Vector(x - 1, y - 1, z - 1), new Vector(x - 1, y - 1, z), new Vector(x - 1, y - 1, z + 1),
                new Vector(x - 1, y, z - 1), new Vector(x - 1, y, z), new Vector(x - 1, y, z + 1),
                new Vector(x - 1, y + 1, z - 1), new Vector(x - 1, y + 1, z), new Vector(x - 1, y + 1, z + 1),
                new Vector(x, y - 1, z - 1), new Vector(x, y - 1, z), new Vector(x, y - 1, z + 1),
                new Vector(x, y, z - 1), this, new Vector(x, y, z + 1),
                new Vector(x, y + 1, z - 1), new Vector(x, y + 1, z), new Vector(x, y + 1, z + 1),
                new Vector(x + 1, y - 1, z - 1), new Vector(x + 1, y - 1, z), new Vector(x + 1, y - 1, z + 1),
                new Vector(x + 1, y, z - 1), new Vector(x + 1, y, z), new Vector(x + 1, y, z + 1),
                new Vector(x + 1, y + 1, z - 1), new Vector(x + 1, y + 1, z), new Vector(x + 1, y + 1, z + 1)
        );
    }

    /**
     * Creates a new vector by adding the given delta values to the coordinates of this vector.
     */
    public Vector plus(long dx, long dy, long dz) {
        return new Vector(x + dx, y + dy, z + dz);
    }

    /**
     * Creates a new vector by adding the given other vector to this one.
     */
    public Vector plus(Vector other) {
        return new Vector(x + other.x, y + other.y, z + other.z);
    }

    /**
     * Creates a new vector by subtracting the given other vector from this one.
     */
    public Vector minus(Vector other) {
        return new Vector(x - other.x, y - other.y, z - other.z);
    }

    /**
     * Creates a new vector that is the opposite of this vector.
     */
    public Vector opposite() {
        return new Vector(-x, -y, -z);
    }

    /**
     * Creates a new vector by multiplying each coordinate of this vector by the given scalar factor.
     */
    public Vector multiply(long factor) {
        return new Vector(factor * x, factor * y, factor * z);
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Taxicab_geometry">"taxicab" distance</a>
     * (aka. L1 distance or Manhattan distance) between this vector and the {@link #ORIGIN} {@code (0,0,0)}.
     */
    public long dist1() {
        return Utils.abs(x) + Utils.abs(y) + Utils.abs(z);
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Taxicab_geometry">"taxicab" distance</a>
     * (aka. L1 distance or Manhattan distance) between this vector and the given other vector.
     */
    public long dist1(Vector other) {
        return other.minus(this).dist1();
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Chebyshev_distance">"maximum" distance</a>
     * (aka. L∞ distance or Chebyshev distance) between this vector and the {@link #ORIGIN} {@code (0,0,0)}.
     */
    public long distMax() {
        return Utils.max(Utils.abs(x), Utils.abs(y), Utils.abs(z));
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Chebyshev_distance">"maximum" distance</a>
     * (aka. L∞ distance or Chebyshev distance) between this vector and the given other vector.
     */
    public long distMax(Vector other) {
        return other.minus(this).distMax();
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance#Squared_Euclidean_distance">squared
     * Eucledian distance</a> between this vector and the {@link #ORIGIN} {@code (0,0,0)}.
     * <p>
     * Warning: this distance metric does not satisfy the triangle inequality.
     */
    public long distSq() {
        return x * x + y * y + z * z;
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance#Squared_Euclidean_distance">squared
     * Eucledian distance</a> between this vector and the given other vector.
     * <p>
     * Warning: this distance metric does not satisfy the triangle inequality.
     */
    public long distSq(Vector other) {
        return other.minus(this).distSq();
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Eucledian distance</a>
     * (aka. L2 distance) between this vector and the {@link #ORIGIN} {@code (0,0,0)}.
     */
    public double dist2() {
        return Math.sqrt(distSq());
    }

    /**
     * Returns the <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Eucledian distance</a>
     * (aka. L2 distance) between this vector and the given other vector.
     */
    public double dist2(Vector other) {
        return other.minus(this).dist2();
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Vector v && v.x == x && v.y == y && v.z == z);
    }

    @Override
    public int hashCode() {
        return (int) (2_641_879 * x + 1627 * y + z); // optimized for small integers
    }

    @Override
    public int compareTo(Vector other) {
        int cx = Long.compare(x, other.x);
        if (cx != 0) {
            return cx;
        }

        int cy = Long.compare(y, other.y);
        return cy != 0 ? cy : Long.compare(z, other.z);
    }

}
