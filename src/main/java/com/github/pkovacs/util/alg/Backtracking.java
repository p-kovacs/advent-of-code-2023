package com.github.pkovacs.util.alg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implements a general backtracking algorithm. We assume that the search tree consists of a fixed number of levels,
 * and each level can be represented by an integer range. Typical examples include the eight queens puzzle and
 * finding permutations of a collection.
 */
public final class Backtracking {

    private Backtracking() {
    }

    /**
     * Runs backtracking to find all feasible solutions to the specified problem.
     * A feasible solution is an array of {@code n} integers, each of which is between {@code 0} (inclusive) and
     * {@code m} (exclusive), and each element of the array is accepted by the predicate.
     *
     * @return the list of all feasible solutions
     */
    public static List<int[]> findAll(int n, int m, BacktrackingPredicate predicate) {
        return run(n, m, predicate, Integer.MAX_VALUE);
    }

    /**
     * Runs backtracking to find the first feasible solution to the specified problem.
     * A feasible solution is an array of {@code n} integers, each of which is between {@code 0} (inclusive) and
     * {@code m} (exclusive), and each element of the array is accepted by the predicate.
     *
     * @return a feasible solution or an empty optional if no solution is found
     */
    public static Optional<int[]> findFirst(int n, int m, BacktrackingPredicate predicate) {
        return run(n, m, predicate, 1).stream().findFirst();
    }

    /**
     * Returns true if the specified element of the given {@code int} array is different from all previous elements.
     * This method can be used as a {@link BacktrackingPredicate predicate} to find solutions with distinct elements
     * (e.g. permutations).
     */
    public static boolean distinct(int[] array, int index) {
        int value = array[index];
        for (int i = 0; i < index; i++) {
            if (array[i] == value) {
                return false;
            }
        }
        return true;
    }

    private static List<int[]> run(int n, int m, BacktrackingPredicate predicate, int limit) {
        var result = new ArrayList<int[]>();

        int[] solution = new int[n];
        Arrays.fill(solution, -1);
        for (int k = 0; k >= 0; ) {
            // Find next valid value for the k-th position
            do {
                solution[k]++;
            } while (solution[k] < m && !predicate.accept(solution, k));

            if (solution[k] < m) {
                // A valid value is found
                if (k < n - 1) {
                    // Step forward to the next level
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
            }
        }

        return result;
    }

    /**
     * Checks if a partial solution can be accepted during the {@link Backtracking} algorithm.
     * Such a partial solution is represented by an {@code int} array and the index {@code k} of the current element
     * to be checked. That is, if {@code k > 0}, then we assume that previous calls of the predicate accepted prefixes
     * of the array up to index {@code k - 1}, and only the element with index {@code k} is to be checked now with
     * respect to the previous elements. The algorithm ensures that the elements are within the feasible range up to
     * index {@code k}, only the additional problem-specific conditions have to be checked by the predicate.
     * (Elements with indices larger than {@code k} are all set to {@code -1} in the partial solution.)
     * <p>
     * For example, in the case of eight queens puzzle, the predicate has to check that the element with index
     * {@code k} is different from all previous elements of the array, and it does not share the same diagonal
     * with any of them.
     */
    public interface BacktrackingPredicate {
        boolean accept(int[] candidate, int k);
    }

}
