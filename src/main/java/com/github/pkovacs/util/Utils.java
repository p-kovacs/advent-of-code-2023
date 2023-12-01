package com.github.pkovacs.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Provides various useful utility methods, also including the ones defined in {@link InputUtils}.
 */
public class Utils extends InputUtils {

    protected Utils() {
    }

    /**
     * Constrains the given {@code index} to the closed range {@code [0..(size - 1)]}.
     */
    public static int constrainIndex(int index, int size) {
        return constrainToRange(index, 0, size - 1);
    }

    /**
     * Wraps the given {@code index} to the closed range {@code [0..(size - 1)]}.
     */
    public static int wrapIndex(int index, int size) {
        checkRange(0, size - 1);
        return Math.floorMod(index, size);
    }

    /**
     * Constrains the given int {@code value} to the closed range {@code [min..max]}.
     */
    public static int constrainToRange(int value, int min, int max) {
        checkRange(min, max);
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Constrains the given long {@code value} to the closed range {@code [min..max]}.
     */
    public static long constrainToRange(long value, long min, long max) {
        checkRange(min, max);
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Wraps the given int {@code value} to the closed range {@code [min..max]}.
     */
    public static int wrapToRange(int value, int min, int max) {
        checkRange(min, max);
        return min + Math.floorMod(value - min, max - min + 1);
    }

    /**
     * Wraps the given long {@code value} to the closed range {@code [min..max]}.
     */
    public static long wrapToRange(long value, long min, long max) {
        checkRange(min, max);
        return min + Math.floorMod(value - min, max - min + 1);
    }

    /**
     * Returns true if the given {@code value} is within the closed range {@code [min..max]}.
     */
    public static <T extends Comparable<T>> boolean isInRange(T value, T min, T max) {
        checkRange(min, max);
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    private static <T extends Comparable<T>> void checkRange(T min, T max) {
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Minimum value " + min + " is greater than maximum value " + max + ".");
        }
    }

    /**
     * Returns the minimum of the given {@code int} values.
     *
     * @throws java.util.NoSuchElementException if no numbers are given
     */
    public static int min(int... ints) {
        return streamOf(ints).min().orElseThrow();
    }

    /**
     * Returns the minimum of the given {@code long} values.
     *
     * @throws java.util.NoSuchElementException if no numbers are given
     */
    public static long min(long... longs) {
        return streamOf(longs).min().orElseThrow();
    }

    /**
     * Returns the minimum of the given {@code char} values.
     *
     * @throws java.util.NoSuchElementException if no characters are given
     */
    public static char min(char... chars) {
        return streamOf(chars).min(Comparator.naturalOrder()).orElseThrow();
    }

    /**
     * Returns the minimum of the {@code int} values of the given numbers.
     *
     * @throws java.util.NoSuchElementException if no numbers are given
     */
    public static int minInt(Collection<? extends Number> numbers) {
        return numbers.stream().mapToInt(Number::intValue).min().orElseThrow();
    }

    /**
     * Returns the minimum of the {@code long} values of the given numbers.
     *
     * @throws java.util.NoSuchElementException if no numbers are given
     */
    public static long minLong(Collection<? extends Number> numbers) {
        return numbers.stream().mapToLong(Number::longValue).min().orElseThrow();
    }

    /**
     * Returns the maximum of the given {@code int} values.
     *
     * @throws java.util.NoSuchElementException if no numbers are given
     */
    public static int max(int... ints) {
        return streamOf(ints).max().orElseThrow();
    }

    /**
     * Returns the maximum of the given {@code long} values.
     *
     * @throws java.util.NoSuchElementException if no numbers are given
     */
    public static long max(long... longs) {
        return streamOf(longs).max().orElseThrow();
    }

    /**
     * Returns the maximum of the given {@code char} values.
     *
     * @throws java.util.NoSuchElementException if no characters are given
     */
    public static char max(char... chars) {
        return streamOf(chars).max(Comparator.naturalOrder()).orElseThrow();
    }

    /**
     * Returns the maximum of the {@code int} values of the given numbers.
     *
     * @throws java.util.NoSuchElementException if no numbers are given
     */
    public static int maxInt(Collection<? extends Number> numbers) {
        return numbers.stream().mapToInt(Number::intValue).max().orElseThrow();
    }

    /**
     * Returns the maximum of the {@code long} values of the given numbers.
     *
     * @throws java.util.NoSuchElementException if no numbers are given
     */
    public static long maxLong(Collection<? extends Number> numbers) {
        return numbers.stream().mapToLong(Number::longValue).max().orElseThrow();
    }

    /**
     * Returns a deep copy of the given int matrix.
     * The "rows" might have different sizes, but null arrays are not supported.
     */
    public static int[][] deepCopyOf(int[][] matrix) {
        return Arrays.stream(matrix).map(a -> a.clone()).toArray(int[][]::new);
    }

    /**
     * Returns a deep copy of the given long matrix.
     * The "rows" might have different sizes, but null arrays are not supported.
     */
    public static long[][] deepCopyOf(long[][] matrix) {
        return Arrays.stream(matrix).map(a -> a.clone()).toArray(long[][]::new);
    }

    /**
     * Returns a deep copy of the given char matrix.
     * The "rows" might have different sizes, but null arrays are not supported.
     */
    public static char[][] deepCopyOf(char[][] matrix) {
        return Arrays.stream(matrix).map(a -> a.clone()).toArray(char[][]::new);
    }

    /**
     * Returns the elements of the given {@code int} array as an unmodifiable list.
     */
    public static List<Integer> listOf(int[] ints) {
        return streamOf(ints).boxed().toList();
    }

    /**
     * Returns the elements of the given {@code int} array as an unmodifiable set.
     */
    public static Set<Integer> setOf(int[] ints) {
        return streamOf(ints).boxed().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the elements of the given {@code int} array as an {@link IntStream}.
     */
    public static IntStream streamOf(int[] ints) {
        return IntStream.of(ints);
    }

    /**
     * Returns the elements of the given {@code long} array as an unmodifiable list.
     */
    public static List<Long> listOf(long[] longs) {
        return streamOf(longs).boxed().toList();
    }

    /**
     * Returns the elements of the given {@code long} array as an unmodifiable set.
     */
    public static Set<Long> setOf(long[] longs) {
        return streamOf(longs).boxed().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the elements of the given {@code long} array as a {@link LongStream}.
     */
    public static LongStream streamOf(long[] longs) {
        return LongStream.of(longs);
    }

    /**
     * Returns the elements of the given {@code char} array as an unmodifiable list.
     */
    public static List<Character> listOf(char[] chars) {
        return streamOf(chars).toList();
    }

    /**
     * Returns the elements of the given {@code char} array as an unmodifiable set.
     */
    public static Set<Character> setOf(char[] chars) {
        return streamOf(chars).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the elements of the given {@code char} array as a stream.
     */
    public static Stream<Character> streamOf(char[] chars) {
        return IntStream.range(0, chars.length).mapToObj(i -> chars[i]);
    }

    /**
     * Returns the characters of the given {@code CharSequence} as a stream.
     */
    public static Stream<Character> charsOf(CharSequence s) {
        return s.toString().chars().mapToObj(i -> (char) i);
    }

    /**
     * Returns the union of the given collections.
     */
    public static <E> Set<E> unionOf(Collection<? extends E> a, Collection<? extends E> b) {
        return unionOf(List.of(a, b));
    }

    /**
     * Returns the union of the given streams.
     */
    public static <E> Set<E> unionOf(Stream<? extends E> a, Stream<? extends E> b) {
        return unionOf(List.of(a.toList(), b.toList()));
    }

    /**
     * Returns the union of the given collections.
     */
    public static <E> Set<E> unionOf(Collection<? extends Collection<? extends E>> collections) {
        var result = new HashSet<E>(collections.iterator().next());
        collections.stream().skip(1).forEach(result::addAll);
        return result;
    }

    /**
     * Returns the intersection of the given collections.
     */
    public static <E> Set<E> intersectionOf(Collection<? extends E> a, Collection<? extends E> b) {
        return intersectionOf(List.of(a, b));
    }

    /**
     * Returns the intersection of the given streams.
     */
    public static <E> Set<E> intersectionOf(Stream<? extends E> a, Stream<? extends E> b) {
        return intersectionOf(List.of(a.toList(), b.toList()));
    }

    /**
     * Returns the intersection of the given collections.
     */
    public static <E> Set<E> intersectionOf(Collection<? extends Collection<? extends E>> collections) {
        var result = new HashSet<E>(collections.iterator().next());
        collections.stream()
                .skip(1)
                .map(c -> c instanceof Set ? c : new HashSet<>(c))
                .forEach(result::retainAll);
        return result;
    }

    /**
     * Returns an ordered stream of the consecutive {@linkplain List#subList(int, int) sublists} (chunks) of the
     * given size constructed from the given list (the last sublist might be smaller).
     * <p>
     * Example: {@code chunked(List.of(1, 2, 3, 4, 5), 3)} is {@code [[1, 2, 3], [4, 5]]}.
     *
     * @throws IllegalArgumentException if the chunk size is smaller than 1
     */
    public static <E> Stream<List<E>> chunked(List<E> list, int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Chunk size must be at least 1.");
        }

        return IntStream.range(0, (list.size() + size - 1) / size)
                .mapToObj(i -> list.subList(i * size, Math.min((i + 1) * size, list.size())));
    }

    /**
     * Returns an ordered stream of all {@linkplain List#subList(int, int) sublists} of the given size constructed
     * from the given list. As if the list was looking at through a sliding window of the given size.
     * <p>
     * Example: {@code windowed(List.of(1, 2, 3, 4, 5), 3)} is {@code [[1, 2, 3], [2, 3, 4], [3, 4, 5]]}.
     *
     * @throws IllegalArgumentException if the window size is smaller than 1
     */
    public static <E> Stream<List<E>> windowed(List<E> list, int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Window size must be at least 1.");
        }

        return IntStream.rangeClosed(0, list.size() - size).mapToObj(i -> list.subList(i, i + size));
    }

    /**
     * Returns an unmodifiable map that is the inverse of the given map.
     * <p>
     * Note: this method simply constructs a new map each time it is called. If you need a dynamic view of the inverse
     * map, consider using Guava's {@code BiMap}.
     *
     * @throws IllegalArgumentException if the values of the given map are not unique
     */
    public static <K, V> Map<V, K> inverse(Map<K, V> map) {
        var inverse = new HashMap<V, K>();
        for (var e : map.entrySet()) {
            if (inverse.put(e.getValue(), e.getKey()) != null) {
                throw new IllegalArgumentException("The values of the map are not unique.");
            }
        }
        return Collections.unmodifiableMap(inverse);
    }

}
