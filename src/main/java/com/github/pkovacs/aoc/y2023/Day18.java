package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.pkovacs.util.alg.Bfs;
import com.github.pkovacs.util.data.Point;
import com.github.pkovacs.util.data.Range;

public class Day18 extends AbstractDay {

    private static final Map<Character, Character> directionMap =
            Map.of('0', 'R', '1', 'D', '2', 'L', '3', 'U');

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

//        System.out.println("Part 1: " + solve(lines, 1));
//        System.out.println("Part 1: " + solveWithRowScanning(lines, 1));
        System.out.println("Part 1: " + solveWithFormula(lines, 1));
//        System.out.println("Part 2: " + solve(lines, 2));
//        System.out.println("Part 2: " + solveWithRowScanning(lines, 2));
        System.out.println("Part 2: " + solveWithFormula(lines, 2));
    }

    /**
     * Solves the puzzle by "compressing" the ranges of x and y coordinates between the corners (bends) of the loop.
     * Each compressed point represents a rectangle of original points. The exterior area is detected using a
     * traversal in this compressed space, then the remaining compressed points are mapped to the corresponding
     * rectangles, and the sum of their areas are calculated.
     */
    private static long solve(List<String> lines, int part) {
        var points = parse(lines, part);

        // Sort the x and y coordinates of the original corner points of the loop
        var xs = points.stream().mapToInt(Point::x).sorted().distinct().toArray();
        var ys = points.stream().mapToInt(Point::y).sorted().distinct().toArray();

        // Function to map an original corner point to the corresponding compressed point
        Function<Point, Point> compress =
                p -> new Point(Arrays.binarySearch(xs, p.x()) * 2 + 1, Arrays.binarySearch(ys, p.y()) * 2 + 1);

        // Collect all compressed points of the loop (not only the corners)
        var loop = new HashSet<Point>();
        for (int i = 0; i < points.size() - 1; i++) {
            var a = compress.apply(points.get(i));
            var b = compress.apply(points.get(i + 1));
            a.lineTo(b).forEach(loop::add);
        }

        // Find the compressed points that are outside the loop
        var box = Point.box(xs.length * 2 + 1, ys.length * 2 + 1).collect(Collectors.toSet());
        var outside = Bfs.run(new Point(0, 0),
                p -> p.neighbors().filter(n -> box.contains(n) && !loop.contains(n)).toList()).keySet();

        // Calculate the sum of the areas of the rectangles that correspond to the relevant compressed points
        return box.stream()
                .filter(p -> !outside.contains(p))
                .mapToLong(p -> {
                    long xSize = p.x() % 2 == 1 ? 1 : (xs[p.x() / 2] - xs[(p.x() - 2) / 2]) - 1;
                    long ySize = p.y() % 2 == 1 ? 1 : (ys[p.y() / 2] - ys[(p.y() - 2) / 2]) - 1;
                    return xSize * ySize;
                }).sum();
    }

    /**
     * Another solution that uses a similar approach for detecting interior area as the "row scanning" solution for
     * Day 10. We assume that the instructions describe a <i>simple polygon</i>: it does not intersect with itself.
     * <p>
     * The same characters are used to represent the loop (|, L, 7, F, J) as for Day 10, but horizontal borders (-)
     * are not stored, only counted. The positions of |, L, 7, F, J characters are collected for the rows, but to
     * make it efficient, consecutive rows with the same pattern are merged and represented as a range of y
     * coordinates.
     */
    private static long solveWithRowScanning(List<String> lines, int part) {
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

    /**
     * The most advanced solution based on the <a href="https://en.wikipedia.org/wiki/Shoelace_formula">Trapezoid
     * Shoelace formula</a> and <a href="https://en.wikipedia.org/wiki/Pick%27s_theorem">Pick's theorem</a>.
     * We assume that the instructions describe a <i>simple polygon</i>: it does not intersect with itself.
     * <p>
     * <b>Trapezoid Shoelace formula:</b> given the vertices (corners) of a simple polygon in counterclockwise order,
     * the area of the polygon can be calculated as: {@code 1/2 * SUM_{i in 1..n} ((y[i] + y[i+1]) * (x[i] - x[i+1]))},
     * where the (n+1)-th vertex is the same as the first one. If the vertices are in clockwise order, then this sum
     * is negative, and its absolute value is the area of the polygon.
     * <p>
     * <b>Pick's theorem:</b> given a simple polygon that has integer coordinates for all of its vertices (corners),
     * let {@code A} denote the area of the polygon, let {@code I} denote the number of integer points interior
     * to the polygon, and let {@code B} denote the number of integer points on its boundary (including both vertices
     * and points along the sides). Then {@code A = I + B/2 - 1}.
     * <p>
     * Our task is to calculate {@code I + B}, for which we can derive the formula {@code I + B = A + B/2 + 1} from
     * Pick's theorem, and {@code A} is calculated using the Trapezoid Shoelace formula.
     */
    private static long solveWithFormula(List<String> lines, int part) {
        var points = parse(lines, part);

        long sum = 0;
        long border = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            var a = points.get(i);
            var b = points.get(i + 1);
            sum += (long) (a.y() + b.y()) * (a.x() - b.x());
            border += a.dist1(b);
        }
        return (Math.abs(sum) + border) / 2 + 1;
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
        if (!pos.equals(list.getFirst())) {
            throw new IllegalArgumentException("Not a closed loop.");
        }
        return list;
    }

    private static char dir(Point from, Point to) {
        return to.x() == from.x() ? (to.y() < from.y() ? 'U' : 'D') : (to.x() < from.x() ? 'L' : 'R');
    }

}
