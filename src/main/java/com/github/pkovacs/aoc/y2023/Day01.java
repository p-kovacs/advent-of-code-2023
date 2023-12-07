package com.github.pkovacs.aoc.y2023;

import java.util.List;

import com.github.pkovacs.aoc.AbstractDay;

public class Day01 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        System.out.println("Part 1: " + solve(lines, 1));
        System.out.println("Part 2: " + solve(lines, 2));
    }

    private static int solve(List<String> lines, int part) {
        return lines.stream()
                .map(line -> part == 2 ? convertDigits(line) : line)
                .mapToInt(Day01::getCalibrationValue)
                .sum();
    }

    private static int getCalibrationValue(String line) {
        var digits = charsOf(line).filter(Character::isDigit).map(c -> c - '0').toList();
        return digits.get(0) * 10 + digits.get(digits.size() - 1);
    }

    private static String convertDigits(String line) {
        return line
                .replace("one", "o1e")
                .replace("two", "t2o")
                .replace("three", "t3e")
                .replace("four", "f4r")
                .replace("five", "f5e")
                .replace("six", "s6x")
                .replace("seven", "s7n")
                .replace("eight", "e8t")
                .replace("nine", "n9e");
    }

}
