package com.github.pkovacs.util;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a table (or matrix) with fixed number of columns and rows. This class is essentially a wrapper for a
 * {@code V[][]} array providing various convenient methods to access and modify the data. A cell of the table
 * is identified by a {@link Pos} object or two integer indices, and it has an associated value.
 * Most methods of this class are defined in {@link AbstractTable}.
 * <p>
 * WARNING: in accordance with {@link Pos}, the cells are accessed by {@code (x,y)} indices, that is, in (column,row)
 * order, like the pixels of an image or screen. This is in contrast with the usual (row,column) indexing of matrices.
 * That is, if you create a table from a matrix {@code V[][] m}, then {@code get(x, y)} will return the element
 * {@code m[y][x]}. You should be aware of this when working with tables.
 * <p>
 * The {@code equals} and {@code hashCode} methods rely on deep equality check, and the {@code toString} method
 * provides a formatted result, which can be useful for debugging.
 * <p>
 * For storing a table of {@code int} or {@code char} values, use {@link IntTable} or {@link CharTable}, respectively,
 * instead of this general class. Furthermore, if your table is "sparse", consider using a {@code Map} with
 * {@link Pos} keys instead (or Guava's {@code Table} class).
 *
 * @param <V> the type of the values associated with the cells of this table
 * @see IntTable
 * @see CharTable
 */
public final class Table<V> extends AbstractTable<V> {

    private final Object[][] data;

    /**
     * Creates a new table as a deep copy of the given {@code V[][]} matrix.
     * The "rows" of the given matrix must have the same length.
     */
    public Table(V[][] data) {
        if (Arrays.stream(data).anyMatch(row -> row.length != data[0].length)) {
            throw new IllegalArgumentException("Rows must have the same length.");
        }

        this.data = new Object[data.length][];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i].clone();
        }
    }

    /**
     * Creates a new table as a deep copy of the given table.
     */
    public Table(Table<? extends V> other) {
        data = new Object[other.data.length][];
        for (int i = 0; i < data.length; i++) {
            data[i] = other.data[i].clone();
        }
    }

    /**
     * Creates a new table with the given width and height, filled with {@code null} values.
     */
    public Table(int width, int height) {
        data = new Object[height][width];
    }

    /**
     * Creates a new table with the given width and height, and calculates initial values by applying the given
     * function to each cell.
     */
    public Table(int width, int height, Function<Pos, ? extends V> function) {
        this(width, height, (x, y) -> function.apply(new Pos(x, y)));
    }

    /**
     * Private constructor used for transformations like rotation and mirroring.
     */
    private Table(int width, int height, BiFunction<Integer, Integer, ? extends V> function) {
        data = new Object[height][width];
        for (int y = 0; y < height; y++) {
            var row = data[y];
            for (int x = 0; x < width; x++) {
                row[x] = function.apply(x, y);
            }
        }
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
    @SuppressWarnings("unchecked")
    V get0(int x, int y) {
        return (V) data[y][x];
    }

    @Override
    void set0(int x, int y, V value) {
        data[y][x] = value;
    }

    @Override
    Table<V> newInstance(int width, int height, BiFunction<Integer, Integer, V> function) {
        return new Table<>(width, height, function);
    }

    /**
     * Returns the value associated with the specified cell.
     */
    @SuppressWarnings("unchecked")
    public V get(Pos pos) {
        return (V) data[(int) pos.y][(int) pos.x];
    }

    /**
     * Returns the value associated with the specified cell.
     */
    @SuppressWarnings("unchecked")
    public V get(int x, int y) {
        return (V) data[y][x];
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public V get(long x, long y) {
        return get((int) x, (int) y);
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(Pos pos, V value) {
        data[(int) pos.y][(int) pos.x] = value;
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(int x, int y, V value) {
        data[y][x] = value;
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(long x, long y, V value) {
        set((int) x, (int) y, value);
    }

    /**
     * Sets all values in this table to the given value.
     */
    public void fill(V value) {
        Arrays.stream(data).forEach(row -> Arrays.fill(row, value));
    }

    /**
     * Returns an ordered stream of all values contained in this table (row by row).
     */
    public Stream<V> values() {
        return cells().map(this::get);
    }

    /**
     * Returns an ordered stream of the values contained in the specified column of this table.
     */
    public Stream<V> colValues(int x) {
        return col(x).map(this::get);
    }

    /**
     * Returns an ordered stream of the values contained in the specified row of this table.
     */
    public Stream<V> rowValues(int y) {
        return row(y).map(this::get);
    }

    @Override
    public Table<V> mirrorHorizontally() {
        return (Table<V>) super.mirrorHorizontally();
    }

    @Override
    public Table<V> mirrorVertically() {
        return (Table<V>) super.mirrorVertically();
    }

    @Override
    public Table<V> rotateRight() {
        return (Table<V>) super.rotateRight();
    }

    @Override
    public Table<V> rotateLeft() {
        return (Table<V>) super.rotateLeft();
    }

    @Override
    public Table<V> transpose() {
        return (Table<V>) super.transpose();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return Arrays.deepEquals(data, ((Table<?>) obj).data);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(data);
    }

    @Override
    public String toString() {
        return Arrays.stream(data)
                .map(rowData -> Arrays.stream(rowData)
                        .map(String::valueOf)
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n")) + "\n";
    }

}
