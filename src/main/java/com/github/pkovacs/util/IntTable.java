package com.github.pkovacs.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a table (or matrix) of {@code int} values with fixed number of columns and rows. This class is
 * essentially a wrapper for an {@code int[][]} array providing various convenient methods to access and modify
 * the data. A cell of the table is identified by a {@link Pos} object or two integer indices, and it has an
 * associated {@code int} value.
 * <p>
 * This class is the primitive type specialization of {@link Table} for {@code int}. Most methods are defined in
 * {@link AbstractTable}.
 * <p>
 * WARNING: in accordance with {@link Pos}, the cells are accessed by {@code (x,y)} indices, that is, in (column,row)
 * order, like the pixels of an image or screen. This is in contrast with the usual (row,column) indexing of matrices.
 * That is, if you create a table from a matrix {@code int[][] m}, then {@code get(x, y)} will return the element
 * {@code m[y][x]}. You should be aware of this when working with tables.
 * <p>
 * The {@code equals} and {@code hashCode} methods rely on deep equality check, and the {@code toString} method
 * provides a nicely formatted result, with the values aligned in columns appropriately, which can be useful for
 * debugging.
 * <p>
 * If your table is "sparse", consider using a {@code Map} with {@link Pos} keys (or Guava's {@code Table})
 * instead of this class.
 *
 * @see CharTable
 * @see Table
 */
public final class IntTable extends AbstractTable<Integer> {

    private final int[][] data;

    /**
     * Creates a new table as a deep copy of the given {@code int[][]} matrix.
     * The "rows" of the given matrix must have the same length.
     */
    public IntTable(int[][] data) {
        if (Arrays.stream(data).anyMatch(row -> row.length != data[0].length)) {
            throw new IllegalArgumentException("Rows must have the same length.");
        }

        this.data = Utils.deepCopyOf(data);
    }

    /**
     * Creates a new table as a deep copy of the given other table.
     */
    public IntTable(IntTable other) {
        this(other.data);
    }

    /**
     * Creates a new table with the given width and height, filled with zeros.
     */
    public IntTable(int width, int height) {
        data = new int[height][width];
    }

    /**
     * Creates a new table with the given width and height, filled with the given initial value.
     */
    public IntTable(int width, int height, int initialValue) {
        data = new int[height][width];
        fill(initialValue);
    }

    /**
     * Creates a new table with the given width and height, and calculates initial values by applying the given
     * function to each cell.
     */
    public IntTable(int width, int height, Function<Pos, Integer> function) {
        this(width, height, (x, y) -> function.apply(new Pos(x, y)));
    }

    /**
     * Private constructor used for transformations like rotation and mirroring.
     */
    private IntTable(int width, int height, BiFunction<Integer, Integer, Integer> function) {
        data = new int[height][width];
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
    public static IntTable wrap(Collection<Pos> positions, int value, int fillValue) {
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
    public static IntTable wrap(Map<Pos, Integer> map, int fillValue) {
        return wrap(map.keySet(), map::get, fillValue);
    }

    private static IntTable wrap(Collection<Pos> positions, Function<Pos, Integer> function, int fillValue) {
        var box = Box.bound(positions);
        long minX = box.x().min;
        long minY = box.y().min;

        var table = new IntTable((int) box.x().size(), (int) box.y().size(), fillValue);
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
    Integer get0(int x, int y) {
        return data[y][x];
    }

    @Override
    void set0(int x, int y, Integer value) {
        data[y][x] = value;
    }

    @Override
    IntTable newInstance(int width, int height, BiFunction<Integer, Integer, Integer> function) {
        return new IntTable(width, height, function);
    }

    /**
     * Returns the {@code int[][]} matrix that backs this table. Changes to the returned matrix are reflected in the
     * table, and vice versa.
     */
    public int[][] asMatrix() {
        return data;
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public int get(Pos pos) {
        return data[(int) pos.y][(int) pos.x];
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public int get(int x, int y) {
        return data[y][x];
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public int get(long x, long y) {
        return get((int) x, (int) y);
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(Pos pos, int value) {
        data[(int) pos.y][(int) pos.x] = value;
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(int x, int y, int value) {
        data[y][x] = value;
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(long x, long y, int value) {
        set((int) x, (int) y, value);
    }

    /**
     * Increments the value associated with the specified cell and returns the new value.
     */
    public int inc(Pos pos) {
        return ++data[(int) pos.y][(int) pos.x];
    }

    /**
     * Sets all values in this table to the given value.
     */
    public void fill(int value) {
        Arrays.stream(data).forEach(row -> Arrays.fill(row, value));
    }

    /**
     * Returns an ordered {@link IntStream} of all values contained in this table (row by row).
     */
    public IntStream values() {
        return Arrays.stream(data).flatMapToInt(Arrays::stream);
    }

    /**
     * Returns an ordered {@code IntStream} of the values contained in the specified column of this table.
     */
    public IntStream colValues(int x) {
        return IntStream.range(0, height()).map(y -> data[y][x]);
    }

    /**
     * Returns an ordered {@code IntStream} of the values contained in the specified row of this table.
     */
    public IntStream rowValues(int y) {
        return Arrays.stream(data[y]);
    }

    /**
     * Returns the minimum of the values contained in this table.
     */
    public int min() {
        return values().min().orElseThrow();
    }

    /**
     * Returns the maximum of the values contained in this table.
     */
    public int max() {
        return values().max().orElseThrow();
    }

    /**
     * Returns the sum of the values contained in this table.
     */
    public long sum() {
        return values().mapToLong(i -> i).sum();
    }

    /**
     * Returns the count of the given value among all values contained in this table.
     */
    public int count(int value) {
        return (int) values().filter(v -> v == value).count();
    }

    @Override
    public IntTable mirrorHorizontally() {
        return (IntTable) super.mirrorHorizontally();
    }

    @Override
    public IntTable mirrorVertically() {
        return (IntTable) super.mirrorVertically();
    }

    @Override
    public IntTable rotateRight() {
        return (IntTable) super.rotateRight();
    }

    @Override
    public IntTable rotateLeft() {
        return (IntTable) super.rotateLeft();
    }

    @Override
    public IntTable transpose() {
        return (IntTable) super.transpose();
    }

    @Override
    public IntTable extend(int delta, Integer fillValue) {
        return (IntTable) super.extend(delta, fillValue);
    }

    @Override
    public IntTable extend(int dx, int dy, Integer fillValue) {
        return (IntTable) super.extend(dx, dy, fillValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return Arrays.deepEquals(data, ((IntTable) obj).data);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(data);
    }

    @Override
    public String toString() {
        int digits = Math.max(String.valueOf(min()).length(), String.valueOf(max()).length());
        var format = "%" + digits + "d";

        return Arrays.stream(data)
                .map(rowData -> Arrays.stream(rowData)
                        .mapToObj(v -> String.format(format, v))
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n")) + "\n";
    }

}
