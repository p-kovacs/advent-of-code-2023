package com.github.pkovacs.util.data;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Abstract base class of table data structures. A table has a fixed number of rows and columns. A cell of a table
 * is identified by a {@link Cell} object or two integer indices, and it has an associated value.
 *
 * @param <V> the type of the values associated with the cells of this table
 */
public abstract class AbstractTable<V> {

    /**
     * Returns the number of rows in this table.
     */
    public abstract int rowCount();

    /**
     * Returns the number of columns in this table.
     */
    public abstract int colCount();

    /**
     * Returns the number of cells in this table.
     */
    public int size() {
        return rowCount() * colCount();
    }

    abstract V get0(int row, int col);

    abstract void set0(int row, int col, V value);

    abstract AbstractTable<V> newInstance(int rowCount, int colCount, BiFunction<Integer, Integer, V> function);

    /**
     * Returns true if this table contains the given cell.
     */
    public boolean containsCell(Cell cell) {
        return cell.isValid(rowCount(), colCount());
    }

    /**
     * Returns an ordered stream of all cells in this table (row by row).
     */
    public Stream<Cell> cells() {
        return Cell.box(rowCount(), colCount());
    }

    /**
     * Returns an ordered stream of the cells in the specified row of this table.
     */
    public Stream<Cell> row(int i) {
        return IntStream.range(0, colCount()).mapToObj(j -> new Cell(i, j));
    }

    /**
     * Returns an ordered stream of the cells in the specified column of this table.
     */
    public Stream<Cell> col(int j) {
        return IntStream.range(0, rowCount()).mapToObj(i -> new Cell(i, j));
    }

    /**
     * Returns the top left cell of this table.
     */
    public Cell topLeft() {
        return new Cell(0, 0);
    }

    /**
     * Returns the bottom left cell of this table.
     */
    public Cell bottomLeft() {
        return new Cell(rowCount() - 1, 0);
    }

    /**
     * Returns the top right cell of this table.
     */
    public Cell topRight() {
        return new Cell(0, colCount() - 1);
    }

    /**
     * Returns the bottom right cell of this table.
     */
    public Cell bottomRight() {
        return new Cell(rowCount() - 1, colCount() - 1);
    }

    /**
     * Returns a lexicographically sorted stream of the neighbors of the given cell within this table.
     * Only those cells are included that are contained in this table (at most four cells).
     */
    public Stream<Cell> neighbors(Cell cell) {
        return cell.neighbors().filter(this::containsCell);
    }

    /**
     * Returns a lexicographically sorted stream of the given cell and its neighbors within this table.
     * Only those cells are included that are contained in this table (at most five cells).
     */
    public Stream<Cell> neighborsAndSelf(Cell cell) {
        return cell.neighborsAndSelf().filter(this::containsCell);
    }

    /**
     * Returns a lexicographically sorted stream of the "extended" neighbors of the given cell within this table.
     * Only those cells are included that are contained in this table (at most eight cells).
     */
    public Stream<Cell> extendedNeighbors(Cell cell) {
        return cell.extendedNeighbors().filter(this::containsCell);
    }

    /**
     * Returns a lexicographically sorted stream of the given cell and its "extended" neighbors within this table.
     * Only those cells are included that are contained in this table (at most nine cells).
     */
    public Stream<Cell> extendedNeighborsAndSelf(Cell cell) {
        return cell.extendedNeighborsAndSelf().filter(this::containsCell);
    }

    /**
     * Returns an ordered stream of cells that constitutes a "ray" moving away from the given cell in the given
     * direction within this table. The first element of the stream (if any) is the corresponding neighbor of
     * this cell, the next element (if any) is the subsequent cell in the same direction, and so on while the cells
     * are contained in the table.
     */
    public Stream<Cell> ray(Cell cell, Direction dir) {
        return ray(cell, cell.neighbor(dir));
    }

    /**
     * Returns an ordered stream of cells that constitutes a "ray" moving away from the given cell in the
     * direction specified by the given other cell within this table. The first element of the stream (if any)
     * is the given other cell (the second parameter), the next element (if any) is the subsequent cell in
     * the same direction, and so on while the cells are contained in the table.
     * <p>
     * This method can be combined with {@link #neighbors(Cell)} or {@link #extendedNeighbors(Cell)} to obtain 4 or 8
     * rays moving away from this cell, respectively (i.e., the movement of <i>rook</i> or <i>queen</i> in chess,
     * respectively).
     */
    public Stream<Cell> ray(Cell cell, Cell other) {
        return cell.ray(other).takeWhile(this::containsCell);
    }

    /**
     * Finds a cell with the given value in this table.
     *
     * @throws java.util.NoSuchElementException if the table does not contain the given value
     */
    public Cell find(V value) {
        return findAll(value).findFirst().orElseThrow();
    }

    /**
     * Finds all cells with the given value in this table.
     */
    public Stream<Cell> findAll(V value) {
        return cells().filter(c -> get0(c.row(), c.col()).equals(value));
    }

    /**
     * Updates the value associated with the specified cell by applying the given function to the current value
     * and returns the new value.
     */
    public V update(int row, int col, Function<? super V, ? extends V> function) {
        V value = function.apply(get0(row, col));
        set0(row, col, value);
        return value;
    }

    /**
     * Updates the value associated with the specified cell by applying the given function to the current value
     * and returns the new value.
     */
    public V update(Cell cell, Function<? super V, ? extends V> function) {
        return update(cell.row(), cell.col(), function);
    }

    /**
     * Creates a new table by mirroring this one horizontally: row indices remain the same, while column indices
     * are flipped.
     */
    public AbstractTable<V> mirrorHorizontally() {
        int colCount = colCount();
        return newInstance(rowCount(), colCount, (i, j) -> get0(i, colCount - 1 - j));
    }

    /**
     * Creates a new table by mirroring this one vertically: column indices remain the same, while row indices
     * are flipped.
     */
    public AbstractTable<V> mirrorVertically() {
        int rowCount = rowCount();
        return newInstance(rowCount, colCount(), (i, j) -> get0(rowCount - 1 - i, j));
    }

    /**
     * Creates a new table by rotating this one to the right (clockwise).
     */
    public AbstractTable<V> rotateRight() {
        int rowCount = rowCount();
        return newInstance(colCount(), rowCount(), (i, j) -> get0(rowCount - 1 - j, i));
    }

    /**
     * Creates a new table by rotating this one to the left (counter-clockwise).
     */
    public AbstractTable<V> rotateLeft() {
        int colCount = colCount();
        return newInstance(colCount(), rowCount(), (i, j) -> get0(j, colCount - 1 - i));
    }

    /**
     * Creates a new table by transposing this one: turns rows into columns and vice versa.
     */
    public AbstractTable<V> transpose() {
        return newInstance(colCount(), rowCount(), (i, j) -> get0(j, i));
    }

}
