package com.github.pkovacs.aoc.y2023;

import java.util.ArrayList;
import java.util.stream.Stream;

import com.github.pkovacs.util.Bfs;
import com.github.pkovacs.util.CharTable;
import com.github.pkovacs.util.Dir;
import com.github.pkovacs.util.Pos;

public class Day16 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());
        var table = new CharTable(lines);

        long ans1 = count(table, new State(table.topLeft(), Dir.E));

        long ans2 = Stream.of(
                table.firstRow().map(c -> new State(c, Dir.S)),
                table.lastRow().map(c -> new State(c, Dir.N)),
                table.firstCol().map(c -> new State(c, Dir.E)),
                table.lastCol().map(c -> new State(c, Dir.W))
        ).flatMap(s -> s).mapToLong(s -> count(table, s)).max().orElseThrow();

        System.out.println("Part 1: " + ans1);
        System.out.println("Part 2: " + ans2);
    }

    private static long count(CharTable table, State start) {
        var paths = Bfs.findPaths(st -> {
            char ch = table.get(st.pos);
            var list = new ArrayList<State>();
            if (ch == '.' || (ch == '-' && st.dir.isHorizontal()) || (ch == '|' && st.dir.isVertical())) {
                var next = st.step(st.dir); // go forward
                if (table.containsCell(next.pos)) {
                    list.add(next);
                }
            }
            if (((ch == '-' || ch == '\\') && st.dir.isVertical())
                    || ((ch == '|' || ch == '/') && st.dir.isHorizontal())) {
                var next = st.step(st.dir.rotateLeft()); // turn left
                if (table.containsCell(next.pos)) {
                    list.add(next);
                }
            }
            if (((ch == '-' || ch == '/') && st.dir.isVertical())
                    || ((ch == '|' || ch == '\\') && st.dir.isHorizontal())) {
                var next = st.step(st.dir.rotateRight()); // turn right
                if (table.containsCell(next.pos)) {
                    list.add(next);
                }
            }
            return list.stream();
        }, start);
        return paths.keySet().stream().map(s -> s.pos).distinct().count();
    }

    private record State(Pos pos, Dir dir) {
        State step(Dir newDir) {
            return new State(pos.neighbor(newDir), newDir);
        }
    }

}
