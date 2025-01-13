package com.github.pkovacs.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Implements <a href="https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Dijkstra's algorithm</a> for
 * finding shortest paths in weighted graphs. This algorithm only supports non-negative edge weights.
 * If you also need negative weights, use {@link BellmanFord} instead.
 * <p>
 * The input is a directed or undirected {@link WeightedGraph} and one or more source nodes. For each execution of
 * the algorithm, it advances from any node of the graph at most once.
 * <p>
 * Some methods also take a target predicate as an argument in order to find a path to a single target node
 * instead of all nodes. In this case, the algorithm terminates when one of the shortest paths is found for a
 * target node having minimum distance from the (nearest) source node. This way, paths can also be searched for
 * in huge or (theoretically) infinite graphs provided that the nodes and edges are generated on-the-fly when
 * requested by the algorithm. For example, the nodes and edges might represent the feasible states and steps of
 * a game or a combinatorial problem, respectively, and we might not be able to or do not want to enumerate all
 * possible (and reachable) states in advance.
 *
 * @see Bfs
 * @see BellmanFord
 */
public final class Dijkstra {

    private Dijkstra() {
    }

    /**
     * Calculates the distance (total edge weight) along one of the shortest paths from the given source node to
     * the nearest target node identified by the given predicate. If you are not sure if a target node is actually
     * reachable from the source, use {@link #findPath} instead.
     *
     * @param graph the {@link WeightedGraph} that provides the outgoing weighted edges for each node.
     * @param source the source node.
     * @param targetPredicate a predicate that returns true for the target node(s). It can accept multiple
     *         nodes, in which case one of the shortest paths to one of the nearest target nodes is to be found.
     *         For a single target node {@code t}, you can simply use {@code t::equals}.
     * @return the distance (total edge weight) along a shortest path from the source node to the nearest target node.
     * @throws java.util.NoSuchElementException if no target nodes are reachable from the source node.
     */
    public static <T> long dist(WeightedGraph<T> graph, T source, Predicate<? super T> targetPredicate) {
        return findPath(graph, source, targetPredicate).orElseThrow().dist();
    }

    /**
     * Finds one of the shortest paths (in terms of the total edge weight) from the given source node to a target node
     * identified by the given predicate.
     *
     * @param graph the {@link WeightedGraph} that provides the outgoing weighted edges for each node.
     * @param source the source node.
     * @param targetPredicate a predicate that returns true for the target node(s). It can accept multiple
     *         nodes, in which case one of the shortest paths to one of the nearest target nodes is to be found.
     *         For a single target node {@code t}, you can simply use {@code t::equals}.
     * @return a shortest {@link Path} to the nearest target node or an empty optional if no target nodes are
     *         reachable from the source node.
     */
    public static <T> Optional<Path<T>> findPath(WeightedGraph<T> graph, T source,
            Predicate<? super T> targetPredicate) {
        return findPathFromAny(graph, Stream.of(source), targetPredicate);
    }

    /**
     * Finds one of the shortest paths (in terms of the total edge weight) from any of the given source nodes to
     * a target node identified by the given predicate.
     *
     * @param graph the {@link WeightedGraph} that provides the outgoing weighted edges for each node.
     * @param sources the source nodes.
     * @param targetPredicate a predicate that returns true for the target node(s). It can accept multiple
     *         nodes, in which case one of the shortest paths to one of the nearest target nodes is to be found.
     *         For a single target node {@code t}, you can simply use {@code t::equals}.
     * @return a shortest {@link Path} to the nearest target node or an empty optional if no target nodes are
     *         reachable from the source nodes.
     */
    public static <T> Optional<Path<T>> findPathFromAny(WeightedGraph<T> graph, Stream<? extends T> sources,
            Predicate<? super T> targetPredicate) {
        var results = new HashMap<T, Path<T>>();
        return run(graph, sources, targetPredicate, results);
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
        run(graph, sources, u -> false, results);
        return results;
    }

    private static <T> Optional<Path<T>> run(WeightedGraph<T> graph, Stream<? extends T> sources,
            Predicate<? super T> targetPredicate, HashMap<T, Path<T>> results) {
        var queue = new PriorityQueue<Path<T>>(Comparator.comparing(Path::dist));
        sources.forEach(s -> {
            var path = new Path<T>(s, 0, null);
            results.put(s, path);
            queue.add(path);
        });

        var decreased = new HashSet<T>();
        var processed = new HashSet<T>();
        while (!queue.isEmpty()) {
            var prev = queue.poll();
            var prevNode = prev.end();
            if (targetPredicate.test(prevNode)) {
                return Optional.of(prev);
            }
            if (decreased.contains(prevNode) && !processed.add(prevNode)) {
                continue;
            }

            graph.edges(prevNode).forEach(edge -> {
                var node = edge.end();
                var current = results.get(node);
                long dist = prev.dist() + edge.weight();
                if (current == null || dist < current.dist()) {
                    var path = new Path<>(node, dist, prev);
                    results.put(node, path);
                    queue.add(path);
                    if (current != null) {
                        decreased.add(node);
                    }
                }
            });
        }

        return Optional.empty();
    }

}
