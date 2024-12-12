package com.github.pkovacs.aoc.y2023;

import java.util.Arrays;

public class Day04 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        var win = new int[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            var parts = lines.get(i).split(":")[1].split("[|]");
            win[i] = intersectionOf(setOf(parseInts(parts[0])), setOf(parseInts(parts[1]))).size();
        }

        int ans1 = Arrays.stream(win).filter(i -> i > 0).map(i -> 1 << (i - 1)).sum();

        int[] copies = new int[win.length];
        Arrays.fill(copies, 1);
        for (int i = 0; i < copies.length; i++) {
            for (int j = 1; j <= win[i]; j++) {
                copies[i + j] += copies[i];
            }
        }
        int ans2 = Arrays.stream(copies).sum();

        System.out.println("Part 1: " + ans1);
        System.out.println("Part 2: " + ans2);
    }

}
