package com.github.pkovacs.util;

import java.util.Collection;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * An immutable box (hyperrectangle or right prism) of {@link Vector} objects in 3D coordinate space. It is
 * represented as three {@link Range}s for x, y, and z coordinates, respectively. Provides various useful methods
 * and supports ordering (first by {@link #x()}, then by {@link #y()}, finally by {@link #z()}).
 * <p>
 * This class is the 3D counterpart of {@link Box}.
 *
 * @see Vector
 * @see Range
 * @see Box
 */
public record VectorBox(Range x, Range y, Range z) implements Comparable<VectorBox> {

    /**
     * Constructs a new vector box with the given coordinate ranges {@code x}, {@code y}, and {@code z}. It contains
     * each vector {@code v} for which {@code x.contains(v.x)}, {@code y.contains(v.y)}, and {@code z.contains(v.z)}
     * all hold.
     */
    public VectorBox {
    }

    /**
     * Constructs a new vector box between the given two vectors {@code min} and {@code max}. It contains each
     * vector {@code v} for which {@code min.x <= v.x <= max.x}, {@code min.y <= v.y <= max.y}, and
     * {@code min.z <= v.z <= max.z} all hold.
     */
    public VectorBox(Vector min, Vector max) {
        this(new Range(min.x, max.x), new Range(min.y, max.y), new Range(min.z, max.z));
    }

    /**
     * Constructs the axis-aligned <a href="https://en.wikipedia.org/wiki/Minimum_bounding_box">minimum bounding box</a>
     * of the given vectors.
     *
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    public static VectorBox bound(Collection<Vector> vectors) {
        return new VectorBox(
                Range.bound(vectors.stream().mapToLong(Vector::x)),
                Range.bound(vectors.stream().mapToLong(Vector::y)),
                Range.bound(vectors.stream().mapToLong(Vector::z))
        );
    }

    /**
     * Returns the x coordinate range of this box.
     */
    public Range x() {
        return x;
    }

    /**
     * Returns the y coordinate range of this box.
     */
    public Range y() {
        return y;
    }

    /**
     * Returns the z coordinate range of this box.
     */
    public Range z() {
        return z;
    }

    /**
     * Returns the minimum vector of this box: {@code (x.min,y.min,z.min)}.
     */
    public Vector min() {
        return new Vector(x.min, y.min, z.min);
    }

    /**
     * Returns the maximum vector of this box: {@code (x.max,y.max,z.max)}.
     */
    public Vector max() {
        return new Vector(x.max, y.max, z.max);
    }

    /**
     * Returns true if this vector box is empty.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns true if this vector box is non-empty.
     */
    public boolean isNonEmpty() {
        return size() > 0;
    }

    /**
     * Returns the number of vectors within this vector box.
     */
    public long size() {
        return x.size() * y.size() * z.size();
    }

    /**
     * Returns true if this vector box contains the given vector.
     */
    public boolean contains(Vector v) {
        return x.contains(v.x) && y.contains(v.y) && z.contains(v.z);
    }

    /**
     * Returns true if this vector box contains all vectors of the given other vector box.
     */
    public boolean containsAll(VectorBox other) {
        return x.containsAll(other.x) && y.containsAll(other.y) && z.containsAll(other.z);
    }

    /**
     * Returns true if this vector box contains all the given vectors.
     */
    public boolean containsAll(Collection<Vector> vectors) {
        return vectors.stream().allMatch(this::contains);
    }

    /**
     * Returns true if this vector box overlaps with the given other vector box (they have a non-empty intersection).
     */
    public boolean overlaps(VectorBox other) {
        return intersection(other).isNonEmpty();
    }

    /**
     * Returns the intersection of this vector box and the given other vector box.
     */
    public VectorBox intersection(VectorBox other) {
        return new VectorBox(x.intersection(other.x), y.intersection(other.y), z.intersection(other.z));
    }

    /**
     * Returns the minimal vector box that contains all elements of both this vector box and the given other
     * vector box.
     */
    public VectorBox span(VectorBox other) {
        return new VectorBox(x.span(other.x), y.span(other.y), z.span(other.z));
    }

    /**
     * Returns a new vector box by shifting this one with the given vector.
     */
    public VectorBox shift(Vector delta) {
        return shift(delta.x, delta.y, delta.z);
    }

    /**
     * Returns a new vector box by shifting this one with the given delta values along the corresponding axes.
     */
    public VectorBox shift(long dx, long dy, long dz) {
        return new VectorBox(x.shift(dx), y.shift(dy), z.shift(dz));
    }

    /**
     * Returns a new vector box by extending this one with the given amount uniformly in all directions.
     * Negative parameter value means shrinking.
     */
    public VectorBox extend(long delta) {
        return extend(delta, delta, delta);
    }

    /**
     * Returns a new vector box by extending this one with the given amount along the corresponding axes
     * (in both directions). Negative parameter value means shrinking.
     */
    public VectorBox extend(long dx, long dy, long dz) {
        return new VectorBox(x.extend(dx), y.extend(dy), z.extend(dz));
    }

    /**
     * Returns a lexicographically sorted stream of the vectors within this vector box. If the box is non-empty, then
     * the first element of the stream is {@link #min()}, and the last element is {@link #max()}. Otherwise, an empty
     * stream is returned. The elements are generated on-the-fly as the stream is processed.
     */
    public Stream<Vector> stream() {
        if (isEmpty()) {
            return Stream.empty();
        }

        long size = size();
        long ys = y.size();
        long zs = z.size();
        return LongStream.range(0, size)
                .mapToObj(i -> new Vector(x.min + (i / zs) / ys, y.min + (i / zs) % ys, z.min + i % zs));
    }

    /**
     * Returns a lexicographically sorted list of the vectors within this vector box. If the box is non-empty, then
     * the first element of the list is {@link #min()}, and the last element is {@link #max()}. Otherwise, an empty
     * list is returned.
     */
    public List<Vector> toList() {
        return stream().toList();
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

    @Override
    public int compareTo(VectorBox other) {
        int cx = x.compareTo(other.x);
        if (cx != 0) {
            return cx;
        }

        int cy = y.compareTo(other.y);
        return cy != 0 ? cy : z.compareTo(other.z);
    }

}
