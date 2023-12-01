package com.github.pkovacs.util.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.pkovacs.util.Utils;

/**
 * Represents a table (or matrix) of {@code char} values with fixed number of rows and columns. This class is
 * essentially a wrapper for a {@code char[][]} array providing various convenient methods to access and modify
 * the data. A cell of the table is identified by a {@link Cell} object or two integer indices.
 * <p>
 * This class is the primitive type specialization of {@link Table} for {@code char}. Most methods are defined in
 * the {@link AbstractTable abstract base class}.
 * <p>
 * The {@code equals} and {@code hashCode} methods rely on deep equality check, and the {@code toString} method
 * provides a nicely formatted compact result, which can be useful for debugging.
 * <p>
 * If your table is "sparse", consider using a {@code Map} with {@link Cell} keys (or Guava's {@code Table})
 * instead of this class.
 *
 * @see IntTable
 * @see Table
 */
public class CharTable extends AbstractTable<Character> {

    private final char[][] data;

    /**
     * Creates a new table by wrapping the given {@code char[][]} array.
     * The array is used directly, so changes to it are reflected in the table and vice versa.
     * The "rows" of the given matrix must have the same length.
     */
    public CharTable(char[][] data) {
        if (IntStream.range(1, data.length).anyMatch(i -> data[i].length != data[0].length)) {
            throw new IllegalArgumentException("Rows must have the same length.");
        }
        this.data = data;
    }

    /**
     * Creates a new table from a list of strings. The rows of the returned table represent the strings in the list,
     * which must have the same length.
     */
    public CharTable(List<String> data) {
        this(data.stream().map(String::toCharArray).toArray(char[][]::new));
    }

    /**
     * Creates a new table with the given number of rows and columns, filled with the given initial value.
     */
    public CharTable(int rowCount, int colCount, char initialValue) {
        data = new char[rowCount][colCount];
        Arrays.stream(data).forEach(rowData -> Arrays.fill(rowData, initialValue));
    }

    /**
     * Creates a new table with the given number of rows and columns, and calculates initial values by applying
     * the given function to the indices of each cell.
     */
    public CharTable(int rowCount, int colCount, BiFunction<Integer, Integer, Character> function) {
        data = new char[rowCount][colCount];
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                data[i][j] = function.apply(i, j);
            }
        }
    }

    /**
     * Creates a new table as a deep copy of the given table.
     */
    public CharTable(CharTable other) {
        data = new char[other.data.length][];
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
    public static CharTable wrap(Collection<? extends Position> positions, char value, char fillValue) {
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
    public static CharTable wrap(Map<? extends Position, Character> map, char fillValue) {
        return wrap(map.keySet(), map::get, fillValue);
    }

    private static <T extends Position> CharTable wrap(Collection<T> positions, Function<T, Character> function,
            char fillValue) {
        int minRow = positions.stream().mapToInt(Position::y).min().orElseThrow();
        int maxRow = positions.stream().mapToInt(Position::y).max().orElseThrow();
        int minCol = positions.stream().mapToInt(Position::x).min().orElseThrow();
        int maxCol = positions.stream().mapToInt(Position::x).max().orElseThrow();

        var table = new CharTable(maxRow - minRow + 1, maxCol - minCol + 1, fillValue);
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
    Character get0(int row, int col) {
        return data[row][col];
    }

    @Override
    void set0(int row, int col, Character value) {
        data[row][col] = value;
    }

    @Override
    CharTable newInstance(int rowCount, int colCount, BiFunction<Integer, Integer, Character> function) {
        return new CharTable(rowCount, colCount, function);
    }

    /**
     * Returns the {@code char[][]} array that backs this table. Changes to the returned array are reflected in the
     * table, and vice versa.
     */
    public char[][] asArray() {
        return data;
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public char get(int row, int col) {
        return data[row][col];
    }

    /**
     * Returns the value associated with the specified cell.
     */
    public char get(Cell cell) {
        return data[cell.row()][cell.col()];
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(int row, int col, char value) {
        data[row][col] = value;
    }

    /**
     * Sets the value associated with the specified cell.
     */
    public void set(Cell cell, char value) {
        data[cell.row()][cell.col()] = value;
    }

    /**
     * Sets all values in this table to the given value.
     */
    public void fill(char value) {
        Arrays.stream(data).forEach(rowData -> Arrays.fill(rowData, value));
    }

    /**
     * Returns an ordered stream of the values contained in the specified row of this table.
     */
    public Stream<Character> rowValues(int i) {
        return Utils.streamOf(data[i]);
    }

    /**
     * Returns an ordered stream of the values contained in the specified column of this table.
     */
    public Stream<Character> colValues(int j) {
        return IntStream.range(0, rowCount()).mapToObj(i -> data[i][j]);
    }

    /**
     * Returns an ordered stream of all values contained in this table (row by row).
     */
    public Stream<Character> values() {
        return Arrays.stream(data).flatMap(Utils::streamOf);
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
