package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.pkovacs.aoc.AbstractDay;
import com.github.pkovacs.util.data.Point;
import com.github.pkovacs.util.data.Range;

public class Day18 extends AbstractDay {

    private static final Map<Character, Character> directionMap =
            Map.of('0', 'R', '1', 'D', '2', 'L', '3', 'U');

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        System.out.println("Part 1: " + solve(lines, 1));
        System.out.println("Part 2: " + solve(lines, 2));
    }

    /**
     * Solves the puzzle using a similar approach for detecting interior area as the "row scanning" solution for
     * Day 10. We assume that the instructions describe a <i>simple polygon</i>: it does not intersect with itself.
     * <p>
     * The same characters are used to represent the loop (|, L, 7, F, J) as for Day 10, but horizontal borders (-)
     * are not stored, only counted. The positions of |, L, 7, F, J characters are collected for the rows, but to
     * make it efficient, consecutive rows with the same pattern are merged and represented as a range of y
     * coordinates.
     */
    private static long solve(List<String> lines, int part) {
        var points = parse(lines, part);

        // Data structure to store the border pattern for each range of rows (i.e. y coordinates)
        var patterns = new HashMap<Range, TreeMap<Integer, Character>>();

        // Collect ranges of rows
        int[] ys = points.stream().mapToInt(Point::y).sorted().distinct().toArray();
        for (int i = 0; i < ys.length; i++) {
            patterns.put(new Range(ys[i], ys[i]), new TreeMap<>()); // single row with corners
            if (i < ys.length - 1) {
                var range = new Range(ys[i] + 1, ys[i + 1] - 1); // following row range without corners
                patterns.put(range, new TreeMap<>());
            }
        }

        // Collect border patterns for the row ranges
        for (int i = 1; i < points.size(); i++) {
            var p = points.get(i);
            var prev = points.get(i - 1);
            var next = points.get((i + 1) % points.size());
            var dir1 = dir(prev, p);
            var dir2 = dir(p, next);

            // Determine the type of the current bend (we assume that there is a 90-degree turn at every step)
            char bend = switch (dir1) {
                case 'U' -> dir2 == 'L' ? '7' : 'F';
                case 'D' -> dir2 == 'L' ? 'J' : 'L';
                case 'L' -> dir2 == 'U' ? 'L' : 'F';
                case 'R' -> dir2 == 'U' ? 'J' : '7';
                default -> throw new IllegalArgumentException("Cannot determine bend: " + dir1 + "->" + dir2);
            };

            // Add current bend (L, 7, F, J)
            patterns.get(new Range(p.y(), p.y())).put(p.x(), bend);

            // Add | character to each row range between the current point and the next one
            if (dir2 == 'U' || dir2 == 'D') {
                var range = new Range(min(p.y(), next.y()) + 1, max(p.y(), next.y()) - 1);
                patterns.keySet().stream().filter(range::containsAll).forEach(r -> patterns.get(r).put(p.x(), '|'));
            }
        }

        // Count the total size of the lagoon
        long total = 0;
        for (var range : patterns.keySet()) {
            boolean inner = false;
            char lastBend = '*';
            long lastX = Long.MIN_VALUE;
            long count = 0;
            for (var entry : patterns.get(range).entrySet()) {
                int x = entry.getKey();
                var ch = entry.getValue();
                if (inner || ch == '7' || ch == 'J') {
                    count += x - lastX - 1; // interior area or horizontal line between two bends
                }
                if (ch == '7' || ch == 'J') {
                    inner = (lastBend == 'L' && ch == '7') || (lastBend == 'F' && ch == 'J') ? !inner : inner;
                } else if (ch == 'L' || ch == 'F') {
                    lastBend = ch;
                } else if (ch == '|') {
                    inner = !inner;
                }
                count++; // single border (| or a bend)
                lastX = x;
            }
            total += count * range.count();
        }

        return total;
    }

    private static List<Point> parse(List<String> lines, int part) {
        var list = new ArrayList<Point>();
        var pos = new Point(0, 0);
        list.add(pos);
        for (var line : lines) {
            var cmd = line.split(" ");
            char dir = part == 1 ? cmd[0].charAt(0) : directionMap.get(cmd[2].charAt(7));
            int count = part == 1 ? Integer.parseInt(cmd[1]) : Integer.parseInt(cmd[2].substring(2, 7), 16);
            pos = switch (dir) {
                case 'R' -> pos.add(count, 0);
                case 'D' -> pos.add(0, count);
                case 'L' -> pos.add(-count, 0);
                case 'U' -> pos.add(0, -count);
                default -> throw new IllegalArgumentException("Invalid direction: " + dir);
            };
            list.add(pos);
        }
        if (!pos.equals(list.get(0))) {
            throw new IllegalArgumentException("Not a closed loop.");
        }
        return list;
    }

    private static char dir(Point from, Point to) {
        return to.x() == from.x() ? (to.y() < from.y() ? 'U' : 'D') : (to.x() < from.x() ? 'L' : 'R');
    }

}
