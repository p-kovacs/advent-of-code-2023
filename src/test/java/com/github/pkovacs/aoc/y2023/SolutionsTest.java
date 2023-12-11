package com.github.pkovacs.aoc.y2023;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class SolutionsTest {

    private final PrintStream origOut = System.out;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private static Stream<Arguments> test() {
        return Stream.of(
                new Arguments("Day01", Day01::main, "56042", "55358"),
                new Arguments("Day02", Day02::main, "2447", "56322"),
                new Arguments("Day03", Day03::main, "533784", "78826761"),
                new Arguments("Day04", Day04::main, "25004", "14427616"),
                new Arguments("Day05", Day05::main, "650599855", "1240035"),
                new Arguments("Day06", Day06::main, "227850", "42948149"),
                new Arguments("Day07", Day07::main, "253313241", "253362743"),
                new Arguments("Day08", Day08::main, "14893", "10241191004509"),
                new Arguments("Day09", Day09::main, "1995001648", "988"),
                new Arguments("Day10", Day10::main, "7005", "417"),
                new Arguments("Day11", Day11::main, "9723824", "731244261352"),
                new Arguments("Day12", Day12::main, "0", "0"),
                new Arguments("Day13", Day13::main, "0", "0"),
                new Arguments("Day14", Day14::main, "0", "0"),
                new Arguments("Day15", Day15::main, "0", "0"),
                new Arguments("Day16", Day16::main, "0", "0"),
                new Arguments("Day17", Day17::main, "0", "0"),
                new Arguments("Day18", Day18::main, "0", "0"),
                new Arguments("Day19", Day19::main, "0", "0"),
                new Arguments("Day20", Day20::main, "0", "0"),
                new Arguments("Day21", Day21::main, "0", "0"),
                new Arguments("Day22", Day22::main, "0", "0"),
                new Arguments("Day23", Day23::main, "0", "0"),
                new Arguments("Day24", Day24::main, "0", "0"),
                new Arguments("Day25", Day25::main, "0", "0")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void test(Arguments args) {
        args.mainMethod().accept(null);
        assertSolution1(args.expected1());
        assertSolution2(args.expected2());
    }

    @BeforeEach
    public void changeSystemOut() {
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void restoreSystemOut() {
        System.setOut(origOut);
    }

    void assertSolution1(String expected) {
        assertSolution(0, expected);
    }

    void assertSolution2(String expected) {
        assertSolution(1, expected);
    }

    private void assertSolution(int index, String expected) {
        var output = outputStream.toString(StandardCharsets.UTF_8);
        var parts = output.split(System.lineSeparator())[index].split(": ");
        var value = parts.length < 2 ? "" : parts[1].trim();
        Assertions.assertEquals(expected, value);
    }

    private static record Arguments(String name, Consumer<String[]> mainMethod, String expected1, String expected2) {
        @Override
        public String toString() {
            return name;
        }
    }

}
