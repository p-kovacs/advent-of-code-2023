package com.github.pkovacs.aoc.y2023;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import com.github.pkovacs.aoc.AbstractDay;

public class Day02 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        var games = lines.stream().map(Day02::parseGame).toList();

        int solution1 = IntStream.range(0, games.size())
                .filter(i -> games.get(i).stream().allMatch(c -> c.r <= 12 && c.g <= 13 && c.b <= 14))
                .map(i -> i + 1)
                .sum();
        int solution2 = games.stream()
                .map(game -> getMinConfig(game))
                .mapToInt(c -> c.r * c.g * c.b)
                .sum();

        System.out.println("Part 1: " + solution1);
        System.out.println("Part 2: " + solution2);
    }

    private static Config getMinConfig(List<Config> game) {
        return new Config(
                game.stream().mapToInt(Config::r).max().orElseThrow(),
                game.stream().mapToInt(Config::g).max().orElseThrow(),
                game.stream().mapToInt(Config::b).max().orElseThrow()
        );
    }

    private static List<Config> parseGame(String line) {
        var configs = line.split(": ")[1].split("; ");
        return Arrays.stream(configs).map(Day02::parseConfig).toList();
    }

    private static Config parseConfig(String s) {
        int[] c = new int[3];
        for (var part : s.split(", ")) {
            int i = part.endsWith("red") ? 0 : (part.endsWith("green") ? 1 : 2);
            c[i] = Integer.parseInt(part.split(" ")[0]);
        }
        return new Config(c[0], c[1], c[2]);
    }

    private record Config(int r, int g, int b) {}

}
