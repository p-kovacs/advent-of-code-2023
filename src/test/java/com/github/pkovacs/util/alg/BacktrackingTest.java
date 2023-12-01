package com.github.pkovacs.util.alg;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BacktrackingTest {

    @Test
    void testPermutations() {
        var permutations = Backtracking.findAll(6, 6, Backtracking::distinct);
        assertEquals(720, permutations.size());
        assertTrue(permutations.stream().allMatch(x -> Arrays.stream(x).allMatch(v -> v >= 0 && v < 6)));
    }

    @Test
    void testSubsets() {
        var subsets = Backtracking.findAll(10, 2, (x, k) -> true);
        assertEquals(1024, subsets.size());
        assertTrue(subsets.stream().allMatch(x -> Arrays.stream(x).allMatch(v -> v == 0 || v == 1)));
    }

    @Test
    void testEightQueens() {
        assertTrue(Backtracking.findFirst(1, 1, BacktrackingTest::safeQueen).isPresent());
        assertTrue(Backtracking.findFirst(2, 2, BacktrackingTest::safeQueen).isEmpty());
        assertTrue(Backtracking.findFirst(3, 3, BacktrackingTest::safeQueen).isEmpty());
        assertTrue(Backtracking.findFirst(4, 4, BacktrackingTest::safeQueen).isPresent());

        assertArrayEquals(new int[] { 0, 4, 7, 5, 2, 6, 1, 3 },
                Backtracking.findFirst(8, 8, BacktrackingTest::safeQueen).orElseThrow());
        assertEquals(92, Backtracking.findAll(8, 8, BacktrackingTest::safeQueen).size());
    }

    private static boolean safeQueen(int[] array, int k) {
        for (int i = 0, value = array[k]; i < k; i++) {
            if (array[i] == value || Math.abs(array[i] - value) == k - i) {
                return false;
            }
        }
        return true;
    }

}
