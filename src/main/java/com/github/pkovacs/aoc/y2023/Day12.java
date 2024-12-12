package com.github.pkovacs.aoc.y2023;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Day12 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        System.out.println("Part 1: " + solve(lines, 1));
        System.out.println("Part 2: " + solve(lines, 5));
    }

    private static long solve(List<String> lines, int copies) {
        return lines.stream().mapToLong(line -> {
            var fields = ("?" + line.split(" ")[0]).repeat(copies).substring(1);
            var groups = ("," + line.split(" ")[1]).repeat(copies).substring(1);
            return count(fields.toCharArray(), 0, parseInts(groups), 0, new HashMap<>());
        }).sum();
    }

    private static long count(char[] fields, int fieldIndex, int[] groups, int groupIndex, Map<State, Long> cache) {
        // Check chached value (memoization, only required for part 2)
        var key = new State(fieldIndex, groupIndex);
        var cachedCount = cache.get(key);
        if (cachedCount != null) {
            return cachedCount;
        }

        // Handle trivial case: 0 remaining groups
        if (groupIndex == groups.length) {
            return noneMatch(fields, fieldIndex, fields.length, '#') ? 1 : 0;
        }

        // Find each feasible position i of the current group and recursively calculate the arrangement counts
        // for the remaining groups
        long count = 0;
        int n = groups[groupIndex];
        int maxPos = fields.length - IntStream.range(groupIndex, groups.length).map(i -> groups[i] + 1).sum() + 1;
        for (int i = fieldIndex; i <= maxPos; i++) {
            if (i > fieldIndex && fields[i - 1] == '#') { // '#' before the group -> break
                break;
            }
            if (noneMatch(fields, i, i + n, '.') // no '.' conflicting with the group
                    && (i == fields.length - n || fields[i + n] != '#')) { // no '#' directly after the group
                count += count(fields, i + n + 1, groups, groupIndex + 1, cache);
            }
        }

        cache.put(key, count);
        return count;
    }

    private static boolean noneMatch(char[] array, int from, int to, char ch) {
        // Note: a loop is used here instead of stream API to make the solution two times faster
        for (int i = from; i < to; i++) {
            if (array[i] == ch) {
                return false;
            }
        }
        return true;
    }

    private record State(int fieldIndex, int groupIndex) {}

}
