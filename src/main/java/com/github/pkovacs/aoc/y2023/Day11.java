package com.github.pkovacs.aoc.y2023;

import java.util.Arrays;
import java.util.stream.IntStream;

import com.github.pkovacs.util.CharTable;

public class Day11 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());
        var table = new CharTable(lines);

        System.out.println("Part 1: " + solve(table, 2));
        System.out.println("Part 2: " + solve(table, 1_000_000));
    }

    private static long solve(CharTable table, long expansion) {
        var galaxies = table.findAll('#').toList();

        var rowCost = IntStream.range(0, table.width())
                .mapToLong(i -> table.rowValues(i).anyMatch(c -> c == '#') ? 1 : expansion).toArray();
        var colCost = IntStream.range(0, table.height())
                .mapToLong(i -> table.colValues(i).anyMatch(c -> c == '#') ? 1 : expansion).toArray();

        long sum = 0;
        for (var g1 : galaxies) {
            for (var g2 : galaxies) {
                sum += Arrays.stream(colCost, (int) min(g1.x, g2.x), (int) max(g1.x, g2.x)).sum()
                        + Arrays.stream(rowCost, (int) min(g1.y, g2.y), (int) max(g1.y, g2.y)).sum();
            }
        }

        return sum / 2;
    }

}
