package com.github.pkovacs.aoc.y2023;

import java.util.List;
import java.util.stream.LongStream;

import com.github.pkovacs.aoc.AocUtils;
import com.github.pkovacs.util.Utils;

public class Day06 {

    public static void main(String[] args) {
        var lines = Utils.readLines(AocUtils.getInputPath());

        System.out.println("Part 1: " + solve(lines, 1));
        System.out.println("Part 2: " + solve(lines, 2));
    }

    private static long solve(List<String> lines, int part) {
        var times = parseLine(lines.get(0), part);
        var distances = parseLine(lines.get(1), part);

        long ans = 1;
        for (int i = 0; i < times.length; i++) {
            long t = times[i];
            long d = distances[i];
            ans *= LongStream.range(1, t).filter(v -> v * (t - v) > d).count();
        }
        return ans;
    }

    private static long[] parseLine(String line, int part) {
        return Utils.parseLongs(part == 1 ? line : line.replace(" ", ""));
    }

}
