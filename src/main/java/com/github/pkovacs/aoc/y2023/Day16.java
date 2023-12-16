package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;
import java.util.stream.Stream;

import com.github.pkovacs.aoc.AbstractDay;
import com.github.pkovacs.util.alg.Bfs;
import com.github.pkovacs.util.data.Cell;
import com.github.pkovacs.util.data.CharTable;
import com.github.pkovacs.util.data.Direction;

public class Day16 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());
        var table = new CharTable(lines);

        long ans1 = count(table, new State(table.topLeft(), Direction.EAST));

        long ans2 = Stream.of(
                table.row(0).map(c -> new State(c, Direction.SOUTH)),
                table.row(table.rowCount() - 1).map(c -> new State(c, Direction.NORTH)),
                table.col(0).map(c -> new State(c, Direction.EAST)),
                table.col(table.colCount() - 1).map(c -> new State(c, Direction.WEST))
        ).flatMap(s -> s).mapToLong(s -> count(table, s)).max().orElseThrow();

        System.out.println("Part 1: " + ans1);
        System.out.println("Part 2: " + ans2);
    }

    private static long count(CharTable table, State start) {
        var res = Bfs.run(start, st -> {
            var list = new ArrayList<State>();
            char ch = table.get(st.cell);
            if (ch == '.' || (ch == '-' && st.dir.isHorizontal()) || (ch == '|' && st.dir.isVertical())) {
                list.add(st.neighbor(st.dir)); // go forward
            }
            if (((ch == '-' || ch == '/') && st.dir.isVertical())
                    || ((ch == '|' || ch == '\\') && st.dir.isHorizontal())) {
                list.add(st.neighbor(st.dir.rotateRight())); // turn right
            }
            if (((ch == '-' || ch == '\\') && st.dir.isVertical())
                    || ((ch == '|' || ch == '/') && st.dir.isHorizontal())) {
                list.add(st.neighbor(st.dir.rotateLeft())); // turn left
            }
            return list.stream().filter(s -> table.containsCell(s.cell)).toList();
        });
        return res.keySet().stream().map(s -> s.cell).distinct().count();
    }

    private record State(Cell cell, Direction dir) {
        State neighbor(Direction dir) {
            return new State(cell.neighbor(dir), dir);
        }
    }

}
