package com.github.pkovacs.aoc.y2023;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.IntStream;

public class Day15 extends AbstractDay {

    public static void main(String[] args) {
        var line = readFirstLine(getInputPath());
        var steps = Arrays.stream(line.split(",")).toList();

        System.out.println("Part 1: " + solve1(steps));
        System.out.println("Part 2: " + solve2(steps));
    }

    private static int solve1(List<String> steps) {
        return steps.stream().mapToInt(Day15::hash).sum();
    }

    private static int solve2(List<String> steps) {
        var boxes = IntStream.range(0, 256).mapToObj(i -> new LinkedHashMap<String, Integer>()).toList();
        for (var step : steps) {
            if (step.endsWith("-")) {
                var label = step.substring(0, step.length() - 1);
                boxes.get(hash(label)).remove(label);
            } else {
                var label = step.split("=")[0];
                int power = Integer.parseInt(step.split("=")[1]);
                boxes.get(hash(label)).put(label, power);
            }
        }

        int ans = 0;
        for (int i = 0; i < 256; i++) {
            int j = 0;
            for (var entry : boxes.get(i).entrySet()) {
                ans += (i + 1) * (++j) * entry.getValue();
            }
        }
        return ans;
    }

    private static int hash(String str) {
        return str.chars().reduce(0, (a, b) -> (a + b) * 17 % 256);
    }

}
