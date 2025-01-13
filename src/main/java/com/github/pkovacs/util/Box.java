package com.github.pkovacs.util;

import java.util.Collection;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * An immutable box (rectangle) of {@link Pos} objects in 2D coordinate space. It is represented as two
 * {@link Range}s for x and y coordinates, respectively. Provides various useful methods and supports ordering
 * (first by {@link #x()}, then by {@link #y()}).
 *
 * @see Pos
 * @see Range
 * @see VectorBox
 */
public record Box(Range x, Range y) implements Comparable<Box> {

    /**
     * Constructs a new box with the given coordinate ranges {@code x} and {@code y}. It contains each position
     * {@code p} for which both {@code x.contains(p.x)} and {@code y.contains(p.y)} hold.
     */
    public Box {
    }

    /**
     * Constructs a new box between the given two positions {@code min} and {@code max}. It contains each position
     * {@code p} for which both {@code min.x <= p.x <= max.x} and {@code min.y <= p.y <= max.y} hold.
     */
    public Box(Pos min, Pos max) {
        this(new Range(min.x, max.x), new Range(min.y, max.y));
    }

    /**
     * Constructs a new box with the given width and height. It contains each position {@code p} for which both
     * {@code 0 <= p.x < width} and {@code 0 <= p.y < height} hold.
     */
    public Box(int width, int height) {
        this(Range.closedOpen(0, width), Range.closedOpen(0, height));
    }

    /**
     * Constructs the axis-aligned <a href="https://en.wikipedia.org/wiki/Minimum_bounding_box">minimum bounding box</a>
     * of the given positions.
     *
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    public static Box bound(Collection<Pos> positions) {
        return new Box(
                Range.bound(positions.stream().mapToLong(Pos::x)),
                Range.bound(positions.stream().mapToLong(Pos::y))
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
     * Returns the minimum position of this box: {@code (x.min,y.min)}.
     */
    public Pos min() {
        return new Pos(x.min, y.min);
    }

    /**
     * Returns the maximum position of this box: {@code (x.max,y.max)}.
     */
    public Pos max() {
        return new Pos(x.max, y.max);
    }

    /**
     * Returns true if this box is empty.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns true if this box is non-empty.
     */
    public boolean isNonEmpty() {
        return size() > 0;
    }

    /**
     * Returns the number of positions within this box.
     */
    public long size() {
        return x.size() * y.size();
    }

    /**
     * Returns true if this box contains the given position.
     */
    public boolean contains(Pos p) {
        return x.contains(p.x) && y.contains(p.y);
    }

    /**
     * Returns true if this box contains all positions of the given other box.
     */
    public boolean containsAll(Box other) {
        return x.containsAll(other.x) && y.containsAll(other.y);
    }

    /**
     * Returns true if this box contains all the given positions.
     */
    public boolean containsAll(Collection<Pos> positions) {
        return positions.stream().allMatch(this::contains);
    }

    /**
     * Returns true if this box overlaps with the given other box (they have a non-empty intersection).
     */
    public boolean overlaps(Box other) {
        return intersection(other).isNonEmpty();
    }

    /**
     * Returns the intersection of this box and the given other box.
     */
    public Box intersection(Box other) {
        return new Box(x.intersection(other.x), y.intersection(other.y));
    }

    /**
     * Returns the minimal box that contains all elements of both this box and the given other box.
     */
    public Box span(Box other) {
        return new Box(x.span(other.x), y.span(other.y));
    }

    /**
     * Returns a new box by shifting this box with the given position vector.
     */
    public Box shift(Pos delta) {
        return shift(delta.x, delta.y);
    }

    /**
     * Returns a new box by shifting this box with the given delta values along the corresponding axes.
     */
    public Box shift(long dx, long dy) {
        return new Box(x.shift(dx), y.shift(dy));
    }

    /**
     * Returns a new box by extending this box with the given amount uniformly in all directions.
     * Negative parameter value means shrinking.
     */
    public Box extend(long delta) {
        return extend(delta, delta);
    }

    /**
     * Returns a new box by extending this box with the given amount along the corresponding axes (in both directions).
     * Negative parameter value means shrinking.
     */
    public Box extend(long dx, long dy) {
        return new Box(x.extend(dx), y.extend(dy));
    }

    /**
     * Returns a lexicographically sorted stream of the positions within this box. If the box is non-empty, then
     * the first element of the stream is {@link #min()}, and the last element is {@link #max()}. Otherwise, an empty
     * stream is returned. The elements are generated on-the-fly as the stream is processed.
     */
    public Stream<Pos> stream() {
        if (isEmpty()) {
            return Stream.empty();
        }

        long size = size();
        long height = y.size();
        return LongStream.range(0, size).mapToObj(i -> new Pos(x.min + i / height, y.min + i % height));
    }

    /**
     * Returns a lexicographically sorted list of the positions within this box. If the box is non-empty, then
     * the first element of the list is {@link #min()}, and the last element is {@link #max()}. Otherwise, an empty
     * list is returned.
     */
    public List<Pos> toList() {
        return stream().toList();
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public int compareTo(Box other) {
        int c = x.compareTo(other.x);
        return c != 0 ? c : y.compareTo(other.y);
    }

}
