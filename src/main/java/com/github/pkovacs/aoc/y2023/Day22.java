package com.github.pkovacs.aoc.y2023;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.pkovacs.aoc.AbstractDay;
import com.github.pkovacs.util.data.Range;

public class Day22 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        // Read the bricks
        var bricks = lines.stream().map(line -> new Brick(parseLongs(line))).toList();

        // Calculate "below" relations: below.get(a) is the set of bricks that are below `a`
        var below = new HashMap<Brick, Set<Brick>>();
        for (var a : bricks) {
            below.put(a, bricks.stream().filter(b -> b != a && b.isBelow(a)).collect(Collectors.toSet()));
        }

        // Calculate new z coordinates of the bricks after they fall down
        var settled = new HashSet<Brick>();
        while (settled.size() < bricks.size()) {
            var next = bricks.stream()
                    .filter(b -> !settled.contains(b) && settled.containsAll(below.get(b)))
                    .findFirst().orElseThrow();
            long newMinZ = below.get(next).stream().mapToLong(b -> b.z.max()).max().orElse(0) + 1;
            next.shiftZ(newMinZ - next.z.min());
            settled.add(next);
        }

        // Calculate "support" relations: supports.get(a) is the set of bricks that supports `a`, while
        // supportedBy.get(a) is the set of bricks that are supported by `a`
        var supports = new HashMap<Brick, Set<Brick>>();
        var supportedBy = new HashMap<Brick, Set<Brick>>();
        for (var a : settled) {
            supports.put(a, below.get(a).stream().filter(b -> b.z.max() + 1 == a.z.min()).collect(Collectors.toSet()));
            supportedBy.put(a, new HashSet<>());
        }
        for (var a : settled) {
            supports.get(a).forEach(b -> supportedBy.get(b).add(a));
        }

        // Solve both parts
        int ans1 = 0;
        int ans2 = 0;
        for (var a : settled) {
            var wouldFall = new HashSet<Brick>();
            wouldFall.add(a);
            var queue = new ArrayDeque<Brick>();
            queue.add(a);
            while (!queue.isEmpty()) {
                var b = queue.remove();
                var list = supportedBy.get(b).stream().filter(c -> wouldFall.containsAll(supports.get(c))).toList();
                wouldFall.addAll(list);
                queue.addAll(list);
            }
            ans1 += wouldFall.size() == 1 ? 1 : 0;
            ans2 += wouldFall.size() - 1;
        }

        System.out.println("Part 1: " + ans1);
        System.out.println("Part 2: " + ans2);
    }

    private static class Brick {

        final Range x;
        final Range y;
        Range z;

        Brick(long[] coords) {
            x = new Range(coords[0], coords[3]);
            y = new Range(coords[1], coords[4]);
            z = new Range(coords[2], coords[5]);
        }

        boolean isBelow(Brick other) {
            return z.max() < other.z.min() && x.overlaps(other.x) && y.overlaps(other.y);
        }

        void shiftZ(long delta) {
            z = z.shift(delta);
        }

    }

}
