package com.github.pkovacs.aoc.y2023;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.pkovacs.aoc.AbstractDay;
import com.github.pkovacs.util.RegexUtils;

public class Day03 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        var operands = IntStream.range(0, lines.size()).boxed()
                .flatMap(i -> parseOperands(lines, i))
                .filter(Objects::nonNull)
                .toList();
        long ans1 = operands.stream().mapToLong(Operand::value).sum();

        var operatorMap = operands.stream().collect(Collectors.groupingBy(Operand::operator));
        long ans2 = operatorMap.entrySet().stream()
                .filter(e -> e.getKey().ch == '*' && e.getValue().size() == 2)
                .mapToLong(e -> e.getValue().get(0).value * e.getValue().get(1).value)
                .sum();

        System.out.println("Part 1: " + ans1);
        System.out.println("Part 2: " + ans2);
    }

    private static Stream<Operand> parseOperands(List<String> lines, int row) {
        return RegexUtils.findAllMatches("[0-9]+", lines.get(row)).stream()
                .map(m -> parseOperand(lines, row, m.start(), m.end()));
    }

    private static Operand parseOperand(List<String> lines, int row, int c1, int c2) {
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = c1 - 1; j <= c2; j++) {
                if (i >= 0 && i < lines.size() && j >= 0 && j < lines.get(i).length()) {
                    char ch = lines.get(i).charAt(j);
                    if (ch != '.' && !Character.isDigit(ch)) {
                        // Note: here we assume that at most one symbol is adjacent to any part number
                        return new Operand(Long.parseLong(lines.get(row).substring(c1, c2)), new Operator(i, j, ch));
                    }
                }
            }
        }
        return null;
    }

    private record Operator(int row, int col, char ch) {}

    private record Operand(long value, Operator operator) {}

}
