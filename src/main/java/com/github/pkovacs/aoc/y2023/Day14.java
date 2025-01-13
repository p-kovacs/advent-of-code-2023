package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;

import com.github.pkovacs.util.CharTable;

public class Day14 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());
        var table = new CharTable(lines);

        System.out.println("Part 1: " + solve1(table));
        System.out.println("Part 2: " + solve2(table, 1_000_000_000));
    }

    private static long solve1(CharTable table) {
        return calculateLoad(tilt(table));
    }

    private static long solve2(CharTable table, int iterationCount) {
        var list = new ArrayList<CharTable>();
        for (int it = 0; it < iterationCount; it++) {
            list.add(table);
            table = new CharTable(table);
            for (int i = 0; i < 4; i++) {
                table = tilt(table).rotateRight();
            }

            var prev = list.indexOf(table); // linear search is acceptable here
            if (prev >= 0) {
                // The current state already occurred previously, so we found a cycle
                int cycleLength = list.size() - prev;
                int cycleCount = (iterationCount - prev) / cycleLength;
                return calculateLoad(list.get(iterationCount - cycleCount * cycleLength));
            }
        }
        return calculateLoad(table); // not reached for large iteration count
    }

    /**
     * Tilts the given table to the north (in place), and also returns it for the sake of simplicity.
     */
    private static CharTable tilt(CharTable table) {
        for (int x = 0; x < table.width(); x++) {
            int next = 0;
            for (int y = 0; y < table.height(); y++) {
                if (table.get(x, y) == 'O') {
                    table.set(x, y, '.');
                    while (next < y && table.get(x, next) != '.') {
                        next++;
                    }
                    table.set(x, next, 'O');
                } else if (table.get(x, y) == '#') {
                    next = y;
                }
            }
        }
        return table;
    }

    private static long calculateLoad(CharTable table) {
        return table.findAll('O').mapToLong(p -> table.height() - p.y).sum();
    }

}
