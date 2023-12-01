package com.github.pkovacs.util.data;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An immutable box (hyperrectangle) of D-dimensional {@link Vector}s (rectangle in 2D, right prism in 3D).
 * Its bounds are represented by two vectors {@code min} and {@code max}, and a vector {@code v} is contained in
 * the box if and only if for each dimension {@code k}, {@code min.get(k) <= v.get(k) <= max.get(k)}.
 * <p>
 * This class is the D-dimensional generalization of {@link Range}.
 *
 * @see Vector
 * @see Range
 */
public record Box(Vector min, Vector max) {

    /**
     * Constructs a new box {@code [min..max]}.
     *
     * @throws IllegalArgumentException if the given vectors have different dimensions
     */
    public Box {
        if (min.dim() != max.dim()) {
            throw new IllegalArgumentException("The vectors have different dimensions.");
        }
    }

    /**
     * Constructs the axis-aligned <a href="https://en.wikipedia.org/wiki/Minimum_bounding_box">minimum bounding box</a>
     * of the given vectors.
     *
     * @throws IllegalArgumentException if the given vectors have different dimensions
     * @throws NoSuchElementException if the collection is empty
     */
    public static Box bound(Collection<Vector> vectors) {
        return new Box(bound(vectors, Math::min), bound(vectors, Math::max));
    }

    /**
     * Returns the dimension of this box.
     */
    public int dim() {
        return min.dim();
    }

    /**
     * Returns true if this box is empty.
     */
    public boolean isEmpty() {
        return count() == 0;
    }

    /**
     * Returns the number of vectors within this box.
     */
    public long count() {
        return IntStream.range(0, dim())
                .mapToLong(k -> Math.max(max.get(k) - min.get(k) + 1, 0))
                .reduce(1, (x, y) -> x * y);
    }

    /**
     * Returns true if this box contains the given vector.
     */
    public boolean contains(Vector v) {
        if (v.dim() != dim()) {
            throw new IllegalArgumentException("The box and the vector have different dimensions.");
        }
        return IntStream.range(0, dim()).allMatch(k -> v.get(k) >= min.get(k) && v.get(k) <= max.get(k));
    }

    /**
     * Returns true if this box contains the given box.
     */
    public boolean containsAll(Box other) {
        return intersection(other).equals(other);
    }

    /**
     * Returns true if this box overlaps with the given box.
     */
    public boolean overlaps(Box other) {
        return !intersection(other).isEmpty();
    }

    /**
     * Returns the intersection of this box and the given box.
     */
    public Box intersection(Box other) {
        if (other.dim() != dim()) {
            throw new IllegalArgumentException("The boxes have different dimensions.");
        }

        long[] lower = IntStream.range(0, dim()).mapToLong(k -> Math.max(min.get(k), other.min.get(k))).toArray();
        long[] upper = IntStream.range(0, dim()).mapToLong(k -> Math.min(max.get(k), other.max.get(k))).toArray();
        return new Box(new Vector(lower), new Vector(upper));
    }

    /**
     * Returns an ordered stream of the vectors within this box. If the box is not empty, then the first element is
     * {@code min}, the last element is {@code max}, and the stream is lexicographically sorted.
     * <p>
     * Warning: this method eagerly constructs all elements of the stream, so be careful with large boxes.
     */
    public Stream<Vector> stream() {
        return Vector.box(min, max);
    }

    @Override
    public String toString() {
        return "[" + min + " .. " + max + "]";
    }

    private static Vector bound(Collection<Vector> vectors, BinaryOperator<Long> op) {
        var first = vectors.iterator().next();
        if (vectors.stream().anyMatch(v -> v.dim() != first.dim())) {
            throw new IllegalArgumentException("The vectors have different dimensions.");
        }

        long[] coords = IntStream.range(0, first.dim()).mapToLong(first::get).toArray();
        for (var v : vectors) {
            for (int k = 0; k < first.dim(); k++) {
                coords[k] = op.apply(coords[k], v.get(k));
            }
        }
        return new Vector(coords);
    }

}
