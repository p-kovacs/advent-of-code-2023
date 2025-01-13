package com.github.pkovacs.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a table (or matrix) of {@code char} values with fixed number of columns and rows. This class is
 * essentially a wrapper for a {@code char[][]} array providing various convenient methods to access and modify
 * the data. A cell of the table is identified by a {@link Pos} object or two integer indices, and it has an
 * associated {@code char} value.
 * <p>
 * This class is the primitive type specialization of {@link Table} for {@code char}. Most methods are defined in
 * {@link AbstractTable}.
 * <p>
 * WARNING: in accordance with {@link Pos}, the cells are accessed by {@code (x,y)} indices, that is, in (column,row)
 * order, like the pixels of an image or screen. This is in contrast with the usual (row,column) indexing of matrices.
 * That is, if you create a table from a matrix {@code char[][] m}, then {@code get(x, y)} will return the element
 * {@code m[y][x]}. You should be aware of this when working with tables.
 * <p>
 * The {@code equals} and {@code hashCode} methods rely on deep equality check, and the {@code toString} method
 * provides a nicely formatted compact result, which can be useful for debugging.
 * <p>
 * If your table is "sparse", consider using a {@code Map} with {@link Pos} keys (or Guava's {@code Table})
 * instead of this class.
 *
 * @see IntTable
 * @see Table
 */
public final class CharTable extends AbstractTable<Character> {

    private final char[][] data;

    /**
     * Creates a new table as a deep copy of the given {@code char[][]} matrix.
     * The "rows" of the given matrix must have the same length.
     */
    public CharTable(char[][] data) {
        if (Arrays.stream(data).anyMatch(row -> row.length != data[0].length)) {
            throw new IllegalArgumentException("Rows must have the same length.");
        }

        this.data = Utils.deepCopyOf(data);
    }

    /**
     * Creates a new table as a deep copy of the given other table.
     */
    public CharTable(CharTable other) {
        this(other.data);
    }

    /**
     * Creates a new table from a list of strings. The rows of the returned table represent the strings in the list,
     * which must have the same length.
     */
    public CharTable(List<String> data) {
        if (data.stream().anyMatch(row -> row.length() != data.getFirst().length())) {
            throw new IllegalArgumentException("Rows must have the same length.");
        }

        this.data = data.stream().map(String::toCharArray).toArray(char[][]::new);
    }

    /**
     * Creates a new table with the given width and height, filled with the given initial value.
     */
    public CharTable(int width, int height, char initialValue) {
        data = new char[height][width];
        fill(initialValue);
    }

    /**
     * Creates a new table with the given width and height, and calculates initial values by applying the given
     * function to each cell.
     */
    public CharTable(int width, int height, Function<Pos, Character> function) {
        this(width, height, (x, y) -> function.apply(new Pos(x, y)));
    }

    /**
     * Private constructor used for transformations like rotation and mirroring.
     */
    public CharTable(int width, int height, BiFunction<Integer, Integer, Character> function) {
        data = new char[height][width];
        for (int y = 0; y < height; y++) {
            var row = data[y];
            for (int x = 0; x < width; x++) {
                row[x] = function.apply(x, y);
            }
        }
    }

    /**
     * Creates a new table by wrapping and shifting the given collection of {@link Pos} objects.
     * This method can be useful for debugging.
     * <p>
     * The cells of the returned table correspond to the
     * <a href="https://en.wikipedia.org/wiki/Minimum_bounding_box">minimum bounding box</a> of the given positions
     * shifted appropriately so that the top left position of the bounding box becomes {@code (0,0)}. The cells
     * corresponding to the given positions are assigned the given {@code value}, while other cells are assigned
     * the given {@code fillValue}.
     */
    public static CharTable wrap(Collection<Pos> positions, char value, char fillValue) {
        return wrap(positions, p -> value, fillValue);
    }

    /**
     * Creates a new table by wrapping and shifting the given map with {@link Pos} keys.
     * This method can be useful for debugging.
     * <p>
     * The cells of the returned table correspond to the
     * <a href="https://en.wikipedia.org/wiki/Minimum_bounding_box">minimum bounding box</a> of the keys of the given
     * map shifted appropriately so that the top left position of the bounding box becomes {@code (0,0)}. The cells
     * corresponding to the map keys are assigned according to the map, while other cells are assigned the given
     * {@code fillValue}.
     */
    public static CharTable wrap(Map<Pos, Character> map, char fillValue) {
        return wrap(map.keySet(), map::get, fillValue);
    }

    private static CharTable wrap(Collection<Pos> positions, Function<Pos, Character> function, char fillValue) {
        var box = Box.bound(positions);
        long minX = box.x().min;
        long minY = box.y().min;

        var table = new CharTable((int) box.x().size(), (int) box.y().size(), fillValue);
        positions.forEach(p -> table.set(p.x - minX, p.y - minY, function.apply(p)));
        return table;
    }

    @Override
    public int width() {
        return (data.length == 0) ? 0 : data[0].length;
    }

    @Override
    public int height() {
        return data.length;
    }

    @Override
    Character get0(int x, int y) {
        return data[y][x];
    }

    @Override
    void set0(int x, int y, Character value) {
        data[y][x] = value;
    }

    @Override
    CharTable newInstance(int width, int height, BiFunction<Integer, Integer, Character> function) {
        return new CharTable(width, height, function);
    }

    /**
     * Returns the {@code char[][]} matrix that backs this table. Changes to the returned matrix are reflected in the
     * table, and vice versa.
     */
    public char[][] asMatrix() {
        return data;
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public char get(Pos pos) {
        return data[(int) pos.y][(int) pos.x];
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public char get(int x, int y) {
        return data[y][x];
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public char get(long x, long y) {
        return get((int) x, (int) y);
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(Pos pos, char value) {
        data[(int) pos.y][(int) pos.x] = value;
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(int x, int y, char value) {
        data[y][x] = value;
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(long x, long y, char value) {
        set((int) x, (int) y, value);
    }

    /**
     * Sets all values in this table to the given value.
     */
    public void fill(char value) {
        Arrays.stream(data).forEach(row -> Arrays.fill(row, value));
    }

    /**
     * Returns an ordered stream of all values contained in this table (row by row).
     */
    public Stream<Character> values() {
        return Arrays.stream(data).flatMap(Utils::streamOf);
    }

    /**
     * Returns an ordered stream of the values contained in the specified column of this table.
     */
    public Stream<Character> colValues(int x) {
        return IntStream.range(0, height()).mapToObj(y -> data[y][x]);
    }

    /**
     * Returns an ordered stream of the values contained in the specified row of this table.
     */
    public Stream<Character> rowValues(int y) {
        return Utils.streamOf(data[y]);
    }

    /**
     * Returns the count of the given value among all values contained in this table.
     */
    public int count(char value) {
        return (int) values().filter(v -> v == value).count();
    }

    @Override
    public CharTable mirrorHorizontally() {
        return (CharTable) super.mirrorHorizontally();
    }

    @Override
    public CharTable mirrorVertically() {
        return (CharTable) super.mirrorVertically();
    }

    @Override
    public CharTable rotateRight() {
        return (CharTable) super.rotateRight();
    }

    @Override
    public CharTable rotateLeft() {
        return (CharTable) super.rotateLeft();
    }

    @Override
    public CharTable transpose() {
        return (CharTable) super.transpose();
    }

    @Override
    public CharTable extend(int delta, Character fillValue) {
        return (CharTable) super.extend(delta, fillValue);
    }

    @Override
    public CharTable extend(int dx, int dy, Character fillValue) {
        return (CharTable) super.extend(dx, dy, fillValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return Arrays.deepEquals(data, ((CharTable) obj).data);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(data);
    }

    @Override
    public String toString() {
        return Arrays.stream(data)
                .map(String::new)
                .collect(Collectors.joining("\n")) + "\n";
    }

}
