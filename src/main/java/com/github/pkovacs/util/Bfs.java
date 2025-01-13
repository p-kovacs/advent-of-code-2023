package com.github.pkovacs.util;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Implements the BFS (breadth-first search) algorithm for traversing graphs and finding shortest paths
 * (in terms of the number of edges).
 * <p>
 * The input is a directed or undirected {@link Graph} and one or more source nodes. For each execution of
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
 * @see Dijkstra
 * @see BellmanFord
 */
public final class Bfs {

    private Bfs() {
    }

    /**
     * Calculates the distance (number of edges) along one of the shortest paths from the given source node to
     * the nearest target node identified by the given predicate. If you are not sure if a target node is actually
     * reachable from the source, use {@link #findPath} instead.
     *
     * @param graph the {@link Graph} that provides the neighbor nodes for each node.
     * @param source the source node.
     * @param targetPredicate a predicate that returns true for the target node(s). It can accept multiple
     *         nodes, in which case one of the shortest paths to one of the nearest target nodes is to be found.
     *         For a single target node {@code t}, you can simply use {@code t::equals}.
     * @return the distance (number of edges) along a shortest path from the source node to the nearest target node.
     * @throws java.util.NoSuchElementException if no target nodes are reachable from the source node.
     */
    public static <T> long dist(Graph<T> graph, T source, Predicate<? super T> targetPredicate) {
        return findPath(graph, source, targetPredicate).orElseThrow().dist();
    }

    /**
     * Finds one of the shortest paths (in terms of the number of edges) from the given source node to a target node
     * identified by the given predicate.
     *
     * @param graph the {@link Graph} that provides the neighbor nodes for each node.
     * @param source the source node.
     * @param targetPredicate a predicate that returns true for the target node(s). It can accept multiple
     *         nodes, in which case one of the shortest paths to one of the nearest target nodes is to be found.
     *         For a single target node {@code t}, you can simply use {@code t::equals}.
     * @return a shortest {@link Path} to the nearest target node or an empty optional if no target nodes are
     *         reachable from the source node.
     */
    public static <T> Optional<Path<T>> findPath(Graph<T> graph, T source, Predicate<? super T> targetPredicate) {
        return findPathFromAny(graph, Stream.of(source), targetPredicate);
    }

    /**
     * Finds one of the shortest paths (in terms of the number of edges) from any of the given source nodes to
     * a target node identified by the given predicate.
     *
     * @param graph the {@link Graph} that provides the neighbor nodes for each node.
     * @param sources the source nodes.
     * @param targetPredicate a predicate that returns true for the target node(s). It can accept multiple
     *         nodes, in which case one of the shortest paths to one of the nearest target nodes is to be found.
     *         For a single target node {@code t}, you can simply use {@code t::equals}.
     * @return a shortest {@link Path} to the nearest target node or an empty optional if no target nodes are
     *         reachable from the source nodes.
     */
    public static <T> Optional<Path<T>> findPathFromAny(Graph<T> graph, Stream<? extends T> sources,
            Predicate<? super T> targetPredicate) {
        var results = new HashMap<T, Path<T>>();
        return run(graph, sources, targetPredicate, results);
    }

    /**
     * Finds shortest paths (in terms of the number of edges) to all nodes reachable from the given source node.
     *
     * @param graph the {@link Graph} that provides the neighbor nodes for each node.
     * @param source the source node.
     * @return a map that associates a shortest {@link Path} with each node reachable from the source node.
     */
    public static <T> Map<T, Path<T>> findPaths(Graph<T> graph, T source) {
        return findPathsFromAny(graph, Stream.of(source));
    }

    /**
     * Finds shortest paths (in terms of the number of edges) to all nodes reachable from any of the given
     * source nodes.
     *
     * @param graph the {@link Graph} that provides the neighbor nodes for each node.
     * @param sources the source nodes.
     * @return a map that associates a shortest {@link Path} with each node reachable from the source nodes.
     */
    public static <T> Map<T, Path<T>> findPathsFromAny(Graph<T> graph, Stream<? extends T> sources) {
        var results = new HashMap<T, Path<T>>();
        run(graph, sources, u -> false, results);
        return results;
    }

    private static <T> Optional<Path<T>> run(Graph<T> graph, Stream<? extends T> sources,
            Predicate<? super T> targetPredicate, HashMap<T, Path<T>> results) {
        var queue = new ArrayDeque<Path<T>>();
        sources.forEach(s -> {
            var path = new Path<T>(s, 0, null);
            results.put(s, path);
            queue.add(path);
        });

        while (!queue.isEmpty()) {
            var prev = queue.poll();
            if (targetPredicate.test(prev.end())) {
                return Optional.of(prev);
            }

            graph.neighbors(prev.end()).filter(node -> !results.containsKey(node)).forEach(node -> {
                var path = new Path<>(node, prev.dist() + 1, prev);
                results.put(node, path);
                queue.add(path);
            });
        }

        return Optional.empty();
    }

}
