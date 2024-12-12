package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.github.pkovacs.util.Utils;
import com.github.pkovacs.util.data.Range;

public class Day05 extends AbstractDay {

    public static void main(String[] args) {
        var blocks = readLineBlocks(getInputPath());

        // Collect initial ranges for part 1 and part 2
        var seeds = parseLongs(blocks.getFirst().getFirst());
        var ranges1 = Arrays.stream(seeds).mapToObj(s -> new Range(s, s)).toList();
        var ranges2 = new ArrayList<Range>();
        for (int i = 0; i < seeds.length; i += 2) {
            ranges2.add(new Range(seeds[i], seeds[i] + seeds[i + 1] - 1));
        }

        // Parse the map for each conversion phase as a list of RangeConverter objects
        var maps = new ArrayList<List<RangeConverter>>();
        for (int i = 1; i < blocks.size(); i++) {
            maps.add(blocks.get(i).stream().skip(1)
                    .map(Utils::parseLongs)
                    .map(v -> new RangeConverter(new Range(v[1], v[1] + v[2] - 1), v[0] - v[1]))
                    .toList());
        }

        System.out.println("Part 1: " + solve(ranges1, maps));
        System.out.println("Part 2: " + solve(ranges2, maps));
    }

    private static long solve(List<Range> seedRanges, List<List<RangeConverter>> maps) {
        var ranges = seedRanges;
        for (var map : maps) {
            // Split the ranges as necessary so that each of them is either disjoint from or fully contained by
            // the source range of any converter in the current map
            for (var converter : map) {
                ranges = ranges.stream()
                        .flatMap(r -> split(r, converter.source))
                        .filter(r -> !r.isEmpty())
                        .toList();
            }

            // Convert the ranges by shifting them as necessary
            ranges = ranges.stream().map(r -> convert(r, map)).toList();
        }

        return ranges.stream().mapToLong(Range::min).min().orElseThrow();
    }

    private static Stream<Range> split(Range range, Range source) {
        var in = range.intersection(source);
        return in.isEmpty()
                ? Stream.of(range)
                : Stream.of(new Range(range.min(), in.min() - 1), in, new Range(in.max() + 1, range.max()));
    }

    private static Range convert(Range range, List<RangeConverter> map) {
        var converter = map.stream().filter(c -> c.source.containsAll(range)).findFirst();
        return converter.isEmpty() ? range : range.shift(converter.get().delta);
    }

    private record RangeConverter(Range source, long delta) {}

}
