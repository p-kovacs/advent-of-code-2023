package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;
import java.util.stream.Stream;

import com.github.pkovacs.util.CharTable;
import com.github.pkovacs.util.Dijkstra;
import com.github.pkovacs.util.Dir;
import com.github.pkovacs.util.IntTable;
import com.github.pkovacs.util.Pos;
import com.github.pkovacs.util.WeightedGraph;
import com.github.pkovacs.util.WeightedGraph.Edge;

public class Day17 extends AbstractDay {

    public static void main(String[] args) {
        var charTable = new CharTable(readLines(getInputPath()));
        var table = new IntTable(charTable.width(), charTable.height(), p -> charTable.get(p) - '0');

        System.out.println("Part 1: " + solve(table, 1, 3));
        System.out.println("Part 2: " + solve(table, 4, 10));
    }

    private static long solve(IntTable table, int minForward, int maxForward) {
        var sources = Stream.of(new State(table.topLeft(), Dir.S, 0), new State(table.topLeft(), Dir.E, 0));
        var path = Dijkstra.findPathFromAny(
                WeightedGraph.of(s -> edges(s, table, minForward, maxForward)),
                sources, s -> s.pos.equals(table.bottomRight()) && s.forward >= minForward).orElseThrow();
        return path.dist();
    }

    private static Stream<Edge<State>> edges(State s, IntTable table, int minForward, int maxForward) {
        var list = new ArrayList<State>();
        if (s.forward < maxForward) {
            list.add(s.step(s.dir));
        }
        if (s.forward >= minForward) {
            list.add(s.step(s.dir.rotateLeft()));
            list.add(s.step(s.dir.rotateRight()));
        }
        return list.stream()
                .filter(next -> table.containsCell(next.pos))
                .map(next -> new Edge<>(next, table.get(next.pos)));
    }

    private record State(Pos pos, Dir dir, int forward) {
        State step(Dir newDir) {
            return new State(pos.neighbor(newDir), newDir, newDir == dir ? forward + 1 : 1);
        }
    }

}
