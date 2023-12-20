package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;

import com.github.pkovacs.aoc.AbstractDay;
import com.github.pkovacs.util.data.CharTable;

public class Day14 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());
        var table = new CharTable(lines);

        System.out.println("Part 1: " + solve1(table));
        System.out.println("Part 2: " + solve2(table, 1_000_000_000));
    }

    private static int solve1(CharTable table) {
        return calculateLoad(tilt(table));
    }

    private static int solve2(CharTable table, int iterationCount) {
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
        for (int j = 0; j < table.colCount(); j++) {
            int next = 0;
            for (int i = 0; i < table.rowCount(); i++) {
                if (table.get(i, j) == 'O') {
                    table.set(i, j, '.');
                    while (next < i && table.get(next, j) != '.') {
                        next++;
                    }
                    table.set(next, j, 'O');
                } else if (table.get(i, j) == '#') {
                    next = i;
                }
            }
        }
        return table;
    }

    private static int calculateLoad(CharTable table) {
        return table.findAll('O').mapToInt(c -> table.rowCount() - c.row()).sum();
    }

}
