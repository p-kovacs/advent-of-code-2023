package com.github.pkovacs.util.alg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.pkovacs.util.alg.Dijkstra.Edge;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractShortestPathTest {

    abstract <T> Optional<Path<T>> findPath(T source,
            Function<? super T, ? extends Iterable<Edge<T>>> edgeProvider,
            Predicate<? super T> targetPredicate);

    @Test
    void testWithSimpleGraph() {
        ListMultimap<String, Edge<String>> graph = MultimapBuilder.hashKeys().arrayListValues().build();
        graph.put("A", new Edge<>("B", 10));
        graph.put("A", new Edge<>("D", 5));
        graph.put("B", new Edge<>("C", 1));
        graph.put("C", new Edge<>("E", 1));
        graph.put("D", new Edge<>("B", 3));
        graph.put("D", new Edge<>("C", 9));
        graph.put("D", new Edge<>("E", 11));

        var result = findPath("A", graph::get, "E"::equals);
        assertTrue(result.isPresent());
        assertEquals(10, result.get().dist());
        assertEquals(List.of("A", "D", "B", "C", "E"), result.get().nodes());
    }

    @Test
    void testMultipleTargets() {
        var nodes = new ArrayList<>(IntStream.range(0, 100).boxed().toList());
        Collections.shuffle(nodes, new Random(123456789));

        var result = findPath(nodes.get(0),
                i -> IntStream.rangeClosed(nodes.indexOf(i), nodes.indexOf(i) + 7)
                        .filter(j -> j < 100)
                        .mapToObj(j -> new Edge<>(nodes.get(j), 1))
                        .toList(),
                i -> nodes.indexOf(i) >= 42);

        assertTrue(result.isPresent());
        assertEquals(6, result.get().dist());
    }

    @Test
    void testGenericParameters() {
        Function<Collection<Integer>, Collection<Edge<List<Integer>>>> neighborProvider = c ->
                IntStream.rangeClosed(0, 3)
                        .mapToObj(i -> new Edge<>(concat(c, i).toList(), Math.max(i * 10, 1)))
                        .filter(e -> e.endNode().size() <= 10)
                        .toList();

        var start = List.of(1, 0);
        var target = List.of(1, 0, 1, 0, 0, 1, 2);
        Predicate<Collection<Integer>> predicate = target::equals;

        var path = findPath(start, neighborProvider, predicate);

        assertTrue(path.isPresent());
        assertEquals(42, path.get().dist());
        assertEquals(target, path.get().endNode());
    }

    private static Stream<Integer> concat(Collection<Integer> collection, int i) {
        return Stream.concat(collection.stream(), Stream.of(i));
    }

}
