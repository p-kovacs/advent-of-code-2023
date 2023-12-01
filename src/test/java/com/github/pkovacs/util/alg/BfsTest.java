package com.github.pkovacs.util.alg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.pkovacs.util.InputUtils;
import com.github.pkovacs.util.data.Cell;
import com.github.pkovacs.util.data.CharTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BfsTest {

    @Test
    void testWithSimpleGraph() {
        ListMultimap<String, String> graph = MultimapBuilder.hashKeys().arrayListValues().build();
        graph.put("A", "B");
        graph.put("A", "C");
        graph.put("A", "D");
        graph.put("B", "E");
        graph.put("C", "E");
        graph.put("D", "G");
        graph.put("E", "D");
        graph.put("E", "F");
        graph.put("E", "G");
        graph.put("F", "B");
        graph.put("F", "G");

        assertEquals(0, Bfs.dist("A", graph::get, "A"::equals));
        assertEquals(1, Bfs.dist("A", graph::get, "B"::equals));
        assertEquals(3, Bfs.dist("A", graph::get, "F"::equals));
        assertEquals(2, Bfs.dist("A", graph::get, "G"::equals));

        var map = Bfs.run("A", graph::get);

        assertEquals(7, map.size());
        assertEquals(0, map.get("A").dist());
        assertEquals(1, map.get("B").dist());
        assertEquals(1, map.get("C").dist());
        assertEquals(2, map.get("G").dist());

        var result1 = Bfs.findPath("A", graph::get, "G"::equals);

        assertTrue(result1.isPresent());
        assertEquals("G", result1.get().endNode());
        assertEquals(2, result1.get().dist());
        assertEquals(List.of("A", "D", "G"), result1.get().nodes());

        graph.put("A", "G");
        var result2 = Bfs.findPath("A", graph::get, "G"::equals);

        assertTrue(result2.isPresent());
        assertEquals("G", result2.get().endNode());
        assertEquals(1, result2.get().dist());
        assertEquals(List.of("A", "G"), result2.get().nodes());

        var result3 = Bfs.findPath("A", graph::get, "A"::equals);

        assertTrue(result3.isPresent());
        assertEquals(0, result3.get().dist());
        assertEquals(List.of("A"), result3.get().nodes());

        var result4 = Bfs.findPath("B", graph::get, "C"::equals);

        assertTrue(result4.isEmpty());
    }

    @Test
    void testWithMaze() {
        // We have to find the shortest path in a maze from the top left tile to the bottom right tile.
        // See maze.txt, '#' represents a wall tile, '.' represents an empty tile.

        var input = InputUtils.readLines(InputUtils.getPath(getClass(), "maze.txt"));
        var maze = new CharTable(input);

        var start = new Cell(0, 0);
        var end = new Cell(maze.rowCount() - 1, maze.colCount() - 1);

        var result = Bfs.findPath(start,
                cell -> maze.neighbors(cell).filter(n -> maze.get(n) == '.').toList(),
                end::equals);

        assertTrue(result.isPresent());
        assertEquals(end, result.get().endNode());
        assertEquals(50, result.get().dist());

        var path = result.get().nodes();
        assertEquals(51, path.size());
        assertEquals(start, path.get(0));
        assertEquals(end, path.get(path.size() - 1));
    }

    @Test
    void testWithJugs() {
        // A simple puzzle also featured in the movie "Die Hard 3". :)
        // We have a 3-liter jug, a 5-liter jug, and a fountain. Let's measure 4 liters of water.
        // BFS algorithm can be used for finding the optimal path in an "implicit graph": the nodes represent
        // valid states, and the edges represent state transformations (steps). The graph is not generated
        // explicitly, but the next states are generated on-the-fly during the traversal.

        record State(int a, int b) {}

        var result = Bfs.findPath(new State(0, 0), state -> {
            var list = new ArrayList<State>();
            list.add(new State(3, state.b())); // 3-liter jug <-- fountain
            list.add(new State(state.a(), 5)); // 5-liter jug <-- fountain
            list.add(new State(0, state.b())); // 3-liter jug --> fountain
            list.add(new State(state.a(), 0)); // 5-liter jug --> fountain
            int d1 = Math.min(3 - state.a(), state.b());
            list.add(new State(state.a() + d1, state.b() - d1)); // 3-liter jug <-- 5-liter jug
            int d2 = Math.min(5 - state.b(), state.a());
            list.add(new State(state.a() - d2, state.b() + d2)); // 3-liter jug --> 5-liter jug
            return list;
        }, pair -> pair.b() == 4);

        assertTrue(result.isPresent());
        assertEquals(6, result.get().dist());
        assertEquals(List.of(
                        new State(0, 0),
                        new State(0, 5),
                        new State(3, 2),
                        new State(0, 2),
                        new State(2, 0),
                        new State(2, 5),
                        new State(3, 4)),
                result.get().nodes());
    }

    @Test
    void testWithInfiniteGraph() {
        var result1 = Bfs.findPath(0, i -> List.of(i + 1, 2 * i), i -> i == 128);
        var result2 = Bfs.findPath(0, i -> List.of(i + 1, 2 * i), i -> i == 127);
        var result3 = Bfs.findPath(0, i -> List.of(i + 1, 2 * i), i -> i == 42);
        var result4 = Bfs.findPath(0, i -> List.of(i + 1, 2 * i), i -> i == 137);

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());
        assertTrue(result4.isPresent());
        assertEquals(List.of(0, 1, 2, 4, 8, 16, 32, 64, 128), result1.get().nodes());
        assertEquals(List.of(0, 1, 2, 3, 6, 7, 14, 15, 30, 31, 62, 63, 126, 127), result2.get().nodes());
        assertEquals(List.of(0, 1, 2, 4, 5, 10, 20, 21, 42), result3.get().nodes());
        assertEquals(List.of(0, 1, 2, 4, 8, 16, 17, 34, 68, 136, 137), result4.get().nodes());
    }

    @Test
    void testMultipleTargets() {
        var nodes = new ArrayList<>(IntStream.range(0, 100).boxed().toList());
        Collections.shuffle(nodes, new Random(123456789));

        var result = Bfs.findPath(nodes.get(0),
                i -> IntStream.rangeClosed(nodes.indexOf(i), nodes.indexOf(i) + 7).mapToObj(nodes::get).toList(),
                i -> nodes.indexOf(i) >= 42);

        assertTrue(result.isPresent());
        assertEquals(6, result.get().dist());
    }

    @Test
    void testMultipleSources() {
        var result = Bfs.findPathFromAny(IntStream.range(82, 100).boxed().toList(),
                i -> List.of(i - 3, i - 7),
                i -> i == 42);

        assertTrue(result.isPresent());
        assertEquals(6, result.get().dist());
        assertEquals(List.of(84, 77, 70, 63, 56, 49, 42), result.get().nodes());
    }

    @Test
    void testGenericParameters() {
        Function<Collection<Integer>, Collection<List<Integer>>> neighborProvider = c ->
                IntStream.rangeClosed(0, 3).mapToObj(i -> concat(c, i).toList()).toList();

        var start = List.of(1, 0);
        var target = List.of(1, 0, 1, 0, 0, 1, 2);
        Predicate<List<Integer>> predicate = target::equals;

        var path = Bfs.findPath(start, neighborProvider, predicate);

        assertTrue(path.isPresent());
        assertEquals(5, path.get().dist());
        assertEquals(target, path.get().endNode());
    }

    private static Stream<Integer> concat(Collection<Integer> collection, int i) {
        return Stream.concat(collection.stream(), Stream.of(i));
    }

}
