package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;
import java.util.List;

import com.github.pkovacs.aoc.AbstractDay;
import com.github.pkovacs.util.alg.Dijkstra;
import com.github.pkovacs.util.alg.Dijkstra.Edge;
import com.github.pkovacs.util.data.Cell;
import com.github.pkovacs.util.data.Direction;
import com.github.pkovacs.util.data.IntTable;

public class Day17 extends AbstractDay {

    public static void main(String[] args) {
        var matrix = readCharMatrix(getInputPath());
        var table = new IntTable(matrix.length, matrix[0].length, (i, j) -> matrix[i][j] - '0');

        System.out.println("Part 1: " + solve(table, 1, 3));
        System.out.println("Part 2: " + solve(table, 4, 10));
    }

    private static long solve(IntTable table, int minForward, int maxForward) {
        var sources = List.of(new State(table.topLeft(), Direction.SOUTH, 0),
                new State(table.topLeft(), Direction.EAST, 0));
        var path = Dijkstra.findPathFromAny(sources, st -> {
            var list = new ArrayList<Edge<State>>();
            if (st.forward < maxForward) {
                var forward = st.step(st.dir);
                if (table.containsCell(forward.cell)) {
                    list.add(Edge.of(forward, table.get(forward.cell)));
                }
            }
            if (st.forward >= minForward) {
                var left = st.step(st.dir.rotateLeft());
                if (table.containsCell(left.cell)) {
                    list.add(Edge.of(left, table.get(left.cell)));
                }
                var right = st.step(st.dir.rotateRight());
                if (table.containsCell(right.cell)) {
                    list.add(Edge.of(right, table.get(right.cell)));
                }
            }
            return list;
        }, s -> s.cell.equals(table.bottomRight()) && s.forward >= minForward).orElseThrow();
        return path.dist();
    }

    private record State(Cell cell, Direction dir, int forward) {
        State step(Direction newDir) {
            return new State(cell.neighbor(newDir), newDir, newDir == dir ? forward + 1 : 1);
        }
    }

}
