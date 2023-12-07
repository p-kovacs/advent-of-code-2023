package com.github.pkovacs.aoc.y2023;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import com.github.pkovacs.aoc.AocUtils;
import com.github.pkovacs.util.Utils;

public class Day07 {

    public static void main(String[] args) {
        var lines = Utils.readLines(AocUtils.getInputPath());

        System.out.println("Part 1: " + solve(lines, false));
        System.out.println("Part 2: " + solve(lines, true));
    }

    private static long solve(List<String> lines, boolean useJoker) {
        var hands = lines.stream().map(s -> new Hand(s, useJoker)).sorted().toList();
        return IntStream.range(0, hands.size()).mapToLong(i -> (i + 1) * hands.get(i).bid).sum();
    }

    private static class Hand implements Comparable<Hand> {

        final boolean useJoker;
        final int[] cards;
        final int[] counts;
        final long bid;

        Hand(String str, boolean useJoker) {
            this.useJoker = useJoker;
            cards = IntStream.range(0, 5).map(i -> getStrength(str.charAt(i))).toArray();
            counts = new int[15];
            for (int i = 0; i < cards.length; i++) {
                counts[cards[i]]++;
            }
            bid = Long.parseLong(str.substring(6));
        }

        int getType() {
            return useJoker
                    ? IntStream.range(2, 15).map(i -> getType(useJokerAs(i))).max().orElseThrow()
                    : getType(counts);
        }

        private int getType(int[] counts) {
            int maxCount = Utils.streamOf(counts).max().orElseThrow();
            int pairCount = (int) Utils.streamOf(counts).filter(i -> i == 2).count();

            if (maxCount == 1) {
                return 0;
            } else if (maxCount == 2) {
                return pairCount;
            } else if (maxCount == 3) {
                return pairCount == 0 ? 3 : 4;
            } else {
                return maxCount + 1;
            }
        }

        private int[] useJokerAs(int i) {
            int[] c = counts.clone();
            c[0] = 0;
            c[i] += counts[0];
            return c;
        }

        private int getStrength(char ch) {
            return switch (ch) {
                case 'A' -> 14;
                case 'K' -> 13;
                case 'Q' -> 12;
                case 'J' -> useJoker ? 0 : 11;
                case 'T' -> 10;
                default -> ch - '0';
            };
        }

        @Override
        public int compareTo(Hand other) {
            int t1 = getType();
            int t2 = other.getType();
            return t1 != t2 ? t1 - t2 : Arrays.compare(cards, other.cards);
        }

    }

}
