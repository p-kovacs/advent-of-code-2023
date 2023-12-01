package com.github.pkovacs.util.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a table (or matrix) of {@code int} values with fixed number of rows and columns. This class is
 * essentially a wrapper for an {@code int[][]} array providing various convenient methods to access and modify
 * the data. A cell of the table is identified by a {@link Cell} object or two integer indices.
 * <p>
 * This class is the primitive type specialization of {@link Table} for {@code int}. Most methods are defined in
 * the {@link AbstractTable abstract base class}.
 * <p>
 * The {@code equals} and {@code hashCode} methods rely on deep equality check, and the {@code toString} method
 * provides a nicely formatted result, with the values aligned in columns appropriately, which can be useful for
 * debugging.
 * <p>
 * If your table is "sparse", consider using a {@code Map} with {@link Cell} keys (or Guava's {@code Table})
 * instead of this class.
 *
 * @see CharTable
 * @see Table
 */
public class IntTable extends AbstractTable<Integer> {

    private final int[][] data;

    /**
     * Creates a new table by wrapping the given {@code int[][]} array.
     * The array is used directly, so changes to it are reflected in the table and vice versa.
     * The "rows" of the given matrix must have the same length.
     */
    public IntTable(int[][] data) {
        if (IntStream.range(1, data.length).anyMatch(i -> data[i].length != data[0].length)) {
            throw new IllegalArgumentException("Rows must have the same length.");
        }
        this.data = data;
    }

    /**
     * Creates a new table with the given number of rows and columns, filled with zeros.
     */
    public IntTable(int rowCount, int colCount) {
        data = new int[rowCount][colCount];
    }

    /**
     * Creates a new table with the given number of rows and columns, filled with the given initial value.
     */
    public IntTable(int rowCount, int colCount, int initialValue) {
        this(rowCount, colCount);
        fill(initialValue);
    }

    /**
     * Creates a new table with the given number of rows and columns, and calculates initial values by applying
     * the given function to the indices of each cell.
     */
    public IntTable(int rowCount, int colCount, BiFunction<Integer, Integer, Integer> function) {
        this(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                data[i][j] = function.apply(i, j);
            }
        }
    }

    /**
     * Creates a new table as a deep copy of the given table.
     */
    public IntTable(IntTable other) {
        data = new int[other.data.length][];
        for (int i = 0; i < data.length; i++) {
            data[i] = other.data[i].clone();
        }
    }

    /**
     * Creates a new table by wrapping and shifting the given collection of positions. This method can be useful for
     * debugging.
     * <p>
     * The cells of the returned table correspond to the
     * <a href="https://en.wikipedia.org/wiki/Minimum_bounding_box">minimum bounding box</a> of the given positions
     * shifted appropriately so that the top left position of the bounding box becomes (0, 0). The cells corresponding
     * to the given positions are assigned the given {@code value}, while other cells are assigned the given
     * {@code fillValue}.
     */
    public static IntTable wrap(Collection<? extends Position> positions, int value, int fillValue) {
        return wrap(positions, p -> value, fillValue);
    }

    /**
     * Creates a new table by wrapping and shifting the given map with position keys. This method can be useful for
     * debugging.
     * <p>
     * The cells of the returned table correspond to the
     * <a href="https://en.wikipedia.org/wiki/Minimum_bounding_box">minimum bounding box</a> of the keys of the given
     * map shifted appropriately so that the top left position of the bounding box becomes (0, 0). The cells
     * corresponding to the map keys are assigned according to the map, while other cells are assigned the given
     * {@code fillValue}.
     */
    public static IntTable wrap(Map<? extends Position, Integer> map, int fillValue) {
        return wrap(map.keySet(), map::get, fillValue);
    }

    private static <T extends Position> IntTable wrap(Collection<T> positions, Function<T, Integer> function,
            int fillValue) {
        int minRow = positions.stream().mapToInt(Position::y).min().orElseThrow();
        int maxRow = positions.stream().mapToInt(Position::y).max().orElseThrow();
        int minCol = positions.stream().mapToInt(Position::x).min().orElseThrow();
        int maxCol = positions.stream().mapToInt(Position::x).max().orElseThrow();

        var table = new IntTable(maxRow - minRow + 1, maxCol - minCol + 1, fillValue);
        positions.forEach(p -> table.set(p.y() - minRow, p.x() - minCol, function.apply(p)));
        return table;
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
    Integer get0(int row, int col) {
        return data[row][col];
    }

    @Override
    void set0(int row, int col, Integer value) {
        data[row][col] = value;
    }

    @Override
    IntTable newInstance(int rowCount, int colCount, BiFunction<Integer, Integer, Integer> function) {
        return new IntTable(rowCount, colCount, function);
    }

    /**
     * Returns the {@code int[][]} array that backs this table. Changes to the returned array are reflected in the
     * table, and vice versa.
     */
    public int[][] asArray() {
        return data;
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public int get(int row, int col) {
        return data[row][col];
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public int get(Cell cell) {
        return data[cell.row()][cell.col()];
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(int row, int col, int value) {
        data[row][col] = value;
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(Cell cell, int value) {
        data[cell.row()][cell.col()] = value;
    }

    /**
     * Increments the value associated with the specified cell and returns the new value.
     */
    public int inc(int row, int col) {
        return ++data[row][col];
    }

    /**
     * Increments the value associated with the specified cell and returns the new value.
     */
    public int inc(Cell cell) {
        return ++data[cell.row()][cell.col()];
    }

    /**
     * Sets all values in this table to the given value.
     */
    public void fill(int value) {
        Arrays.stream(data).forEach(rowData -> Arrays.fill(rowData, value));
    }

    /**
     * Returns an ordered {@code IntStream} of the values contained in the specified row of this table.
     */
    public IntStream rowValues(int i) {
        return Arrays.stream(data[i]);
    }

    /**
     * Returns an ordered {@code IntStream} of the values contained in the specified column of this table.
     */
    public IntStream colValues(int j) {
        return IntStream.range(0, rowCount()).map(i -> data[i][j]);
    }

    /**
     * Returns an ordered {@link IntStream} of all values contained in this table (row by row).
     */
    public IntStream values() {
        return Arrays.stream(data).flatMapToInt(Arrays::stream);
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
