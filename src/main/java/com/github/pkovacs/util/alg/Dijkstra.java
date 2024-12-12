package com.github.pkovacs.util.alg;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements <a href="https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Dijkstra's algorithm</a> for
 * finding shortest paths.
 * <p>
 * The input is a directed or undirected graph with {@code long} edge weights (implicitly defined by an edge provider
 * function) and one or more source nodes. The edge provider function has to provide for each node {@code u} a
 * collection of {@code (node, weight)} pairs ({@link Edge} objects) describing the outgoing edges of {@code u}.
 * This function is applied at most once for each node, when the algorithm advances from that node.
 * <p>
 * This algorithm only supports non-negative edge weights. If you also need negative edge weights, use
 * {@link BellmanFord} instead.
 * <p>
 * A target predicate can also be used in order to find path to a single target node instead of all nodes. The
 * algorithm terminates when a shortest path is found for a target node having minimum distance from the (nearest)
 * source node. This way, paths can also be searched in huge or (theoretically) infinite graphs provided that the
 * edges are generated on-the-fly when requested by the algorithm. For example, nodes and edges might represent
 * feasible states and steps of a combinatorial problem, and we might not be able to or do not want to enumerate
 * all possible (and reachable) states in advance.
 *
 * @see Bfs
 * @see BellmanFord
 */
public final class Dijkstra {

    /**
     * Represents an outgoing directed edge of a node being evaluated (expanded) by this algorithm.
     */
    public record Edge<T>(T endNode, long weight) {
        public static <T> Edge<T> of(T endNode, long weight) {
            return new Edge<>(endNode, weight);
        }
    }

    private Dijkstra() {
    }

    /**
     * Calculates the distance along a shortest path from the given source node to the nearest target node specified
     * by the given predicate. For more details, see {@link #findPath(Object, Function, Predicate)}.
     *
     * @throws java.util.NoSuchElementException if no target nodes are reachable from the source node.
     */
    public static <T> long dist(T source,
            Function<? super T, ? extends Iterable<Edge<T>>> edgeProvider,
            Predicate<? super T> targetPredicate) {
        return findPath(source, edgeProvider, targetPredicate).orElseThrow().dist();
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
        var results = new HashMap<T, Path<T>>();
        return runDijkstra(sources, edgeProvider, targetPredicate, results);
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
        runDijkstra(sources, edgeProvider, n -> false, results);
        return results;
    }

    private static <T> Optional<Path<T>> runDijkstra(Iterable<? extends T> sources,
            Function<? super T, ? extends Iterable<Edge<T>>> edgeProvider,
            Predicate<? super T> targetPredicate,
            HashMap<T, Path<T>> results) {

        var queue = new PriorityQueue<Path<T>>(Comparator.comparing(Path::dist));
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

        return Optional.empty();
    }

}
