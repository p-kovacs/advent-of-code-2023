package com.github.pkovacs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

/**
 * A simple but efficient array-based backtracking algorithm. We assume that the search tree consists of a fixed
 * number of levels, and each level can be represented by an integer range of the same size. Typical examples
 * include the eight queens puzzle and finding permutations of a collection.
 */
public final class Backtracking {

    private Backtracking() {
    }

    /**
     * Finds all permutations of integers between 0 (inclusive) and {@code n} (exclusive).
     *
     * @return the list of all permutations of the closed range {@code [0..n-1]}
     */
    public static List<int[]> findAllPermutations(int n) {
        return findAllDistinct(n, n, (a, k) -> true);
    }

    /**
     * Finds all arrays of distinct values that are feasible solutions of the specified problem.
     * A feasible solution is an array of {@code n} distinct integers, each of which is between {@code 0} (inclusive)
     * and {@code m} (exclusive), and each element of the array is accepted by the given predicate with respect to
     * the previous elements.
     * <p>
     * {@code m >= n} must hold. If {@code m = n}, then the feasible solutions are permutations of the closed range
     * {@code [0..n-1]} that are accepted by the given predicate.
     * <p>
     * This method is faster than using {@link #findAll(int, int, BacktrackingPredicate)} and checking uniqueness of
     * the values by the predicate.
     *
     * @return the list of all arrays of distinct values that are feasible solutions
     */
    public static List<int[]> findAllDistinct(int n, int m, BacktrackingPredicate predicate) {
        if (m < n) {
            throw new IllegalArgumentException(
                    String.format("The value range is smaller than the domain: %d < %d.", m, n));
        }

        return run(n, m, true, predicate, Integer.MAX_VALUE);
    }

    /**
     * Finds all feasible solutions of the specified problem.
     * A feasible solution is an array of {@code n} integers, each of which is between {@code 0} (inclusive)
     * and {@code m} (exclusive), and each element of the array is accepted by the given predicate with respect to
     * the previous elements.
     *
     * @return the list of all feasible solutions
     */
    public static List<int[]> findAll(int n, int m, BacktrackingPredicate predicate) {
        return run(n, m, false, predicate, Integer.MAX_VALUE);
    }

    /**
     * Finds the first array of distinct values that is a feasible solution of the specified problem.
     * A feasible solution is an array of {@code n} distinct integers, each of which is between {@code 0} (inclusive)
     * and {@code m} (exclusive), and each element of the array is accepted by the given predicate with respect to
     * the previous elements.
     * <p>
     * {@code m >= n} must hold. If {@code m = n}, then the feasible solutions are permutations of the closed range
     * {@code [0..n-1]} that are accepted by the given predicate.
     * <p>
     * This method is faster than using {@link #findFirst(int, int, BacktrackingPredicate)} and checking uniqueness of
     * the values by the predicate.
     *
     * @return an array of distinct values that is a feasible solution or an empty optional if no such solution
     *         is found
     */
    public static Optional<int[]> findFirstDistinct(int n, int m, BacktrackingPredicate predicate) {
        if (m < n) {
            throw new IllegalArgumentException(
                    String.format("The value range is smaller than the domain: %d < %d.", m, n));
        }

        return run(n, m, true, predicate, 1).stream().findFirst();
    }

    /**
     * Finds the first feasible solution of the specified problem.
     * A feasible solution is an array of {@code n} integers, each of which is between {@code 0} (inclusive)
     * and {@code m} (exclusive), and each element of the array is accepted by the given predicate with respect to
     * the previous elements.
     *
     * @return a feasible solution or an empty optional if no solution is found
     */
    public static Optional<int[]> findFirst(int n, int m, BacktrackingPredicate predicate) {
        return run(n, m, false, predicate, 1).stream().findFirst();
    }

    private static List<int[]> run(int n, int m, boolean distinct, BacktrackingPredicate predicate, int limit) {
        var result = new ArrayList<int[]>();

        var available = new BitSet();
        available.set(0, m + 1);

        int[] solution = new int[n];
        Arrays.fill(solution, -1);
        for (int k = 0; k >= 0; ) {
            // Find next valid value for the k-th position
            do {
                solution[k] = distinct ? available.nextSetBit(solution[k] + 1) : solution[k] + 1;
            } while (solution[k] < m && !predicate.accept(solution, k));

            if (solution[k] < m) {
                // A valid value is found
                if (k < n - 1) {
                    // Step forward to the next level
                    if (distinct) {
                        available.set(solution[k], false);
                    }
                    k++;
                } else {
                    // A solution is found
                    result.add(solution.clone());
                    if (result.size() == limit) {
                        break;
                    }
                }
            } else {
                // Step back to the previous level
                solution[k] = -1;
                k--;
                if (distinct && k >= 0) {
                    available.set(solution[k], true);
                }
            }
        }

        return result;
    }

    /**
     * Checks if a partial solution can be accepted during the {@link Backtracking} algorithm.
     * <p>
     * Such a partial solution is represented by an {@code int} array and the index {@code k} of the current element
     * to be checked. That is,we assume that previous calls of the predicate accepted prefixes of the array up to
     * index {@code k - 1}, and only the element with index {@code k} is to be checked now with respect to the
     * previous elements. The algorithm ensures that the elements are within the feasible range up to index {@code k},
     * only the additional problem-specific conditions have to be checked by the predicate.
     * (Elements with indices larger than {@code k} are all set to {@code -1} in the partial solution.)
     * <p>
     * For example, in the case of eight queens puzzle, the predicate has to check that the element with index
     * {@code k} is different from all previous elements of the array, and it does not share the same diagonal
     * with any of them.
     */
    @FunctionalInterface
    public interface BacktrackingPredicate {
        boolean accept(int[] candidate, int k);
    }

}
