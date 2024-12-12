package com.github.pkovacs.aoc.y2023;

import java.util.ArrayDeque;
import java.util.Arrays;

public class Day09 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        System.out.println("Part 1: " + lines.stream().mapToLong(s -> extrapolate(s, 1)).sum());
        System.out.println("Part 2: " + lines.stream().mapToLong(s -> extrapolate(s, 2)).sum());
    }

    private static long extrapolate(String line, int part) {
        var values = part == 1 ? parseLongs(line) : reverseOf(parseLongs(line));

        var diffs = new ArrayDeque<long[]>();
        diffs.add(values);
        while (Arrays.stream(diffs.getLast()).anyMatch(i -> i != 0)) {
            diffs.add(getDifferences(diffs.getLast()));
        }

        long x = 0;
        while (!diffs.isEmpty()) {
            var d = diffs.removeLast();
            x = d[d.length - 1] + x;
        }

        return x;
    }

    private static long[] getDifferences(long[] v) {
        var d = new long[v.length - 1];
        for (int i = 0; i < d.length; i++) {
            d[i] = v[i + 1] - v[i];
        }
        return d;
    }

}
