package com.github.pkovacs.util.alg;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A general implementation of the BFS (breadth-first search) algorithm.
 * <p>
 * The input is a directed or undirected graph (implicitly defined by a neighbor provider function) and one or more
 * source nodes. The neighbor provider function has to provide for each node {@code u} the neighbor nodes directly
 * reachable form {@code u} via its outgoing edges. This function is applied at most once for each node, when the
 * algorithm advances from that node.
 * <p>
 * A target predicate can also be used in order to find path to a single target node instead of all nodes. The
 * algorithm terminates when a shortest path is found for a target node having minimum distance from the (nearest)
 * source node. This way, paths can also be searched in huge or (theoretically) infinite graphs provided that the
 * edges are generated on-the-fly when requested by the algorithm. For example, nodes and edges might represent
 * feasible states and steps of a combinatorial problem, and we might not be able to or do not want to enumerate
 * all possible (and reachable) states in advance.
 *
 * @see Dijkstra
 * @see BellmanFord
 */
public final class Bfs {

    private Bfs() {
    }

    /**
     * Calculates the distance (in terms the number of edges) along a shortest path from the given source node
     * to the nearest target node specified by the given predicate.
     * For more details, see {@link #findPath(Object, Function, Predicate)}.
     *
     * @throws java.util.NoSuchElementException if no target nodes are reachable from the source node.
     */
    public static <T> int dist(T source,
            Function<? super T, ? extends Iterable<T>> neighborProvider,
            Predicate<? super T> targetPredicate) {
        return (int) findPath(source, neighborProvider, targetPredicate).orElseThrow().dist();
    }

    /**
     * Finds a shortest path (in terms of the number of edges) from the given source node to a target node specified
     * by the given predicate.
     *
     * @param source the source node.
     * @param neighborProvider the neighbor provider function. For each node {@code u}, it has to provide the
     *         end nodes of the outgoing edges of {@code u} as a collection.
     * @param targetPredicate a predicate that returns true for the target node(s). It can accept multiple
     *         nodes, in which case a shortest path to one of the nearest target nodes is to be found.
     *         However, for a single target node {@code t}, you can simply use {@code t::equals}.
     * @return a shortest {@link Path} to the nearest target node or an empty optional if no target nodes are
     *         reachable from the source node.
     */
    public static <T> Optional<Path<T>> findPath(T source,
            Function<? super T, ? extends Iterable<T>> neighborProvider,
            Predicate<? super T> targetPredicate) {
        return findPathFromAny(List.of(source), neighborProvider, targetPredicate);
    }

    /**
     * Finds a shortest path (in terms of the number of edges) from any of the given source nodes to a target node
     * specified by the given predicate.
     *
     * @param sources the source nodes.
     * @param neighborProvider the neighbor provider function. For each node {@code u}, it has to provide the
     *         end nodes of the outgoing edges of {@code u} as a collection.
     * @param targetPredicate a predicate that returns true for the target node(s). It can accept multiple
     *         nodes, in which case a shortest path to one of the nearest target nodes is to be found.
     *         However, for a single target node {@code t}, you can simply use {@code t::equals}.
     * @return a shortest {@link Path} to the nearest target node or an empty optional if no target nodes are
     *         reachable from the source nodes.
     */
    public static <T> Optional<Path<T>> findPathFromAny(Iterable<? extends T> sources,
            Function<? super T, ? extends Iterable<T>> neighborProvider,
            Predicate<? super T> targetPredicate) {
        var results = new HashMap<T, Path<T>>();
        return runBfs(sources, neighborProvider, targetPredicate, results);
    }

    /**
     * Runs the algorithm to find shortest paths (in terms of the number of edges) to all nodes reachable from the
     * given source node.
     *
     * @param source the source node.
     * @param neighborProvider the neighbor provider function. For each node {@code u}, it has to provide the
     *         end nodes of the outgoing edges of {@code u} as a collection.
     * @return a map that associates a {@link Path} with each node reachable from the source node.
     */
    public static <T> Map<T, Path<T>> run(T source,
            Function<? super T, ? extends Iterable<T>> neighborProvider) {
        return runFromAny(List.of(source), neighborProvider);
    }

    /**
     * Runs the algorithm to find shortest paths (in terms of the number of edges) to all nodes reachable from any
     * of the given source nodes.
     *
     * @param sources the source nodes.
     * @param neighborProvider the neighbor provider function. For each node {@code u}, it has to provide the
     *         end nodes of the outgoing edges of {@code u} as a collection.
     * @return a map that associates a {@link Path} with each node reachable from the source nodes.
     */
    public static <T> Map<T, Path<T>> runFromAny(Iterable<? extends T> sources,
            Function<? super T, ? extends Iterable<T>> neighborProvider) {
        var results = new HashMap<T, Path<T>>();
        runBfs(sources, neighborProvider, n -> false, results);
        return results;
    }

    private static <T> Optional<Path<T>> runBfs(Iterable<? extends T> sources,
            Function<? super T, ? extends Iterable<T>> neighborProvider,
            Predicate<? super T> targetPredicate,
            Map<T, Path<T>> results) {

        var queue = new ArrayDeque<Path<T>>();
        for (var source : sources) {
            var path = new Path<T>(source, 0, null);
            results.put(source, path);
            queue.add(path);
        }

        while (!queue.isEmpty()) {
            var path = queue.poll();
            if (targetPredicate.test(path.endNode())) {
                return Optional.of(path);
            }

            for (T neighbor : neighborProvider.apply(path.endNode())) {
                if (!results.containsKey(neighbor)) {
                    var p = new Path<>(neighbor, path.dist() + 1, path);
                    results.put(neighbor, p);
                    queue.add(p);
                }
            }
        }

        return Optional.empty();
    }

}
