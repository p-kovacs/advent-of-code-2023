package com.github.pkovacs.util.data;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a table (or matrix) with fixed number of rows and columns. This class is essentially a wrapper for a
 * {@code V[][]} array providing various convenient methods to access and modify the data. A cell of the table
 * is identified by a {@link Cell} object or two integer indices. Most methods of this class are defined in the
 * {@link AbstractTable abstract base class}.
 * <p>
 * The {@code equals} and {@code hashCode} methods rely on deep equality check, and the {@code toString} method
 * provides a formatted result, which can be useful for debugging.
 * <p>
 * For storing a table of {@code int} or {@code char} values, use {@link IntTable} or {@link CharTable} instead of
 * this general class. Furthermore, if your table is "sparse", consider using a {@code Map} with {@link Cell} or
 * {@link Point} keys instead (or Guava's {@code Table} class).
 *
 * @param <V> the type of the values associated with the cells of this table
 * @see IntTable
 * @see CharTable
 */
public class Table<V> extends AbstractTable<V> {

    private final Object[][] data;

    /**
     * Creates a new table by wrapping the given {@code V[][]} array.
     * The array is used directly, so changes to it are reflected in the table and vice versa.
     * The "rows" of the given matrix must have the same length.
     */
    public Table(V[][] data) {
        if (IntStream.range(1, data.length).anyMatch(i -> data[i].length != data[0].length)) {
            throw new IllegalArgumentException("Rows must have the same length.");
        }
        this.data = data;
    }

    /**
     * Creates a new table with the given number of rows and columns.
     * The initial value for each cell is {@code null}.
     */
    public Table(int rowCount, int colCount) {
        data = new Object[rowCount][colCount];
    }

    /**
     * Creates a new table with the given number of rows and columns, and calculates initial values by applying
     * the given function to the indices of each cell.
     */
    public Table(int rowCount, int colCount, BiFunction<Integer, Integer, ? extends V> function) {
        data = new Object[rowCount][colCount];
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                data[i][j] = function.apply(i, j);
            }
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

    @Override
    public int rowCount() {
        return data.length;
    }

    @Override
    public int colCount() {
        return data.length == 0 ? 0 : data[0].length;
    }

    @Override
    @SuppressWarnings("unchecked")
    V get0(int row, int col) {
        return (V) data[row][col];
    }

    @Override
    void set0(int row, int col, V value) {
        data[row][col] = value;
    }

    @Override
    Table<V> newInstance(int rowCount, int colCount, BiFunction<Integer, Integer, V> function) {
        return new Table<V>(rowCount, colCount, function);
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public V get(int row, int col) {
        return get0(row, col);
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public V get(Cell cell) {
        return get0(cell.row(), cell.col());
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(int row, int col, V value) {
        data[row][col] = value;
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(Cell cell, V value) {
        data[cell.row()][cell.col()] = value;
    }

    /**
     * Sets all values in this table to the given value.
     */
    public void fill(V value) {
        Arrays.stream(data).forEach(rowData -> Arrays.fill(rowData, value));
    }

    /**
     * Returns an ordered stream of the values contained in the specified row of this table.
     */
    public Stream<V> rowValues(int i) {
        return row(i).map(this::get);
    }

    /**
     * Returns an ordered stream of the values contained in the specified column of this table.
     */
    public Stream<V> colValues(int j) {
        return col(j).map(this::get);
    }

    /**
     * Returns an ordered stream of all values contained in this table (row by row).
     */
    public Stream<V> values() {
        return cells().map(this::get);
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
