package com.github.pkovacs.aoc.y2023;

import java.util.HashMap;
import java.util.function.Function;

import com.github.pkovacs.aoc.AbstractDay;

public class Day08 extends AbstractDay {

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        // Parse input
        var cmd = lines.getFirst();
        var left = new HashMap<String, String>();
        var right = new HashMap<String, String>();
        for (var line : lines.subList(2, lines.size())) {
            var from = line.substring(0, 3);
            left.put(from, line.substring(7, 10));
            right.put(from, line.substring(12, 15));
        }

        // Function for calculating the step count from a given source node ..A to a target node ..Z.
        // Although it's not specified in the puzzle description, it seems that from any source node ..A, exactly
        // one target node ..Z can be reached, and the step count for the path ..A->..Z is the same as the step
        // count for each cycle ..Z->..Z afterwards. Furthermore, this step count is a multiple of the number of
        // L/R instructions. This method also checks these assumptions and throws exception if they do not hold.
        Function<String, Integer> calculateStepCount = from -> {
            int n = cmd.length();

            // Calculate step count from the source node to the first target node
            var x = from;
            int i = 0;
            while (!x.endsWith("Z")) {
                x = cmd.charAt(i % n) == 'L' ? left.get(x) : right.get(x);
                i++;
            }
            var target1 = x;
            var count1 = i;

            // Calculate step count from the first target node to the second target node
            while (!x.endsWith("Z") || i == count1) {
                x = cmd.charAt(i % n) == 'L' ? left.get(x) : right.get(x);
                i++;
            }
            var target2 = x;
            var count2 = i - count1;

            // Check assumptions
            if (!target2.equals(target1) || count1 != count2 || count1 % n != 0) {
                throw new IllegalArgumentException("Assumptions do not hold for the input.");
            }

            return count1;
        };

        // Solve part 1
        int ans1 = calculateStepCount.apply("AAA");

        // Solve part 2
        // Given the assumptions discussed above, the answer for part 2 is the least common multiple (LCM)
        // of the step counts of the source nodes.
        //
        // Note that these assumptions are sufficient, but not necessary to ensure the correctness of the
        // calculation. In fact, the example input for part 2 does not satisfy one of them: the step count for
        // 22A is 3, which is not a multiple of 2. Yet, each cycle still has the same size 3 after reaching the
        // target node 22Z the first time, independently of the position in the list of L/R steps. Therefore,
        // the calculation is correct for the example as well, but due to other reasons.
        var startNodes = left.keySet().stream().filter(s -> s.endsWith("A")).toList();
        long ans2 = lcm(startNodes.stream().mapToLong(calculateStepCount::apply));

        System.out.println("Part 1: " + ans1);
        System.out.println("Part 2: " + ans2);
    }

}
