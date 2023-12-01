package com.github.pkovacs.util.alg;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.pkovacs.util.alg.Dijkstra.Edge;

/**
 * Implements an efficient version of the Bellman-Ford algorithm, which is known as the
 * <a href="https://en.wikipedia.org/wiki/Shortest_Path_Faster_Algorithm">SPFA algorithm</a>.
 * This algorithm is significantly slower than {@link Dijkstra}, but it also supports negative edge weights.
 * <p>
 * The input is a directed or undirected graph with {@code long} edge weights (implicitly defined by an edge provider
 * function) and one or more source nodes. The edge provider function has to provide for each node {@code u} a
 * collection of {@code (node, weight)} pairs ({@link Dijkstra.Edge} objects) describing the outgoing edges of
 * {@code u}. This function might be applied multiple times to a single node as necessary.
 * <p>
 * This algorithm also supports negative edge weights, but the graph must not contain a directed cycle with negative
 * total weight. The current implementation might not terminate for such input. If there are no negative weights,
 * use {@link Dijkstra} instead, because it is faster.
 * <p>
 * A target predicate can also be specified in order to find path to a single target node instead of all nodes.
 * However, in contrast with {@link Dijkstra}, it does not make the search process faster for this algorithm, and
 * the underlying graph must always be finite.
 *
 * @see Dijkstra
 * @see Bfs
 */
public final class BellmanFord {

    private BellmanFord() {
    }

    /**
     * Finds a shortest path from the given source node to a target node specified by the given predicate.
     *
     * @param source the source node.
     * @param edgeProvider the edge provider function. For each node {@code u}, it has to provide the outgoing
     *         edges of {@code u} as a collection of {@link Edge} objects.
     * @param targetPredicate a predicate that returns true for the target node(s). It can accept multiple
     *         nodes, in which case a shortest path to one of the nearest target nodes is to be found.
     *         However, for a single target node {@code t}, you can simply use {@code t::equals}.
     * @return a shortest {@link Path} to the nearest target node or an empty optional if no target nodes are
     *         reachable from the source node.
     */
    public static <T> Optional<Path<T>> findPath(T source,
            Function<? super T, ? extends Iterable<Edge<T>>> edgeProvider,
            Predicate<? super T> targetPredicate) {
        return findPathFromAny(List.of(source), edgeProvider, targetPredicate);
    }

    /**
     * Finds a shortest path from any of the given source nodes to a target node specified by the given predicate.
     *
     * @param sources the source nodes.
     * @param edgeProvider the edge provider function. For each node {@code u}, it has to provide the outgoing
     *         edges of {@code u} as a collection of {@link Edge} objects.
     * @param targetPredicate a predicate that returns true for the target node(s). It can accept multiple
     *         nodes, in which case a shortest path to one of the nearest target nodes is to be found.
     *         However, for a single target node {@code t}, you can simply use {@code t::equals}.
     * @return a shortest {@link Path} to the nearest target node or an empty optional if no target nodes are
     *         reachable from the source nodes.
     */
    public static <T> Optional<Path<T>> findPathFromAny(Iterable<? extends T> sources,
            Function<? super T, ? extends Iterable<Edge<T>>> edgeProvider,
            Predicate<? super T> targetPredicate) {
        var map = runFromAny(sources, edgeProvider);
        return map.values().stream()
                .filter(p -> targetPredicate.test(p.endNode()))
                .min(Comparator.comparing(Path::dist));
    }

    /**
     * Runs the algorithm to find shortest paths to all nodes reachable from the given source node.
     *
     * @param source the source node.
     * @param edgeProvider the edge provider function. For each node {@code u}, it has to provide the outgoing
     *         edges of {@code u} as a collection of {@link Edge} objects.
     * @return a map that associates a {@link Path} with each node reachable from the source node.
     */
    public static <T> Map<T, Path<T>> run(T source,
            Function<? super T, ? extends Iterable<Edge<T>>> edgeProvider) {
        return runFromAny(List.of(source), edgeProvider);
    }

    /**
     * Runs the algorithm to find shortest paths to all nodes reachable from any of the given source nodes.
     *
     * @param sources the source nodes.
     * @param edgeProvider the edge provider function. For each node {@code u}, it has to provide the outgoing
     *         edges of {@code u} as a collection of {@link Edge} objects.
     * @return a map that associates a {@link Path} with each node reachable from the source nodes.
     */
    public static <T> Map<T, Path<T>> runFromAny(Iterable<? extends T> sources,
            Function<? super T, ? extends Iterable<Edge<T>>> edgeProvider) {

        var results = new HashMap<T, Path<T>>();

        var queue = new ArrayDeque<Path<T>>();
        for (var source : sources) {
            var path = new Path<T>(source, 0, null);
            results.put(source, path);
            queue.add(path);
        }

        while (!queue.isEmpty()) {
            var path = queue.poll();
            for (var edge : edgeProvider.apply(path.endNode())) {
                var neighbor = edge.endNode();
                var dist = path.dist() + edge.weight();
                var current = results.get(neighbor);
                if (current == null || dist < current.dist()) {
                    var p = new Path<>(neighbor, dist, path);
                    results.put(neighbor, p);
                    queue.add(p);
                }
            }
        }

        return results;
    }

}
