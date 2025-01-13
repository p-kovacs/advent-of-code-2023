package com.github.pkovacs.util;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Implements an efficient version of the Bellman-Ford algorithm, which is known as the
 * <a href="https://en.wikipedia.org/wiki/Shortest_Path_Faster_Algorithm">SPFA algorithm</a> for finding shortest
 * paths in weighted graphs. This algorithm is significantly slower than {@link Dijkstra}, but it also supports
 * negative edge weights. However, the graph must not contain a <i>directed cycle of negative total weight</i>.
 * The current implementation does not terminate if such a cycle is reachable from the source node(s).
 * <p>
 * The input is a directed or undirected {@link WeightedGraph} and one or more source nodes.
 *
 * @see Dijkstra
 */
public final class BellmanFord {

    private BellmanFord() {
    }

    /**
     * Finds shortest paths (in terms of the total edge weight) to all nodes reachable from the given source node.
     *
     * @param graph the {@link WeightedGraph} that provides the outgoing weighted edges for each node.
     * @param source the source node.
     * @return a map that associates a shortest {@link Path} with each node reachable from the source node.
     */
    public static <T> Map<T, Path<T>> findPaths(WeightedGraph<T> graph, T source) {
        return findPathsFromAny(graph, Stream.of(source));
    }

    /**
     * Finds shortest paths (in terms of the total edge weight) to all nodes reachable from any of the given
     * source nodes.
     *
     * @param graph the {@link WeightedGraph} that provides the outgoing weighted edges for each node.
     * @param sources the source nodes.
     * @return a map that associates a shortest {@link Path} with each node reachable from the source nodes.
     */
    public static <T> Map<T, Path<T>> findPathsFromAny(WeightedGraph<T> graph, Stream<? extends T> sources) {
        var results = new HashMap<T, Path<T>>();

        var queue = new ArrayDeque<Path<T>>();
        sources.forEach(s -> {
            var path = new Path<T>(s, 0, null);
            results.put(s, path);
            queue.add(path);
        });

        while (!queue.isEmpty()) {
            var prev = queue.poll();
            graph.edges(prev.end()).forEach(edge -> {
                var node = edge.end();
                long dist = prev.dist() + edge.weight();
                var currentPath = results.get(node);
                if (currentPath == null || dist < currentPath.dist()) {
                    var path = new Path<>(node, dist, prev);
                    results.put(node, path);
                    queue.add(path);
                }
            });
        }

        return results;
    }

}
