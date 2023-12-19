package com.github.pkovacs.aoc.y2023;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import com.github.pkovacs.aoc.AbstractDay;
import com.github.pkovacs.util.Utils;
import com.github.pkovacs.util.data.Range;

public class Day19 extends AbstractDay {

    private static final int MAX = 4000;

    public static void main(String[] args) {
        var blocks = readLineBlocks(getInputPath());
        var workflows = parseWorkflows(blocks.get(0));
        var parts = blocks.get(1).stream().map(Utils::parseLongs).toList();

        System.out.println("Part 1: " + solve1(workflows, parts));
        System.out.println("Part 2: " + solve2(workflows));
    }

    private static long solve1(Map<String, List<Rule>> workflows, List<long[]> parts) {
        return parts.stream().filter(p -> isAccepted(workflows, p)).mapToLong(p -> LongStream.of(p).sum()).sum();
    }

    private static boolean isAccepted(Map<String, List<Rule>> workflows, long[] part) {
        var current = "in";
        while (!current.equals("A") && !current.equals("R")) {
            current = workflows.get(current).stream()
                    .filter(rule -> rule.accepted.contains(part[rule.index]))
                    .findFirst().orElseThrow().target;
        }
        return current.equals("A");
    }

    private static long solve2(Map<String, List<Rule>> workflows) {
        return countAccepted(workflows, "in", Collections.nCopies(4, new Range(1, MAX)).toArray(Range[]::new));
    }

    private static long countAccepted(Map<String, List<Rule>> workflows, String current, Range[] ranges) {
        // We assume that there is no cycle in the rules, so any combination of ratings eventually leads to A or R
        if (current.equals("R") || Arrays.stream(ranges).anyMatch(Range::isEmpty)) {
            return 0;
        } else if (current.equals("A")) {
            return Arrays.stream(ranges).mapToLong(Range::count).reduce(1, (a, b) -> a * b);
        }

        long count = 0;
        for (var rule : workflows.get(current)) {
            var rng = ranges[rule.index];
            ranges[rule.index] = rng.intersection(rule.accepted);
            count += countAccepted(workflows, rule.target, ranges.clone());
            ranges[rule.index] = rng.intersection(rule.rejected);
        }
        return count;
    }

    private static Map<String, List<Rule>> parseWorkflows(List<String> lines) {
        var map = new HashMap<String, List<Rule>>();
        for (var line : lines) {
            var name = line.substring(0, line.indexOf('{'));
            var rules = line.substring(line.indexOf('{') + 1, line.indexOf('}')).split(",");
            map.put(name, Arrays.stream(rules).map(Rule::fromString).toList());
        }
        return map;
    }

    private record Rule(int index, Range accepted, Range rejected, String target) {
        static Rule fromString(String str) {
            if (str.contains(":")) {
                var parts = str.split(":");
                int index = "xmas".indexOf(str.charAt(0));
                long value = Long.parseLong(parts[0].substring(2));
                return str.charAt(1) == '<'
                        ? new Rule(index, new Range(1, value - 1), new Range(value, MAX), parts[1])
                        : new Rule(index, new Range(value + 1, MAX), new Range(1, value), parts[1]);
            } else {
                return new Rule(0, new Range(1, MAX), new Range(1, 0), str);
            }
        }
    }

}
