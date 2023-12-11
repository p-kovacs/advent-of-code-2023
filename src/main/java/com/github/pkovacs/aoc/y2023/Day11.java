package com.github.pkovacs.aoc.y2023;

import java.util.Arrays;
import java.util.stream.IntStream;

import com.github.pkovacs.aoc.AbstractDay;
import com.github.pkovacs.util.data.CharTable;

public class Day11 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());
        var table = new CharTable(lines);

        System.out.println("Part 1: " + solve(table, 2));
        System.out.println("Part 2: " + solve(table, 1_000_000));
    }

    private static long solve(CharTable table, long expansion) {
        var galaxies = table.findAll('#').toList();

        var rowCost = IntStream.range(0, table.rowCount())
                .mapToLong(i -> table.rowValues(i).anyMatch(c -> c == '#') ? 1 : expansion).toArray();
        var colCost = IntStream.range(0, table.colCount())
                .mapToLong(i -> table.colValues(i).anyMatch(c -> c == '#') ? 1 : expansion).toArray();

        long sum = 0;
        for (var g1 : galaxies) {
            for (var g2 : galaxies) {
                sum += Arrays.stream(rowCost, min(g1.row(), g2.row()), max(g1.row(), g2.row())).sum()
                        + Arrays.stream(colCost, min(g1.col(), g2.col()), max(g1.col(), g2.col())).sum();
            }
        }

        return sum / 2;
    }

}
