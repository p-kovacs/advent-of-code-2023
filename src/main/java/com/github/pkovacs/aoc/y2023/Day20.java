package com.github.pkovacs.aoc.y2023;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.pkovacs.aoc.AbstractDay;

public class Day20 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        var config = new Configuration(lines);
        config.calculate();

        System.out.println("Part 1: " + config.lowCount * config.highCount);
        System.out.println("Part 2: " + lcm(config.cycleLength.values()));
    }

    private static class Configuration {

        static final String BROADCASTER = "broadcaster";

        final Map<String, Node> nodes = new HashMap<>();
        final Map<String, List<String>> graph = new HashMap<>();

        long lowCount = 0;
        long highCount = 0;
        final Map<String, Long> cycleLength = new HashMap<>();

        Configuration(List<String> lines) {
            // Parse input lines
            for (var line : lines) {
                var parts = line.split(" -> ");
                boolean isBroadcaster = BROADCASTER.equals(parts[0]);
                var name = isBroadcaster ? BROADCASTER : parts[0].substring(1);
                var type = isBroadcaster ? '*' : line.charAt(0);
                nodes.put(name, new Node(name, type, new HashMap<>()));
                graph.put(name, Arrays.stream(parts[1].split(", ")).toList());
            }

            // Add nodes that only occur as target nodes (with unknown type)
            graph.values().stream().flatMap(List::stream)
                    .filter(name -> !nodes.containsKey(name))
                    .forEach(name -> nodes.put(name, new Node(name, '?', new HashMap<>())));

            // Initialize the "memory" of the nodes.
            // For the sake of simplicity, the on/off state of a flip-flop node is represented as an input from itself.
            nodes.values().stream()
                    .filter(node -> node.type == '%')
                    .forEach(node -> node.input.put(node.name, PulseType.LOW));
            graph.forEach((from, list) ->
                    list.forEach(to -> nodes.get(to).input.put(from, PulseType.LOW)));
        }

        /**
         * Performs iterations to solve both parts simultaneously.
         * <p>
         * For part 1, we simply count the low/high pulses in the first 1000 iterations. For part 2, however,
         * we need to make particular assumptions (similarly to Day 8). It seems that each input file describes
         * a configuration in which a single conjunction node sends pulse to rx. Let's call this node the "sink".
         * Furthermore, each input node of the sink sends a high pulse in every k-th iteration. Therefore, the
         * answer for part 2 is the LCM of these cycle lengths. This solution exploits these assumptions without
         * checking them.
         * <p>
         * In fact, it seems that each configuration actually combines 4 independent subsystems between the
         * broadcaster and the sink, and each subsystem independently produces a high pulse input for the sink
         * in every k-th iteration, where k is a prime number around 4000. However, these more strict assumptions
         * (separability, prime numbers) are not exploited in this code.
         */
        void calculate() {
            var sink = graph.keySet().stream().filter(n -> graph.get(n).contains("rx")).findFirst().orElseThrow();
            for (long it = 1; true; it++) {
                var queue = new ArrayDeque<Pulse>();
                queue.add(new Pulse("button", BROADCASTER, PulseType.LOW));
                while (!queue.isEmpty()) {
                    var pulse = queue.remove();
                    var name = pulse.to;
                    var node = nodes.get(name);

                    // Collect pulse counts for part 1
                    if (it <= 1000) {
                        if (pulse.type == PulseType.LOW) {
                            lowCount++;
                        } else {
                            highCount++;
                        }
                    }

                    // Collect cycle sizes for part 2
                    if (name.equals(sink) && pulse.type == PulseType.HIGH && !cycleLength.containsKey(pulse.from)) {
                        cycleLength.put(pulse.from, it);
                    }

                    // Process the current pulse
                    switch (node.type) {
                        case '%' -> {
                            if (pulse.type == PulseType.LOW) {
                                var type = node.input.get(name) == PulseType.LOW ? PulseType.HIGH : PulseType.LOW;
                                node.input.put(name, type);
                                graph.get(name).forEach(next -> queue.add(new Pulse(name, next, type)));
                            }
                        }
                        case '&' -> {
                            node.input.put(pulse.from, pulse.type);
                            boolean allHigh = node.input.values().stream().allMatch(v -> v == PulseType.HIGH);
                            var type = allHigh ? PulseType.LOW : PulseType.HIGH;
                            graph.get(name).forEach(next -> queue.add(new Pulse(name, next, type)));
                        }
                        case '*' -> graph.get(name).forEach(next -> queue.add(new Pulse(name, next, PulseType.LOW)));
                        default -> {  // unknown type, no action
                        }
                    }
                }

                if (it >= 1000 && cycleLength.size() == nodes.get(sink).input.size()) {
                    break; // we are ready for both parts: 1000+ iterations and each cycle length is collected
                }
            }
        }

    }

    private enum PulseType {
        LOW, HIGH
    }

    private record Node(String name, char type, Map<String, PulseType> input) {}

    private record Pulse(String from, String to, PulseType type) {}

    private static long lcm(Collection<Long> values) {
        long ans = 1;
        for (var v : values) {
            ans = ans / LongMath.gcd(ans, v) * v;
        }
        return ans;
    }

}
