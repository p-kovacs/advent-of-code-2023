package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.pkovacs.util.alg.Bfs;
import com.github.pkovacs.util.data.CounterMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

public class Day25 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        System.out.println("Part 1: " + solve(lines));
        System.out.println("Part 2: " + 0);
    }

    private static int solve(List<String> lines) {
        // Read the graph
        Multimap<String, String> graph = MultimapBuilder.hashKeys().arrayListValues().build();
        for (var line : lines) {
            var from = line.split(": ")[0];
            for (var to : line.split(": ")[1].split(" ")) {
                graph.put(from, to);
                graph.put(to, from);
            }
        }

        // Iteratively find s-t min-cuts until a cut of size 3 is found
        var nodes = new ArrayList<>(graph.keySet());
        var s = nodes.getFirst();
        for (var node : graph.keySet()) {
            if (!node.equals(s)) {
                var comp = findMinCut(graph, s, node);
                long cut = graph.entries().stream()
                        .filter(e -> comp.contains(e.getKey()) && !comp.contains(e.getValue()))
                        .count();
                if (cut == 3) {
                    return comp.size() * (nodes.size() - comp.size());
                }
            }
        }

        return 0; // not reached
    }

    /**
     * Finds a minimum s-t cut using Edmonds-Karp algorithm. Edge capacities are considered to be uniformly 1.
     */
    private static Set<String> findMinCut(Multimap<String, String> graph, String s, String t) {
        if (s.equals(t)) {
            throw new IllegalArgumentException("Source and target must be different.");
        }

        // Set initial residual capacities: 1 for each edge
        var residualCapacity = new CounterMap<Edge>();
        graph.forEach((u, v) -> residualCapacity.put(new Edge(u, v), 1));

        // Find max-flow and min-cut using the Edmonds-Karp algorithm
        while (true) {
            // Find augmenting path from s to t using BFS
            var result = Bfs.run(s,
                    u -> graph.get(u).stream().filter(v -> residualCapacity.get(new Edge(u, v)) > 0).toList());
            var path = result.get(t);

            // If no more paths can be found, return the result (the component of s)
            if (path == null) {
                return result.keySet();
            }

            // Adjust residual capacities along the augmenting path
            for (int i = 0; i < path.dist(); i++) {
                var u = path.nodes().get(i);
                var v = path.nodes().get(i + 1);
                residualCapacity.dec(new Edge(u, v));
                residualCapacity.inc(new Edge(v, u));
            }
        }
    }

    private record Edge(String u, String v) {}

}
