package com.github.pkovacs.util;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Abstract base class of table (or matrix) data structures. A table has a fixed number of columns and rows.
 * A cell of a table is identified by a {@link Pos} object or two integer indices, and it has an associated value.
 *
 * @param <V> the type of the values associated with the cells of this table
 * @see IntTable
 * @see CharTable
 * @see Table
 */
public abstract sealed class AbstractTable<V> permits IntTable, CharTable, Table {

    private List<Pos> cells;

    /**
     * Returns the width of this table (the number of columns).
     */
    public abstract int width();

    /**
     * Returns the height of this table (the number of rows).
     */
    public abstract int height();

    /**
     * Returns true if this range is empty, that is, {@code max < min}.
     */
    public final boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of cells in this table.
     */
    public final int size() {
        return width() * height();
    }

    abstract V get0(int x, int y);

    abstract void set0(int x, int y, V value);

    abstract AbstractTable<V> newInstance(int width, int height, BiFunction<Integer, Integer, V> function);

    /**
     * Returns true if this table contains the given cell.
     */
    public final boolean containsCell(Pos pos) {
        return pos.x >= 0 && pos.x < width() && pos.y >= 0 && pos.y < height();
    }

    /**
     * Returns true if this table contains the given cell.
     */
    public final boolean containsCell(int x, int y) {
        return x >= 0 && x < width() && y >= 0 && y < height();
    }

    /**
     * Returns true if this table contains the given cell.
     */
    public final boolean containsCell(long x, long y) {
        return x >= 0 && x < width() && y >= 0 && y < height();
    }

    /**
     * Returns an ordered stream of all cells in this table. The stream is ordered row by row (not lexicographically)
     * to provide faster access to the associated values for large tables.
     */
    public final Stream<Pos> cells() {
        return cellList().stream();
    }

    /**
     * Returns a list of all cells in this table. The list is ordered row by row (not lexicographically) to provide
     * faster access to the associated values for large tables.
     */
    public final List<Pos> cellList() {
        if (cells == null) {
            // Lazy load: generate and store cells
            int width = width();
            cells = IntStream.range(0, size()).mapToObj(i -> new Pos(i % width, i / width)).toList();
        }
        return cells;
    }

    /**
     * Returns the top left cell of this table.
     */
    public final Pos topLeft() {
        return new Pos(0, 0);
    }

    /**
     * Returns the bottom left cell of this table.
     */
    public final Pos bottomLeft() {
        return new Pos(0, height() - 1);
    }

    /**
     * Returns the top right cell of this table.
     */
    public final Pos topRight() {
        return new Pos(width() - 1, 0);
    }

    /**
     * Returns the bottom right cell of this table.
     */
    public final Pos bottomRight() {
        return new Pos(width() - 1, height() - 1);
    }

    /**
     * Returns a lexicographically sorted stream of the cells in the specified column of this table.
     */
    public final Stream<Pos> col(int x) {
        return IntStream.range(0, height()).mapToObj(y -> new Pos(x, y));
    }

    /**
     * Returns a lexicographically sorted stream of the cells in the first column of this table.
     */
    public Stream<Pos> firstCol() {
        return col(0);
    }

    /**
     * Returns a lexicographically sorted stream of the cells in the last column of this table.
     */
    public Stream<Pos> lastCol() {
        return col(width() - 1);
    }

    /**
     * Returns a lexicographically sorted stream of the cells in the specified row of this table.
     */
    public final Stream<Pos> row(int y) {
        return IntStream.range(0, width()).mapToObj(x -> new Pos(x, y));
    }

    /**
     * Returns a lexicographically sorted stream of the cells in the first row of this table.
     */
    public Stream<Pos> firstRow() {
        return row(0);
    }

    /**
     * Returns a lexicographically sorted stream of the cells in the last row of this table.
     */
    public Stream<Pos> lastRow() {
        return row(height() - 1);
    }

    /**
     * Returns an ordered stream of the cells around the border of this table. That is, the first and lost rows,
     * as well as the first and lost columns are included, but no cells are repeated. The encounter order of this
     * stream is not specified.
     */
    public Stream<Pos> border() {
        if (width() <= 2 || height() <= 2) {
            return cells();
        }

        return Stream.concat(
                Stream.concat(firstRow(), lastRow()),
                Stream.concat(firstCol().skip(1).limit(height() - 2), lastCol().skip(1).limit(height() - 2))
        );
    }

    /**
     * Returns a lexicographically sorted stream of the main (side-adjacent) neighbors of the given cell
     * within this table. Only those cells are included that are contained in this table (at most 4 positions).
     */
    public final Stream<Pos> neighbors(Pos pos) {
        return pos.neighbors().filter(this::containsCell);
    }

    /**
     * Returns a lexicographically sorted stream of the given cell and its main (side-adjacent) neighbors
     * within this table. Only those cells are included that are contained in this table (at most 5 positions).
     */
    public final Stream<Pos> neighborsAndSelf(Pos pos) {
        return pos.neighborsAndSelf().filter(this::containsCell);
    }

    /**
     * Returns a lexicographically sorted stream of the "extended" neighbors of the given cell within this table
     * (also including the diagonal ones). Only those cells are included that are contained in this table
     * (at most 8 positions).
     */
    public final Stream<Pos> neighbors8(Pos cell) {
        return cell.neighbors8().filter(this::containsCell);
    }

    /**
     * Returns a lexicographically sorted stream of the given cell and its "extended" neighbors within this table
     * (also including the diagonal ones). Only those cells are included that are contained in this table
     * (at most 9 positions).
     */
    public final Stream<Pos> neighbors8AndSelf(Pos cell) {
        return cell.neighbors8AndSelf().filter(this::containsCell);
    }

