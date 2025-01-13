package com.github.pkovacs.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongBiFunction;
import java.util.stream.Stream;

/**
 * Functional interface that represents the edges of a directed or undirected graph. For each node, this interface
 * provides its neighbors (the end nodes of the outgoing edges of that node). In the case of an undirected graph,
 * this relation must be symmetric.
 * <p>
 * This is a simplistic approach that makes it as easy and flexible as possible to define graphs when solving coding
 * puzzles (e.g., with lambda expressions or method references). On the other hand, the collection of all nodes is
 * not available via this interface, so the algorithms require one or more nodes to be specified explicitly.
 * <p>
 * The {@link #of Graph.of} static factory methods can be used to create graphs.
 * {@link #filterNodes(Predicate)} and {@link #filterEdges(BiPredicate)} can be used to obtain subgraphs of a graph.
 *
 * @param <T> the type of nodes in the graph
 * @see WeightedGraph
 */
@FunctionalInterface
public interface Graph<T> {

    /**
     * Provides the neighbors (adjacent nodes) of the given node. That is, the end nodes of its outgoing edges.
     * <p>
     * In the case of an undirected graph, the adjacency relation must be symmetric. That is, if node {code v} is
     * among the neighbors of {@code u}, then {@code u} must be among the neighbors of {@code v}.
     * <p>
     * This is the only one abstract method, it represents the function contract of this interface.
     */
    Stream<T> neighbors(T node);

    /**
     * Wraps the given neighbor provider function as a graph.
     * <p>
     * In fact, the function itself can also be used as a graph, but this factory method allows the usage of
     * additional methods of this interface, e.g., to filter nodes or edges.
     */
    static <T> Graph<T> of(Function<? super T, ? extends Stream<T>> neighborProvider) {
        return neighborProvider::apply;
    }

    /**
     * Wraps the given map as a graph. Changes in the map are reflected in the returned graph.
     */
    static <T> Graph<T> of(Map<? super T, ? extends Collection<T>> map) {
        return u -> map.get(u).stream();
    }

    /**
     * Restricts this graph to only contain the nodes that satisfy the given predicate.
     */
    default Graph<T> filterNodes(Predicate<? super T> nodeFilter) {
        return u -> neighbors(u).filter(nodeFilter);
    }

    /**
     * Restricts this graph to only contain the edges between pairs of nodes that satisfy the given predicate.
     */
    default Graph<T> filterEdges(BiPredicate<? super T, ? super T> edgeFilter) {
        return u -> neighbors(u).filter(v -> edgeFilter.test(u, v));
    }

    /**
     * Converts this graph into a {@link WeightedGraph} using the given weight function.
     */
    default WeightedGraph<T> weighted(ToLongBiFunction<? super T, ? super T> weight) {
        return WeightedGraph.of(this, weight);
    }

}
