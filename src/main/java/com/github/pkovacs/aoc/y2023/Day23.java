package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.pkovacs.util.Bfs;
import com.github.pkovacs.util.CharTable;
import com.github.pkovacs.util.Pos;

public class Day23 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        System.out.println("Part 1: " + new HikingTrails(lines, 1).findLongestHike());
        System.out.println("Part 2: " + new HikingTrails(lines, 2).findLongestHike());
    }

    private static class HikingTrails {

        record Path(int to, int dist) {}

        final CharTable table;
        final int source;
        final int target;
        final List<Pos> junctions = new ArrayList<>();
        final List<List<Path>> junctionPaths = new ArrayList<>();

        HikingTrails(List<String> lines, int part) {
            table = new CharTable(lines);

            // Collect junction nodes
            source = 0;
            target = 1;
            junctions.add(table.firstRow().filter(p -> table.get(p) == '.').findFirst().orElseThrow());
            junctions.add(table.lastRow().filter(p -> table.get(p) == '.').findFirst().orElseThrow());
            table.cells()
                    .filter(p -> table.get(p) != '#')
                    .filter(p -> table.neighbors(p).filter(n -> table.get(n) != '#').count() > 2)
                    .forEach(junctions::add);

            // Collect paths between junctions
            var junctionSet = new HashSet<>(junctions);
            IntStream.range(0, junctions.size()).forEach(i -> {
                var current = junctions.get(i);
                var bfsResult = Bfs.findPaths(p -> {
                    var next = part == 1 && table.get(p) != '.'
                            ? List.of(p.neighbor(table.get(p)))
                            : table.neighbors(p).filter(n -> table.get(n) != '#').toList();
                    return !p.equals(current) && next.size() > 2 ? Stream.of() : next.stream();
                }, current);
                junctionPaths.add(bfsResult.entrySet().stream()
                        .filter(e -> junctionSet.contains(e.getKey()) && !e.getKey().equals(current))
                        .map(e -> new Path(junctions.indexOf(e.getKey()), (int) e.getValue().dist()))
                        .toList());
            });

            // Check assumption: at most 64 junction nodes (to represent sets of them using bits of a long value)
            if (junctions.size() > Long.BYTES * 8) {
                throw new IllegalArgumentException("Too many junction nodes.");
            }
        }

        long findLongestHike() {
            return findLongestPath(source, 0, 1L << source);
        }

        private long findLongestPath(int current, int dist, long nodes) {
            if (current == target) {
                return dist;
            }

            long result = -1;
            for (var path : junctionPaths.get(current)) {
                long bit = 1L << path.to;
                if ((nodes & bit) == 0) {
                    result = max(result, findLongestPath(path.to, dist + path.dist, nodes | bit));
                }
            }
            return result;
        }

    }

}