    /**
     * Returns an ordered stream of cells that constitutes a "ray" moving away from the given cell in the given
     * direction within this table. The first element of the stream (if any) is the corresponding neighbor of
     * this cell, the next element (if any) is the subsequent cell in the same direction, and so on while the cells
     * are contained in the table.
     */
    public final Stream<Pos> ray(Pos pos, Dir dir) {
        return ray(pos, pos.neighbor(dir));
    }

    /**
     * Returns an ordered stream of cells that constitutes a "ray" moving away from the given cell in the
     * direction specified by the given other cell within this table. The first element of the stream (if any)
     * is the given other cell (the second parameter), the next element (if any) is the subsequent cell in
     * the same direction, and so on while the cells are contained in the table.
     * <p>
     * This method can be combined with {@link #neighbors(Pos)} or {@link #neighbors8(Pos)} to obtain 4 or 8
     * rays moving away from this cell, respectively (i.e., the movement of a <i>rook</i> or <i>queen</i> in chess,
     * respectively).
     */
    public final Stream<Pos> ray(Pos pos, Pos other) {
        return pos.ray(other).takeWhile(this::containsCell);
    }

    /**
     * Returns the {@linkplain #neighbors(Pos) neighbor} graph of the cells in this table.
     */
    public final Graph<Pos> graph() {
        return Graph.of(this::neighbors);
    }

    /**
     * Returns a subgraph of the {@linkplain #neighbors(Pos) neighbor} graph of the cells in this table.
     * The cells are filtered by the given predicate applied to their associated values.
     */
    public final Graph<Pos> graph(Predicate<V> valueFilter) {
        return Graph.of(this::neighbors).filterNodes(p -> valueFilter.test(get0((int) p.x, (int) p.y)));
    }

    /**
     * Returns the {@linkplain #neighbors8(Pos) "extended" neighbor} graph of the cells in this table.
     */
    public final Graph<Pos> graph8() {
        return Graph.of(this::neighbors8);
    }

    /**
     * Returns a subgraph of the {@linkplain #neighbors8(Pos) "extended" neighbor} graph of the cells in this table.
     * The cells are filtered by the given predicate applied to their associated values.
     */
    public final Graph<Pos> graph8(Predicate<V> valueFilter) {
        return Graph.of(this::neighbors8).filterNodes(p -> valueFilter.test(get0((int) p.x, (int) p.y)));
    }

    /**
     * Finds the first cell with the given associated value in this table.
     *
     * @throws java.util.NoSuchElementException if the table does not contain the given value
     */
    public final Pos find(V value) {
        return findAll(value).findFirst().orElseThrow();
    }

    /**
     * Returns an ordered stream of all cells with the given associated value in this table (row by row).
     */
    public final Stream<Pos> findAll(V value) {
        return cells().filter(p -> value.equals(get0((int) p.x, (int) p.y)));
    }

    /**
     * Updates the value associated with the given cell by applying the specified function to the current value
     * and returns the new value.
     */
    public final V update(Pos pos, Function<? super V, ? extends V> function) {
        return update((int) pos.x, (int) pos.y, function);
    }

    /**
     * Updates the value associated with the given cell by applying the specified function to the current value
     * and returns the new value.
     */
    public final V update(int x, int y, Function<? super V, ? extends V> function) {
        V value = function.apply(get0(x, y));
        set0(x, y, value);
        return value;
    }

    /**
     * Creates a new table by mirroring this one horizontally: y indices remain the same, while x indices are flipped.
     */
    public AbstractTable<V> mirrorHorizontally() {
        int width = width();
        return newInstance(width, height(), (x, y) -> get0(width - 1 - x, y));
    }

    /**
     * Creates a new table by mirroring this one vertically: x indices remain the same, while y indices are flipped.
     */
    public AbstractTable<V> mirrorVertically() {
        int height = height();
        return newInstance(width(), height, (x, y) -> get0(x, height - 1 - y));
    }

    /**
     * Creates a new table by rotating this one to the right (clockwise).
     */
    public AbstractTable<V> rotateRight() {
        int height = height();
        return newInstance(height, width(), (x, y) -> get0(y, height - 1 - x));
    }

    /**
     * Creates a new table by rotating this one to the left (counter-clockwise).
     */
    public AbstractTable<V> rotateLeft() {
        int width = width();
        return newInstance(height(), width, (x, y) -> get0(width - 1 - y, x));
    }

    /**
     * Creates a new table by transposing this one: turns rows into columns and vice versa.
     */
    public AbstractTable<V> transpose() {
        return newInstance(height(), width(), (x, y) -> get0(y, x));
    }

    /**
     * Returns a new table by extending this one with the given amount uniformly in all directions.
     * Negative amount means shrinking. Newly added cells will be assigned with the given fill value.
     */
    public AbstractTable<V> extend(int delta, V fillValue) {
        return extend(delta, delta, fillValue);
    }

    /**
     * Returns a new table by extending this one with the given amounts along the corresponding axes (in both
     * directions). Negative amount means shrinking. Newly added cells will be assigned with the given fill value.
     */
    public AbstractTable<V> extend(int dx, int dy, V fillValue) {
        int w = width() + 2 * dx;
        int h = height() + 2 * dy;
        if (w < 0 || h < 0) {
            throw new IllegalArgumentException("Negative table size.");
        }

        return newInstance(w, h, (x, y) -> containsCell(x - dx, y - dy) ? get0(x - dx, y - dy) : fillValue);
    }

}
