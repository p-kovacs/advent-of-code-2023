package com.github.pkovacs.aoc.y2023;

import java.util.List;
import java.util.stream.Stream;

import com.github.pkovacs.aoc.AbstractDay;
import com.github.pkovacs.util.alg.Bfs;
import com.github.pkovacs.util.alg.Path;
import com.github.pkovacs.util.data.Cell;
import com.github.pkovacs.util.data.CharTable;
import com.github.pkovacs.util.data.Direction;

public class Day10 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());
        var table = new CharTable(lines);

        System.out.println("Part 1: " + solve1(table));
        System.out.println("Part 2: " + solve2(table));
//        System.out.println("Part 2: " + solveWithRowScanning(table)); // a faster solution
    }

    private static long solve1(CharTable table) {
        var loopMap = Bfs.run(table.find('S'), cell -> pipeNeighbors(table, cell));
        return loopMap.values().stream().mapToLong(Path::dist).max().orElseThrow();
    }

    private static long solve2(CharTable originalTable) {
        // Stretch the table
        var table = stretch(originalTable);

        // Clear cells except for the main loop
        var loop = Bfs.run(table.find('S'), cell -> pipeNeighbors(table, cell)).keySet();
        table.cells().filter(c -> !loop.contains(c)).forEach(c -> table.set(c, '.'));

        // Find empty cells reachable from outside
        var reached = Bfs.run(table.topLeft(), cell -> emptyNeighbors(table, cell)).keySet();

        // Calculate the result
        return table.cells()
                .filter(c -> c.row() % 2 == 1 && c.col() % 2 == 1) // original cell
                .filter(c -> table.get(c) == '.' && !reached.contains(c)) // empty and not reachable from outside
                .count();
    }

    /**
     * Stretches the given table. Each original cell (i, j) becomes the cell (2 * i + 1, 2 * j + 1) in the stretched
     * table, and each new cell between two original ones is filled with '|' or '-' character appropriately to ensure
     * that pipes remain continuous.
     */
    private static CharTable stretch(CharTable table) {
        var result = new CharTable(table.rowCount() * 2 + 1, table.colCount() * 2 + 1, '.');
        table.cells().forEach(c -> {
            var rc = new Cell(2 * c.row() + 1, 2 * c.col() + 1);
            result.set(rc, table.get(c));
            result.set(rc.row(), rc.col() + 1, '-');
            result.set(rc.row() + 1, rc.col(), '|');
        });
        return result;
    }

    private static List<Cell> emptyNeighbors(CharTable table, Cell cell) {
        return table.neighbors(cell).filter(c -> table.get(cell) == '.').toList();
    }

    private static List<Cell> pipeNeighbors(CharTable table, Cell cell) {
        char ch = table.get(cell);
        return ch == 'S'
                ? table.neighbors(cell).filter(c -> pipeNeighbors(table, c).contains(cell)).toList()
                : directions(ch).map(cell::neighbor).filter(table::containsCell).toList();
    }

    private static Stream<Direction> directions(char ch) {
        return switch (ch) {
            case '|' -> Stream.of(Direction.NORTH, Direction.SOUTH);
            case '-' -> Stream.of(Direction.EAST, Direction.WEST);
            case 'L' -> Stream.of(Direction.NORTH, Direction.EAST);
            case 'J' -> Stream.of(Direction.NORTH, Direction.WEST);
            case '7' -> Stream.of(Direction.SOUTH, Direction.WEST);
            case 'F' -> Stream.of(Direction.SOUTH, Direction.EAST);
            default -> Stream.of();
        };
    }

    /**
     * Another solution for part 2 based on the "row scanning" method used by others. For each row, we check the
     * bends from left to right, and determine if the current point is within the main loop or not. (This solution
     * is significantly faster.)
     */
    private static int solveWithRowScanning(CharTable table) {
        // Clear cells except for the main loop
        var loop = Bfs.run(table.find('S'), cell -> pipeNeighbors(table, cell)).keySet();
        table.cells().filter(c -> !loop.contains(c)).forEach(c -> table.set(c, '.'));

        // Replace S symbol (it's inconvenient, but can be necessary)
        var start = table.find('S');
        var origNeighbors = pipeNeighbors(table, start);
        for (var ch : "|-LJ7F".toCharArray()) {
            table.set(start, ch);
            if (pipeNeighbors(table, start).equals(origNeighbors)) {
                break;
            }
        }

        // Count inner cells for each row
        int count = 0;
        for (int i = 0; i < table.rowCount(); i++) {
            boolean inner = false;
            char lastBend = '*';
            for (int j = 0; j < table.colCount(); j++) {
                var ch = table.get(i, j);
                if (ch == 'L' || ch == 'F' || ch == '7' || ch == 'J') {
                    inner = (lastBend == 'L' && ch == '7') || (lastBend == 'F' && ch == 'J') ? !inner : inner;
                    lastBend = ch;
                } else if (ch == '|') {
                    inner = !inner;
                } else if (ch == '.' && inner) {
                    count++;
                }
            }
        }

        return count;
    }

}
