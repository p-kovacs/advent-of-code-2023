package com.github.pkovacs.aoc.y2023;

import java.util.stream.Stream;

import com.github.pkovacs.util.Bfs;
import com.github.pkovacs.util.CharTable;
import com.github.pkovacs.util.Dir;
import com.github.pkovacs.util.Path;
import com.github.pkovacs.util.Pos;

public class Day10 extends AbstractDay {

    public static void main(String[] args) {
        var table = new CharTable(readLines(getInputPath()));

        System.out.println("Part 1: " + solve1(table));
//        System.out.println("Part 2: " + solve2(table)); // original solution
        System.out.println("Part 2: " + solveWithRowScanning(table)); // a faster solution

        // Note: a formula-based solution can also be given, see Day 18
    }

    private static long solve1(CharTable table) {
        var loopMap = Bfs.findPaths(p -> pipeNeighbors(table, p), table.find('S'));
        return loopMap.values().stream().mapToLong(Path::dist).max().orElseThrow();
    }

    private static long solve2(CharTable originalTable) {
        // Stretch the table
        var table = stretch(originalTable);

        // Clear cells except for the main loop
        var loop = Bfs.findPaths(p -> pipeNeighbors(table, p), table.find('S')).keySet();
        table.cells().filter(p -> !loop.contains(p)).forEach(p -> table.set(p, '.'));

        // Find empty cells reachable from outside
        var reached = Bfs.findPaths(table.graph(c -> c == '.'), table.topLeft()).keySet();

        // Calculate the result
        return table.cells()
                .filter(p -> p.y % 2 == 1 && p.x % 2 == 1) // original cell
                .filter(p -> table.get(p) == '.' && !reached.contains(p)) // empty and not reachable from outside
                .count();
    }

    /**
     * Stretches the given table. Each original cell (i, j) becomes the cell (2 * i + 1, 2 * j + 1) in the stretched
     * table, and each new cell between two original ones is filled with '|' or '-' character appropriately to ensure
     * that pipes remain continuous.
     */
    private static CharTable stretch(CharTable table) {
        var result = new CharTable(table.width() * 2 + 1, table.height() * 2 + 1, '.');
        table.cells().forEach(p -> {
            var rp = new Pos(2 * p.x + 1, 2 * p.y + 1);
            result.set(rp, table.get(p));
            result.set(rp.x + 1, rp.y, '-');
            result.set(rp.x, rp.y + 1, '|');
        });
        return result;
    }

    private static Stream<Pos> pipeNeighbors(CharTable table, Pos p) {
        char ch = table.get(p);
        return ch == 'S'
                ? table.neighbors(p).filter(c -> pipeNeighbors(table, c).anyMatch(p::equals))
                : directions(ch).map(p::neighbor).filter(table::containsCell);
    }

    private static Stream<Dir> directions(char ch) {
        return switch (ch) {
            case '|' -> Stream.of(Dir.N, Dir.S);
            case '-' -> Stream.of(Dir.E, Dir.W);
            case 'L' -> Stream.of(Dir.N, Dir.E);
            case 'J' -> Stream.of(Dir.N, Dir.W);
            case '7' -> Stream.of(Dir.S, Dir.W);
            case 'F' -> Stream.of(Dir.S, Dir.E);
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
        var loop = Bfs.findPaths(p -> pipeNeighbors(table, p), table.find('S')).keySet();
        table.cells().filter(p -> !loop.contains(p)).forEach(p -> table.set(p, '.'));

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
        for (int y = 0; y < table.height(); y++) {
            boolean inner = false;
            char lastBend = '*';
            for (int x = 0; x < table.width(); x++) {
                var ch = table.get(x, y);
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
