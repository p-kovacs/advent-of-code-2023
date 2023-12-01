package com.github.pkovacs.util.alg;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.github.pkovacs.util.alg.Dijkstra.Edge;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BellmanFordTest extends AbstractShortestPathTest {

    @Override
    <T> Optional<Path<T>> findPath(T source,
            Function<? super T, ? extends Iterable<Edge<T>>> edgeProvider,
            Predicate<? super T> targetPredicate) {
        return BellmanFord.findPath(source, edgeProvider, targetPredicate);
    }

    @Test
    void testWithSimpleGraph() {
        ListMultimap<String, Edge<String>> graph = MultimapBuilder.hashKeys().arrayListValues().build();
        graph.put("A", new Edge<>("B", 1));
        graph.put("A", new Edge<>("C", 1));
        graph.put("A", new Edge<>("D", 1));
        graph.put("B", new Edge<>("E", 2));
        graph.put("C", new Edge<>("E", -3));
        graph.put("D", new Edge<>("G", 4));
        graph.put("E", new Edge<>("D", 5));
        graph.put("E", new Edge<>("F", 5));
        graph.put("E", new Edge<>("G", 5));
        graph.put("F", new Edge<>("B", -6));
        graph.put("F", new Edge<>("G", -6));

        var result = BellmanFord.run("A", graph::get);

        assertEquals(0, result.get("A").dist());
        assertEquals(-3, result.get("B").dist());
        assertEquals(1, result.get("C").dist());
        assertEquals(-2, result.get("E").dist());
        assertEquals(3, result.get("F").dist());
        assertEquals(-3, result.get("G").dist());
    }

    @Test
    void testMultipleSources() {
        var result = BellmanFord.findPathFromAny(IntStream.range(82, 100).boxed().toList(),
                i -> i >= 0
                        ? List.of(new Edge<>(i - 3, 1), new Edge<>(i - 7, 2))
                        : List.of(),
                i -> i == 42);

        assertTrue(result.isPresent());
        assertEquals(12, result.get().dist());
        assertEquals(List.of(84, 77, 70, 63, 56, 49, 42), result.get().nodes());
    }

}
