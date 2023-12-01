package com.github.pkovacs.util.data;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a cell (or position) in a table or matrix as an immutable pair of {@code int} values:
 * {@code (row, col)}. This class provides various useful methods and also supports lexicographical ordering
 * (first by row index, then by column index).
 * <p>
 * {@link Point} is a similar class with different order and names of the components: {@code (x, y)} instead of
 * {@code (row, col)}.
 *
 * @see Point
 * @see Table
 */
public record Cell(int row, int col) implements Position, Comparable<Cell> {

    /** The origin cell: (0, 0). */
    public static final Cell ORIGIN = new Cell(0, 0);

    @Override
    public int x() {
        return col;
    }

    @Override
    public int y() {
        return row;
    }

    /**
     * Returns true if the indices of this cell are between zero (inclusive) and the given row/column count
     * (exclusive).
     */
    public boolean isValid(int rowCount, int colCount) {
        return row >= 0 && row < rowCount && col >= 0 && col < colCount;
    }

    /**
     * Returns the neighbor of this cell in the given direction. (0, 0) represents the <i>top</i> left cell among
     * the ones with non-negative indices, so "north" or "up" means decreasing row index, while "south" or "down"
     * means increasing row index.
     */
    public Cell neighbor(Direction dir) {
        return switch (dir) {
            case NORTH -> new Cell(row - 1, col);
            case EAST -> new Cell(row, col + 1);
            case SOUTH -> new Cell(row + 1, col);
            case WEST -> new Cell(row, col - 1);
        };
    }

    /**
     * Returns the neighbor of this cell in the given direction. (0, 0) represents the <i>top</i> left cell among
     * the ones with non-negative indices, so "north" or "up" means decreasing row index, while "south" or "down"
     * means increasing row index.
     *
     * @param dir the direction character. One of 'N' (north), 'E' (east), 'S' (south), 'W' (west),
     *         'U' (up), 'R' (right), 'D' (down), 'L' (left), and their lowercase variants.
     */
    public Cell neighbor(char dir) {
        return neighbor(Direction.fromChar(dir));
    }

    /**
     * Returns a lexicographically sorted stream of the four neighbors of this cell.
     */
    public Stream<Cell> neighbors() {
        return neighborsAndSelf().filter(c -> c != this);
    }

    /**
     * Returns a lexicographically sorted stream of this cell and its four neighbors.
     */
    public Stream<Cell> neighborsAndSelf() {
        return Stream.of(
                new Cell(row - 1, col),
                new Cell(row, col - 1),
                this,
                new Cell(row, col + 1),
                new Cell(row + 1, col));
    }

    /**
     * Returns a lexicographically sorted stream of the eight "extended" neighbors of this cell (also including the
     * diagonal ones).
     */
    public Stream<Cell> extendedNeighbors() {
        return extendedNeighborsAndSelf().filter(c -> c != this);
    }

    /**
     * Returns a lexicographically sorted stream of this cell and its eight "extended" neighbors (also including the
     * diagonal ones).
     */
    public Stream<Cell> extendedNeighborsAndSelf() {
        return Stream.of(
                new Cell(row - 1, col - 1),
                new Cell(row - 1, col),
                new Cell(row - 1, col + 1),
                new Cell(row, col - 1),
                this,
                new Cell(row, col + 1),
                new Cell(row + 1, col - 1),
                new Cell(row + 1, col),
                new Cell(row + 1, col + 1));
    }

    /**
     * Returns an ordered stream of cells that constitutes a straight line segment from this cell to the given
     * other cell (horizontally, vertically, or diagonally). The first element of the stream is this cell, and
     * the last element is the given other cell (provided that they lay on a common line).
     *
     * @throws IllegalArgumentException if the cells do not lay on a common horizontal, vertical, or diagonal
     *         line.
     */
    public Stream<Cell> lineTo(Cell other) {
        int rowDist = other.row - row;
        int colDist = other.col - col;

        if (equals(other)) {
            return Stream.of(this);
        } else if (rowDist == 0 || colDist == 0 || Math.abs(rowDist) == Math.abs(colDist)) {
            int dist = Math.max(Math.abs(rowDist), Math.abs(colDist));
            int dr = rowDist / dist;
            int dc = colDist / dist;
            return IntStream.rangeClosed(0, dist).mapToObj(i -> new Cell(row + i * dr, col + i * dc));
        } else {
            throw new IllegalArgumentException(
                    "The cells do not lay on a common horizontal, vertical, or diagonal line.");
        }
    }

