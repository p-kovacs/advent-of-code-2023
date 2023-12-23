package com.github.pkovacs.aoc.y2023;

import com.github.pkovacs.aoc.AbstractDay;
import com.github.pkovacs.util.alg.Bfs;
import com.github.pkovacs.util.data.Cell;
import com.github.pkovacs.util.data.CharTable;

public class Day21 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());
        var table = new CharTable(lines);

        System.out.println("Part 1: " + countReachableFields(table, table.find('S'), 64));
        System.out.println("Part 2: " + solve2(table));
    }

    /**
     * Solves part 2 greatly exploiting the speciality of the input. We assume that the width and height of the
     * "garden" is the same odd number, and start tile is at the middle of the garden. Furthermore, we also
     * assume that there are no rocks in the row and column of the start tile and at the border of the garden.
     * Consequently, we can safely assume that for each tile at the border of the garden, the minimum step count
     * to reach that tile from the start tile is equal to the "taxicab" (aka. Manhattan) distance of the tiles.
     * <p>
     * Furthermore, note that if a tile is reachable in K steps, then it is also reachable in K + 2 * n steps
     * for each positive integer n.
     * <p>
     * Using these insights, the calculation is as follows: starting with a single garden, we iteratively add
     * a new "circle" of gardens around it in diamond shape (in accordance with the taxicab metric). For each
     * such diamond added, we determine whether all of its gardens will be "inner" gardens (for which each tile is
     * reachable within the given maximum step count) or not. If they are inner gardens, then we can count all of
     * their tiles with odd/even distance from the start tile appropriately. Otherwise, we categorize the gardens
     * of the diamond into 8 categories according to their directions, and we count the reachable tiles for each
     * category (using the position of the tile that is closest to the start tile and the remaining step count).
     */
    private static long solve2(CharTable table) {
        var start = table.find('S');
        table.set(start, '.');

        int size = table.rowCount();
        long maxDist = 26501365;

        // Number of reachable tiles in odd/even steps in inner gardens
        long innerOddTileCount = countReachableFields(table, start, table.rowCount());
        long innerEvenTileCount = countReachableFields(table, start, table.rowCount() + 1);

        // Start with the middle garden
        long total = innerOddTileCount;

        // Process diamonds
        for (long diamond = 1; true; diamond++) {
            long remStraight = maxDist - (diamond - 1) * size - size / 2 - 1; // remaining steps for N, E, S, W gardens
            long remDiagonal = maxDist - (diamond - 1) * size - 1; // remaining steps for other gardens in the diamond
            if (remDiagonal < 0) {
                break;
            }

            if (remDiagonal >= 2L * size - 2) { // only inner gardens
                total += diamond * 4 * ((diamond - 1) % 2 == 0 ? innerEvenTileCount : innerOddTileCount);
            } else {
                // N, E, S, W gardens
                if (remStraight >= 0) {
                    total += countReachableFields(table, new Cell(size - 1, start.col()), remStraight);
                    total += countReachableFields(table, new Cell(start.row(), 0), remStraight);
                    total += countReachableFields(table, new Cell(0, start.col()), remStraight);
                    total += countReachableFields(table, new Cell(start.row(), size - 1), remStraight);
                }

                // "Diagonal" gardens in directions NE, SE, SW, NW
                total += (diamond - 1) * countReachableFields(table, table.bottomLeft(), remDiagonal);
                total += (diamond - 1) * countReachableFields(table, table.topLeft(), remDiagonal);
                total += (diamond - 1) * countReachableFields(table, table.topRight(), remDiagonal);
                total += (diamond - 1) * countReachableFields(table, table.bottomRight(), remDiagonal);
            }
        }

        return total;
    }

    private static long countReachableFields(CharTable table, Cell start, long maxSteps) {
        return Bfs.run(start, c -> table.neighbors(c).filter(n -> table.get(n) != '#').toList()).values().stream()
                .filter(p -> p.dist() <= maxSteps && p.dist() % 2 == maxSteps % 2)
                .count();
    }

}
