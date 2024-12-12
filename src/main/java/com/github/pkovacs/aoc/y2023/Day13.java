package com.github.pkovacs.aoc.y2023;

import java.util.stream.IntStream;

import com.github.pkovacs.util.data.CharTable;

public class Day13 extends AbstractDay {

    public static void main(String[] args) {
        var blocks = readLineBlocks(getInputPath());

        long ans1 = blocks.stream().map(CharTable::new).mapToLong(b -> getScore(b, 0, 0)).sum();
        long ans2 = blocks.stream().map(CharTable::new).mapToLong(b -> getScore2(b)).sum();

        System.out.println("Part 1: " + ans1);
        System.out.println("Part 2: " + ans2);
    }

    private static int getScore2(CharTable table) {
        int row = findRowReflection(table, 0);
        int col = findRowReflection(table.rotateRight(), 0);

        for (var cell : table.cells().toList()) {
            var copy = new CharTable(table);
            copy.set(cell, table.get(cell) == '.' ? '#' : '.');
            int score = getScore(copy, row, col);
            if (score > 0) {
                return score;
            }
        }

        return 0; // never happens
    }

    private static int getScore(CharTable table, int skipRow, int skipCol) {
        int row = findRowReflection(table, skipRow);
        return row > 0 ? row * 100 : findRowReflection(table.rotateRight(), skipCol);
    }

    private static int findRowReflection(CharTable table, int skipRow) {
        return IntStream.range(1, table.rowCount())
                .filter(i -> i != skipRow)
                .filter(i -> IntStream.range(0, min(i, table.rowCount() - i))
                        .allMatch(j -> sameRow(table, i - j - 1, i + j)))
                .findFirst().orElse(0);
    }

    private static boolean sameRow(CharTable table, int i, int j) {
        return IntStream.range(0, table.colCount()).allMatch(k -> table.get(i, k) == table.get(j, k));
    }

}