    /**
     * Returns an infinite ordered stream of cells that constitutes a "ray" moving away from this cell in the
     * direction specified by the given other cell. The first element of the stream is the given cell, the next
     * element is the subsequent cell in the same direction (applying the same changes to the row and column indices),
     * and so on.
     * <p>
     * This method can be combined with {@link #neighbors()} or {@link #extendedNeighbors()} to obtain 4 or 8 rays
     * moving away from this cell, respectively (i.e., the movement of <i>rook</i> or <i>queen</i> in chess,
     * respectively).
     */
    public Stream<Cell> ray(Cell other) {
        int deltaRow = other.row - row;
        int deltaCol = other.col - col;
        return Stream.iterate(other, t -> t.add(deltaRow, deltaCol));
    }

    /**
     * Creates a new cell by adding the given delta values to the indices of this cell.
     */
    public Cell add(int deltaRow, int deltaCol) {
        return new Cell(row + deltaRow, col + deltaCol);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }

    @Override
    public int compareTo(Cell other) {
        return row != other.row ? Integer.compare(row, other.row) : Integer.compare(col, other.col);
    }

    /**
     * Returns the bounding {@link Range} of the row indices of the given cells.
     *
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    public static Range rowRange(Collection<Cell> cells) {
        return Range.bound(cells.stream().mapToInt(Cell::row));
    }

    /**
     * Returns the bounding {@link Range} of the column indices of the given cells.
     *
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    public static Range colRange(Collection<Cell> cells) {
        return Range.bound(cells.stream().mapToInt(Cell::col));
    }

    /**
     * Returns an ordered stream of {@link #isValid(int, int) valid} cells within the given bounds.
     * If both arguments are positive, then the first element of the stream is (0, 0), the last element is
     * {@code (rowCount - 1, colCount - 1)}, and the stream is lexicographically sorted.
     * Otherwise, an empty stream is returned.
     */
    public static Stream<Cell> box(int rowCount, int colCount) {
        return box(ORIGIN, new Cell(rowCount - 1, colCount - 1));
    }

    /**
     * Returns an ordered stream of cells within the closed box {@code [min..max]}.
     * If {@code min.row <= max.row} and {@code min.col <= max.col}, then the first element of the stream is
     * {@code min}, the last element is {@code max}, and the stream is lexicographically sorted.
     * Otherwise, an empty stream is returned.
     */
    public static Stream<Cell> box(Cell min, Cell max) {
        int rowCount = max.row - min.row + 1;
        int colCount = max.col - min.col + 1;
        if (rowCount <= 0 || colCount <= 0) {
            return Stream.empty();
        }

        return IntStream.range(0, rowCount * colCount)
                .mapToObj(i -> new Cell(min.row + i / colCount, min.col + i % colCount));
    }

    /**
     * Returns an ordered stream of cells within the
     * <a href="https://en.wikipedia.org/wiki/Minimum_bounding_box">minimum bounding box</a> of the given cells.
     * The returned stream is lexicographically sorted and non-empty if the given array is non-empty.
     */
    public static Stream<Cell> boundingBox(Cell... cells) {
        return boundingBox(List.of(cells));
    }

    /**
     * Returns an ordered stream of cells within the
     * <a href="https://en.wikipedia.org/wiki/Minimum_bounding_box">minimum bounding box</a> of the given cells.
     * The returned stream is lexicographically sorted and non-empty if the given collection is non-empty.
     */
    public static Stream<Cell> boundingBox(Collection<Cell> cells) {
        if (cells.isEmpty()) {
            return Stream.empty();
        }

        var rowRange = rowRange(cells);
        var colRange = colRange(cells);
        return box(new Cell((int) rowRange.min(), (int) colRange.min()),
                new Cell((int) rowRange.max(), (int) colRange.max()));
    }

}
