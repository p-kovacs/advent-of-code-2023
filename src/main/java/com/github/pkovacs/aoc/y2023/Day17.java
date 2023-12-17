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
            var list = new ArrayList<State>();
            if (st.forward < maxForward) {
                list.add(st.step(st.dir)); // go forward
            }
            if (st.forward >= minForward) {
                list.add(st.step(st.dir.rotateLeft())); // turn left
                list.add(st.step(st.dir.rotateRight())); // turn right
            }
            return list.stream()
                    .filter(s -> table.containsCell(s.cell))
                    .map(s -> Edge.of(s, table.get(s.cell)))
                    .toList();
        }, s -> s.cell.equals(table.bottomRight()) && s.forward >= minForward).orElseThrow();
        return path.dist();
    }

    private record State(Cell cell, Direction dir, int forward) {
        State step(Direction newDir) {
            return new State(cell.neighbor(newDir), newDir, newDir == dir ? forward + 1 : 1);
        }
    }

}
