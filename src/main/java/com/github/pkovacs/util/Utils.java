package com.github.pkovacs.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Provides various useful utility methods of different categories. They are not separated into multiple classes
 * so that you can extend this class for easier access to all its methods.
 * <ul>
 * <li>
 *     <b>Strings and text files.</b> These methods can be used for parsing inputs of coding puzzles and for working
 *     with regular expressions more easily. For example, {@link #readLines}, {@link #readString}, {@link #parseInts}.
 *     For the sake of simplicity, the file reading methods do not throw checked exceptions ({@link IOException}s
 *     are wrapped as {@link UncheckedIOException}s).
 * </li>
 * <li>
 *     <b>Collections and streams.</b> For example, {@link #listOf}, {@link #setOf}, {@link #streamOf} for converting
 *     arrays of primitive types to collections and streams (unfortunately, {@link Arrays#asList(Object[])} cannot be
 *     used for this); {@link #unionOf} and {@link #intersectionOf} for collections and streams;
 *     {@link #chunked(List, int)} and {@link #windowed(List, int)} to enumerate certain sublists of lists.
 * </li>
 * <li>
 *     <b>Arrays.</b> For example, {@link #reverse} and {@link #deepCopyOf} methods.
 * </li>
 * <li>
 *     <b>Math.</b> Simple util methods like the ones provided by {@link Math} and Guava's {@code LongMath}.
 *     For example, {@link #min} and {@link #max} for more than two arguments; {@link #wrapIndex}; {@link #gcd}
 *     and {@link #lcm}.
 * </li>
 * </ul>
 */
public class Utils {

    protected Utils() {
    }

    // **************************************** STRINGS AND TEXT FILES ****************************************

    /**
     * Returns a {@code Path} object for the given resource path relative to the given class.
     */
    public static Path getPath(Class<?> clazz, String resourcePath) {
        var resource = clazz.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException(String.format("Resource file not found: %s.", resourcePath));
        }

        try {
            return Path.of(resource.toURI());
        } catch (Exception e) {
            throw new IllegalArgumentException("Resource file not found.", e);
        }
    }

    /**
     * Reads all lines from the given input file.
     */
    public static List<String> readLines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads all characters from the given input file into a string.
     * Line separators are converted to UNIX/Mac style (LF).
     */
    public static String readString(Path path) {
        try {
            return convertLineSeparators(Files.readString(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads the lines of the given input file into a char matrix.
     */
    public static char[][] readCharMatrix(Path path) {
        return toCharMatrix(readLines(path));
    }

    /**
     * Returns the char matrix representation of the lines of the given strings.
     */
    public static char[][] toCharMatrix(String input) {
        return toCharMatrix(input.lines().toList());
    }

    /**
     * Returns the char matrix representation of the given list of strings.
     */
    public static char[][] toCharMatrix(List<String> lines) {
        var matrix = new char[lines.size()][];
        for (int i = 0, n = matrix.length; i < n; i++) {
            matrix[i] = lines.get(i).toCharArray();
        }
        return matrix;
    }

    /**
     * Reads the sections from the given input file. Sections are groups of lines separated by one or more blank lines.
     */
    public static List<List<String>> readSections(Path path) {
        return collectSections(readString(path));
    }

    /**
     * Collects the sections of the given string. Sections are groups of lines separated by one or more blank lines.
     */
    public static List<List<String>> collectSections(String input) {
        return Arrays.stream(convertLineSeparators(input).split("\n\n+"))
                .map(section -> List.of(section.split("\n")))
                .toList();
    }

    private static String convertLineSeparators(String str) {
        return str.replace("\r\n", "\n").replace("\r", "\n");
    }

    /**
     * Reads all integers from the given input file into an {@code int} array.
     * All other characters are ignored.
     * <p>
     * See {@link #parseInts(String)} for more details.
     */
    public static int[] readInts(Path path) {
        return parseInts(readString(path));
    }

    /**
     * Reads all integers from the given input file into a {@code long} array.
     * All other characters are ignored.
     * <p>
     * See {@link #parseLongs(String)} for more details.
     */
    public static long[] readLongs(Path path) {
        return parseLongs(readString(path));
    }

    /**
     * Parses all integers from the given string and returns them as an {@code int} array.
     * All other characters are ignored. A "-" character is considered as a unary minus sign if and only if it is
     * not directly preceded by a letter or digit (otherwise, it is considered as a separator instead).
     * <p>
     * Examples:
     * <pre>
     * "5 apples and 12 bananas"  --> {5, 12}
     * "A-10, B20"                --> {10, 20}
     * "[-10,20]"                 --> {-10, 20}
     * "5-3"                      --> {5, 3}
     * "5+-3"                     --> {5, -3}
     * </pre>
     */
    public static int[] parseInts(String input) {
        return PatternHolder.integerPattern.matcher(input).results()
                .map(MatchResult::group)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    /**
     * Parses all integers from the given string and returns them as a {@code long} array.
     * All other characters are ignored. A "-" character is considered as a unary minus sign if and only if it is
     * not directly preceded by a letter or digit (otherwise, it is considered as a separator instead).
     * <p>
     * Examples:
     * <pre>
     * "5 apples and 12 bananas"  --> {5, 12}
     * "A-10, B20"                --> {10, 20}
     * "[-10,20]"                 --> {-10, 20}
     * "5-3"                      --> {5, 3}
     * "5+-3"                     --> {5, -3}
     * </pre>
     */
    public static long[] parseLongs(String input) {
        return PatternHolder.integerPattern.matcher(input).results()
                .map(MatchResult::group)
                .mapToLong(Long::parseLong)
                .toArray();
    }

    /**
     * Parses the given character as a signed decimal integer.
     * This is just a shorthand for {@link Integer#parseInt(String)}.
     */
    public static int parseInt(char c) {
        return Integer.parseInt(String.valueOf(c));
    }

    /**
     * Parses the given character as a signed integer in the given radix.
     * This is just a shorthand for {@link Integer#parseInt(String, int)}.
     */
    public static int parseInt(char c, int radix) {
        return Integer.parseInt(String.valueOf(c), radix);
    }

    /**
     * Parses the given string as a signed decimal integer.
     * This is just a shorthand for {@link Integer#parseInt(String)}.
     */
    public static int parseInt(String s) {
        return Integer.parseInt(s);
    }

    /**
     * Parses the given string as a signed integer in the given radix.
     * This is just a shorthand for {@link Integer#parseInt(String, int)}.
     */
    public static int parseInt(String s, int radix) {
        return Integer.parseInt(s, radix);
    }

    /**
     * Parses the given string as a signed decimal integer.
     * This is just a shorthand for {@link Long#parseLong(String)}.
     */
    public static long parseLong(String s) {
        return Long.parseLong(s);
    }

    /**
     * Parses the given string as a signed integer in the given radix.
     * This is just a shorthand for {@link Long#parseLong(String, int)}.
     */
    public static long parseLong(String s, int radix) {
        return Long.parseLong(s, radix);
    }

    /**
     * Returns the {@link Matcher} object to match the given regular expression and input sequence.
     */
    public static Matcher matcher(String regex, CharSequence input) {
        return Pattern.compile(regex).matcher(input);
    }

    /**
     * Returns true if the given regular expression matches the entire given input sequence.
     */
    public static boolean matches(String regex, CharSequence input) {
        return matches(Pattern.compile(regex), input);
    }

    /**
     * Returns true if the given pattern matches the entire given input sequence.
     */
    public static boolean matches(Pattern pattern, CharSequence input) {
        return pattern.matcher(input).matches();
    }

    /**
     * Returns the input subsequences captured by the groups of the given regular expression with respect to its
     * first match.
     * <p>
     * For example, {@code findGroups("(\\d+) ([^ ]+)", "We have 12 apples and 5 bananas.")} returns
     * {@code ["12", "apples"]}.
     *
     * @throws NoSuchElementException if no matches found
     */
    public static List<String> findGroups(String regex, CharSequence input) {
        return findGroups(Pattern.compile(regex), input);
    }

    /**
     * Returns the input subsequences captured by the groups of the given pattern with respect to its first match.
     * <p>
     * For example, {@code findGroups(Pattern.compile("(\\d+) ([^ ]+)"), "We have 12 apples and 5 bananas.")} returns
     * {@code ["12", "apples"]}.
     *
     * @throws NoSuchElementException if no matches found
     */
    public static List<String> findGroups(Pattern pattern, CharSequence input) {
        var match = findFirstMatch(pattern, input);
        return IntStream.rangeClosed(1, match.groupCount()).mapToObj(match::group).toList();
    }

    /**
     * Returns the first match of the given regular expression within the given input sequence as a string.
     *
     * @throws NoSuchElementException if no matches found
     */
    public static String findFirst(String regex, CharSequence input) {
        return findFirst(Pattern.compile(regex), input);
    }

    /**
     * Returns the first match of the given regular expression within the given input sequence as a string.
     *
     * @throws NoSuchElementException if no matches found
     */
    public static String findFirst(Pattern pattern, CharSequence input) {
        return findFirstMatch(pattern, input).group();
    }

    /**
     * Returns the first match of the given regular expression within the given input sequence as a {@link MatchResult}.
     *
     * @throws NoSuchElementException if no matches found
     */
    public static MatchResult findFirstMatch(String regex, CharSequence input) {
        return findFirstMatch(Pattern.compile(regex), input);
    }

    /**
     * Returns the first match of the given pattern within the given input sequence as a {@link MatchResult}.
     *
     * @throws NoSuchElementException if no matches found
     */
    public static MatchResult findFirstMatch(Pattern pattern, CharSequence input) {
        return pattern.matcher(input).results().findFirst().orElseThrow();
    }

    /**
     * Returns all matches of the given regular expression within the given input sequence as a list of strings.
     */
    public static List<String> findAll(String regex, CharSequence input) {
        return findAll(Pattern.compile(regex), input);
    }

    /**
     * Returns all matches of the given pattern within the given input sequence as a list of strings.
     */
    public static List<String> findAll(Pattern pattern, CharSequence input) {
        return pattern.matcher(input).results().map(MatchResult::group).toList();
    }

    /**
     * Returns all matches of the given regular expression within the given input sequence as a list of
     * {@link MatchResult} objects.
     */
    public static List<MatchResult> findAllMatches(String regex, CharSequence input) {
        return findAllMatches(Pattern.compile(regex), input);
    }

    /**
     * Returns all matches of the given pattern within the given input sequence as a list of {@link MatchResult}
     * objects.
     */
    public static List<MatchResult> findAllMatches(Pattern pattern, CharSequence input) {
        return pattern.matcher(input).results().toList();
    }

    /**
     * Replaces each match of the given regular expression within the given input sequence with the result of
     * applying the given replacer function to the match result.
     */
    public static String replaceAll(String regex, CharSequence input, Function<MatchResult, String> replacer) {
        return replaceAll(Pattern.compile(regex), input, replacer);
    }

    /**
     * Replaces each match of the given pattern within the given input sequence with the result of applying the
     * given replacer function to the match result.
     */
    public static String replaceAll(Pattern pattern, CharSequence input, Function<MatchResult, String> replacer) {
        return pattern.matcher(input).replaceAll(replacer);
    }

    private static class PatternHolder {
        static final Pattern integerPattern = Pattern.compile("(?:(?<![a-zA-Z0-9])-)?\\d+");
    }

    // **************************************** COLLECTIONS AND STREAMS ****************************************

    /**
     * Returns the occurrence count of the given value in the given collection.
     */
    public static <T> int count(Collection<T> collection, T value) {
        return (int) collection.stream().filter(v -> Objects.equals(v, value)).count();
    }

    /**
     * Returns the occurrence count of the given character in the given string.
     */
    public static int count(CharSequence s, char ch) {
        return (int) charsOf(s).filter(c -> c == ch).count();
    }

    /**
     * Returns the given {@code int} values as an unmodifiable list.
     */
    public static List<Integer> listOf(int... ints) {
        return Arrays.stream(ints).boxed().toList();
    }

    /**
     * Returns the given {@code long} values as an unmodifiable list.
     */
    public static List<Long> listOf(long... longs) {
        return Arrays.stream(longs).boxed().toList();
    }

    /**
     * Returns the given {@code double} values as an unmodifiable list.
     */
    public static List<Double> listOf(double... doubles) {
        return Arrays.stream(doubles).boxed().toList();
    }

    /**
     * Returns the given {@code char} values as an unmodifiable list.
     */
    public static List<Character> listOf(char... chars) {
        return streamOf(chars).toList();
    }

    /**
     * Returns the given {@code int} values as an unmodifiable set.
     */
    public static Set<Integer> setOf(int... ints) {
        return Arrays.stream(ints).boxed().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the given {@code long} values as an unmodifiable set.
     */
    public static Set<Long> setOf(long... longs) {
        return Arrays.stream(longs).boxed().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the given {@code double} values as an unmodifiable set.
     */
    public static Set<Double> setOf(double... doubles) {
        return Arrays.stream(doubles).boxed().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the given {@code char} values as an unmodifiable set.
     */
    public static Set<Character> setOf(char... chars) {
        return streamOf(chars).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the given {@code int} values as an {@link IntStream}.
     */
    public static IntStream streamOf(int... ints) {
        return Arrays.stream(ints);
    }

    /**
     * Returns the given {@code long} values as a {@link LongStream}.
     */
    public static LongStream streamOf(long... longs) {
        return Arrays.stream(longs);
    }

    /**
     * Returns the given {@code double} values as a {@link DoubleStream}.
     */
    public static DoubleStream streamOf(double... doubles) {
        return Arrays.stream(doubles);
    }

    /**
     * Returns the given {@code char} values as a stream.
     */
    public static Stream<Character> streamOf(char... chars) {
        return IntStream.range(0, chars.length).mapToObj(i -> chars[i]);
    }

    /**
     * Returns the characters of the given {@code CharSequence} as a stream.
     */
    public static Stream<Character> charsOf(CharSequence s) {
        return s.toString().chars().mapToObj(i -> (char) i);
    }

    /**
     * Returns the union of the given two collections as a set.
     */
    public static <E> Set<E> unionOf(Collection<? extends E> a, Collection<? extends E> b) {
        return unionOf(List.of(a, b));
    }

    /**
     * Returns the union of the given streams as a set.
     */
    public static <E> Set<E> unionOf(Stream<? extends E> a, Stream<? extends E> b) {
        return unionOf(List.of(a.toList(), b.toList()));
    }

    /**
     * Returns the union of the given collections as a set.
     */
    public static <E> Set<E> unionOf(Collection<? extends Collection<? extends E>> collections) {
        var result = new HashSet<E>(collections.iterator().next());
        collections.stream().skip(1).forEach(result::addAll);
        return result;
    }

    /**
     * Returns the intersection of the given two collections as a set.
     */
    public static <E> Set<E> intersectionOf(Collection<? extends E> a, Collection<? extends E> b) {
        return intersectionOf(List.of(a, b));
    }

    /**
     * Returns the intersection of the given streams as a set.
     */
    public static <E> Set<E> intersectionOf(Stream<? extends E> a, Stream<? extends E> b) {
        return intersectionOf(List.of(a.toList(), b.toList()));
    }

    /**
     * Returns the intersection of the given collections as a set.
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
     * from the given list. As if the list was looking at through a sliding window of a certain size.
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

    // **************************************** ARRAYS ****************************************

    /**
     * Reverses the given {@code byte} array.
     */
    public static void reverse(byte[] array) {
        if (array.length <= 1) {
            return;
        }
        for (int i = 0, j = array.length - 1; i < j; i++, j--) {
            byte tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    /**
     * Reverses the given {@code int} array.
     */
    public static void reverse(int[] array) {
        if (array.length <= 1) {
            return;
        }
        for (int i = 0, j = array.length - 1; i < j; i++, j--) {
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    /**
     * Reverses the given {@code long} array.
     */
    public static void reverse(long[] array) {
        if (array.length <= 1) {
            return;
        }
        for (int i = 0, j = array.length - 1; i < j; i++, j--) {
            long tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    /**
     * Reverses the given {@code double} array.
     */
    public static void reverse(double[] array) {
        if (array.length <= 1) {
            return;
        }
        for (int i = 0, j = array.length - 1; i < j; i++, j--) {
            double tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    /**
     * Reverses the given {@code char} array.
     */
    public static void reverse(char[] array) {
        if (array.length <= 1) {
            return;
        }
        for (int i = 0, j = array.length - 1; i < j; i++, j--) {
            char tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    /**
     * Reverses the given {@code boolean} array.
     */
    public static void reverse(boolean[] array) {
        if (array.length <= 1) {
            return;
        }
        for (int i = 0, j = array.length - 1; i < j; i++, j--) {
            boolean tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    /**
     * Reverses the given array.
     */
    public static <T> void reverse(T[] array) {
        if (array.length <= 1) {
            return;
        }
        for (int i = 0, j = array.length - 1; i < j; i++, j--) {
            T tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    /**
     * Returns a deep copy of the given {@code byte} matrix.
     * The "rows" might have different sizes, but they must not be null.
     */
    public static byte[][] deepCopyOf(byte[][] matrix) {
        var result = new byte[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = matrix[i].clone();
        }
        return result;
    }

    /**
     * Returns a deep copy of the given {@code int} matrix.
     * The "rows" might have different sizes, but they must not be null.
     */
    public static int[][] deepCopyOf(int[][] matrix) {
        var result = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = matrix[i].clone();
        }
        return result;
    }

    /**
     * Returns a deep copy of the given {@code long} matrix.
     * The "rows" might have different sizes, but they must not be null.
     */
    public static long[][] deepCopyOf(long[][] matrix) {
        var result = new long[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = matrix[i].clone();
        }
        return result;
    }

    /**
     * Returns a deep copy of the given {@code double} matrix.
     * The "rows" might have different sizes, but they must not be null.
     */
    public static double[][] deepCopyOf(double[][] matrix) {
        var result = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = matrix[i].clone();
        }
        return result;
    }

    /**
     * Returns a deep copy of the given {@code char} matrix.
     * The "rows" might have different sizes, but they must not be null.
     */
    public static char[][] deepCopyOf(char[][] matrix) {
        var result = new char[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = matrix[i].clone();
        }
        return result;
    }

    /**
     * Returns a deep copy of the given {@code boolean} matrix.
     * The "rows" might have different sizes, but they must not be null.
     */
    public static boolean[][] deepCopyOf(boolean[][] matrix) {
        var result = new boolean[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = matrix[i].clone();
        }
        return result;
    }

    // **************************************** MATH ****************************************

    /**
     * Returns the minimum of the given {@code int} values.
     *
     * @throws NoSuchElementException if no numbers are given
     */
    public static int min(int... ints) {
        return streamOf(ints).min().orElseThrow();
    }

    /**
     * Returns the minimum of the given {@code long} values.
     *
     * @throws NoSuchElementException if no numbers are given
     */
    public static long min(long... longs) {
        return streamOf(longs).min().orElseThrow();
    }

    /**
     * Returns the minimum of the given {@code double} values.
     *
     * @throws NoSuchElementException if no numbers are given
     */
    public static double min(double... doubles) {
        return streamOf(doubles).min().orElseThrow();
    }

    /**
     * Returns the minimum of the given {@code char} values.
     *
     * @throws NoSuchElementException if no characters are given
     */
    public static char min(char... chars) {
        return streamOf(chars).min(Comparator.naturalOrder()).orElseThrow();
    }

    /**
     * Returns the minimum of the given comparable values.
     *
     * @throws NoSuchElementException if no values are given
     */
    public static <T extends Comparable<T>> T min(Collection<T> values) {
        return values.stream().min(Comparator.naturalOrder()).orElseThrow();
    }

    /**
     * Returns the maximum of the given {@code int} values.
     *
     * @throws NoSuchElementException if no numbers are given
     */
    public static int max(int... ints) {
        return streamOf(ints).max().orElseThrow();
    }

    /**
     * Returns the maximum of the given {@code long} values.
     *
     * @throws NoSuchElementException if no numbers are given
     */
    public static long max(long... longs) {
        return streamOf(longs).max().orElseThrow();
    }

    /**
     * Returns the maximum of the given {@code double} values.
     *
     * @throws NoSuchElementException if no numbers are given
     */
    public static double max(double... doubles) {
        return streamOf(doubles).max().orElseThrow();
    }

    /**
     * Returns the maximum of the given {@code char} values.
     *
     * @throws NoSuchElementException if no characters are given
     */
    public static char max(char... chars) {
        return streamOf(chars).max(Comparator.naturalOrder()).orElseThrow();
    }

    /**
     * Returns the maximum of the given comparable values.
     *
     * @throws NoSuchElementException if no values are given
     */
    public static <T extends Comparable<T>> T max(Collection<T> values) {
        return values.stream().max(Comparator.naturalOrder()).orElseThrow();
    }

    /**
     * Returns the absolute value of the given {@code int} value.
     * This is just a shorthand for {@link Math#absExact(int)}.
     *
     * @throws ArithmeticException if the argument is {@link Integer#MIN_VALUE}
     */
    public static int abs(int value) {
        return Math.absExact(value);
    }

    /**
     * Returns the absolute value of the given {@code long} value.
     * This is just a shorthand for {@link Math#absExact(long)}.
     *
     * @throws ArithmeticException if the argument is {@link Long#MIN_VALUE}
     */
    public static long abs(long value) {
        return Math.absExact(value);
    }

    /**
     * Returns the absolute value of the given {@code double} value.
     * This is just a shorthand for {@link Math#abs(double)}.
     */
    public static double abs(double value) {
        return Math.abs(value);
    }

    /**
     * Returns the first argument raised to the power of the second argument.
     *
     * @throws IllegalArgumentException if the exponent (the second argument) is negative
     */
    public static long pow(long a, long b) {
        if (b < 0) {
            throw new IllegalArgumentException("Negative exponent.");
        }

        if (a >= 0 && a <= 2) {
            return switch ((int) a) {
                case 0 -> (b == 0) ? 1 : 0;
                case 1 -> 1;
                case 2 -> 1L << b;
                default -> throw new AssertionError();
            };
        }

        long r = 1;
        for (; b != 0; b >>= 1) {
            r *= ((b & 1) == 0) ? 1 : a;
            a *= a;
        }
        return r;
    }

    /**
     * Returns the <i>greatest common divisor</i> (GCD) of the given two non-negative {@code long} values.
     *
     * @throws IllegalArgumentException if {@code a < 0} or {@code b < 0}
     */
    public static long gcd(long a, long b) {
        if (a < 0 || b < 0) {
            throw new IllegalArgumentException("Negative numbers are not supported.");
        }

        return b == 0 ? a : gcd(b, a % b);
    }

    /**
     * Returns the <i>greatest common divisor</i> (GCD) of the given non-negative {@code int} values.
     *
     * @throws IllegalArgumentException if any of the given values is negative
     */
    public static long gcd(int... values) {
        return gcd(IntStream.of(values));
    }

    /**
     * Returns the <i>greatest common divisor</i> (GCD) of the given non-negative {@code int} values.
     *
     * @throws IllegalArgumentException if any of the given values is negative
     */
    public static long gcd(IntStream values) {
        return gcd(values.mapToLong(i -> i));
    }

    /**
     * Returns the <i>greatest common divisor</i> (GCD) of the given non-negative {@code long} values.
     *
     * @throws IllegalArgumentException if any of the given values is negative
     */
    public static long gcd(long... values) {
        return gcd(LongStream.of(values));
    }

    /**
     * Returns the <i>greatest common divisor</i> (GCD) of the given non-negative {@code long} values.
     *
     * @throws IllegalArgumentException if any of the given values is negative
     */
    public static long gcd(LongStream values) {
        return values.reduce(0L, (a, b) -> gcd(a, b));
    }

    /**
     * Returns the <i>greatest common divisor</i> (GCD) of the given non-negative integers.
     *
     * @throws IllegalArgumentException if any of the given values is negative or not of type {@code Integer}
     *         or {@code Long}
     */
    public static long gcd(Collection<? extends Number> values) {
        if (values.stream().map(Object::getClass).anyMatch(c -> c != Integer.class && c != Long.class)) {
            throw new IllegalArgumentException("Only Integer and Long values are supported");
        }

        return gcd(values.stream().mapToLong(Number::longValue));
    }

    /**
     * Returns the <i>least common multiple</i> (LCM) of the given two non-negative {@code long} values.
     */
    public static long lcm(long a, long b) {
        if (a < 0 || b < 0) {
            throw new IllegalArgumentException("Negative numbers are not supported.");
        }

        return a / gcd(a, b) * b; // note: unusual order of operations to avoid overflow
    }

    /**
     * Returns the <i>least common multiple</i> (LCM) of the given non-negative {@code int} values.
     *
     * @throws IllegalArgumentException if any of the given values is negative
     */
    public static long lcm(int... values) {
        return lcm(IntStream.of(values));
    }

    /**
     * Returns the <i>least common multiple</i> (LCM) of the given non-negative {@code int} values.
     *
     * @throws IllegalArgumentException if any of the given values is negative
     */
    public static long lcm(IntStream values) {
        return lcm(values.mapToLong(i -> i));
    }

    /**
     * Returns the <i>least common multiple</i> (LCM) of the given non-negative {@code long} values.
     *
     * @throws IllegalArgumentException if any of the given values is negative
     */
    public static long lcm(long... values) {
        return lcm(LongStream.of(values));
    }

    /**
     * Returns the <i>least common multiple</i> (LCM) of the given non-negative {@code long} values.
     *
     * @throws IllegalArgumentException if any of the given values is negative
     */
    public static long lcm(LongStream values) {
        return values.reduce(1L, (a, b) -> lcm(a, b));
    }

    /**
     * Returns the <i>least common multiple</i> (LCM) of the given non-negative integers.
     *
     * @throws IllegalArgumentException if any of the given values is negative or not of type {@code Integer}
     *         or {@code Long}
     */
    public static long lcm(Collection<? extends Number> values) {
        if (values.stream().map(Object::getClass).anyMatch(c -> c != Integer.class && c != Long.class)) {
            throw new IllegalArgumentException("Only Integer and Long values are supported");
        }

        return lcm(values.stream().mapToLong(Number::longValue));
    }

    /**
     * Constrains the given {@code index} to the closed range {@code [0..(size - 1)]}.
     */
    public static int constrainIndex(long index, int size) {
        return constrainToRange(index, 0, size - 1);
    }

    /**
     * Constrains the given {@code index} to the closed range {@code [0..(size - 1)]}.
     */
    public static long constrainIndex(long index, long size) {
        return constrainToRange(index, 0, size - 1);
    }

    /**
     * Constrains the given int {@code value} to the closed range {@code [min..max]}.
     */
    public static int constrainToRange(long value, int min, int max) {
        return Math.clamp(value, min, max);
    }

    /**
     * Constrains the given long {@code value} to the closed range {@code [min..max]}.
     */
    public static long constrainToRange(long value, long min, long max) {
        return Math.clamp(value, min, max);
    }

    /**
     * Wraps the given {@code index} to the closed range {@code [0..(size - 1)]}.
     */
    public static int wrapIndex(long index, int size) {
        checkRange(0, size - 1);
        return Math.floorMod(index, size);
    }

    /**
     * Wraps the given {@code index} to the closed range {@code [0..(size - 1)]}.
     */
    public static long wrapIndex(long index, long size) {
        checkRange(0L, size - 1);
        return Math.floorMod(index, size);
    }

    /**
     * Checks if the given {@code index} is within the closed range {@code [0..(size - 1)]}.
     */
    public static boolean isValidIndex(long index, long size) {
        checkRange(0L, size - 1);
        return index >= 0 && index < size;
    }

    /**
     * Checks if the given index is valid for the given collection.
     */
    public static boolean isValidIndex(long index, Collection<?> collection) {
        return isValidIndex(index, collection.size());
    }

    /**
     * Checks if the given index is valid for the given array.
     */
    public static <T> boolean isValidIndex(long index, T[] array) {
        return isValidIndex(index, array.length);
    }

    /**
     * Checks if the given index is valid for the given array.
     */
    public static boolean isValidIndex(long index, int[] array) {
        return isValidIndex(index, array.length);
    }

    /**
     * Checks if the given index is valid for the given array.
     */
    public static boolean isValidIndex(long index, long[] array) {
        return isValidIndex(index, array.length);
    }

    /**
     * Checks if the given index is valid for the given array.
     */
    public static boolean isValidIndex(long index, byte[] array) {
        return isValidIndex(index, array.length);
    }

    /**
     * Checks if the given index is valid for the given array.
     */
    public static boolean isValidIndex(long index, char[] array) {
        return isValidIndex(index, array.length);
    }

    /**
     * Checks if the given index is valid for the character sequence.
     */
    public static boolean isValidIndex(long index, CharSequence s) {
        return isValidIndex(index, s.length());
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

}
