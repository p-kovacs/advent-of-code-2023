package com.github.pkovacs.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Provides simple utility methods for processing strings and text files. They can be used to parse inputs of various
 * coding puzzles.
 * <p>
 * For the sake of simplicity, the methods do not throw checked exceptions. {@link IOException}s are wrapped in
 * {@link UncheckedIOException} objects.
 */
public class InputUtils {

    private static final Pattern integerPattern = Pattern.compile("(?:(?<![a-zA-Z0-9])-)?\\d+");

    protected InputUtils() {
    }

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
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads the first line from the given input file. This method can be practical if the input is a single line,
     * and you would like to read it without line breaks (in contrast with {@link #readString(Path)}).
     */
    public static String readFirstLine(Path path) {
        return readLines(path).get(0);
    }

    /**
     * Reads all characters from the given input file into a string.
     * Line separators are converted to UNIX/Mac style (LF).
     */
    public static String readString(Path path) {
        try {
            return convertLineSeparators(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads the lines of the given input file into a char matrix.
     */
    public static char[][] readCharMatrix(Path path) {
        var lines = readLines(path);
        var matrix = new char[lines.size()][];
        for (int i = 0, n = matrix.length; i < n; i++) {
            matrix[i] = lines.get(i).toCharArray();
        }
        return matrix;
    }

    /**
     * Reads blocks of lines (separated by blank line(s)) from the given input file.
     */
    public static List<List<String>> readLineBlocks(Path path) {
        return collectLineBlocks(readString(path));
    }

    /**
     * Collects blocks of lines (separated by blank line(s)) from the given string.
     */
    public static List<List<String>> collectLineBlocks(String input) {
        return Arrays.stream(convertLineSeparators(input).split("\n(\n)+"))
                .map(block -> List.of(block.split("\n")))
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
     * All other characters are ignored. A "-" character is considered as a minus sign if and only if
     * it is not directly preceded by a letter or digit.
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
        return integerPattern.matcher(input).results()
                .map(MatchResult::group)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    /**
     * Parses all integers from the given string and returns them as a {@code long} array.
     * All other characters are ignored. A "-" character is considered as a minus sign if and only if
     * it is not directly preceded by a letter or digit.
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
        return integerPattern.matcher(input).results()
                .map(MatchResult::group)
                .mapToLong(Long::parseLong)
                .toArray();
    }

    /**
     * Parses the given character as an integer in radix 36. That is, both digits and letters are accepted.
     * Characters from '0' to '9' are parsed as integers from 0 to 9, while characters from 'a' to 'z' and
     * from 'A' to 'Z' are parsed as integers from 10 to 35.
     */
    public static int parseInt(char c) {
        return Integer.parseInt(String.valueOf(c), 36);
    }

    /**
     * Parses the given input string according to the given pattern (similarly to the {@code scanf} method in C)
     * and returns the parsed values.
     * <p>
     * The given pattern may contain "%d", "%c", "%s". Otherwise, it is considered as a regular expression,
     * so be aware of escaping special characters like '(', ')', '[', ']', '.', '*', '?' etc. Furthermore,
     * it must not contain capturing groups (unescaped '(' and ')').
     * <p>
     * The returned list contains the parsed values in the order of their occurrence in the input.
     *
     * @param str input string
     * @param pattern pattern string: a regular expression that may contain "%d", "%c", "%s", but must not
     *         contain capturing groups (unescaped '(' and ')'). For example, "Product %s: .* %d out of %d".
     * @return the list of {@link ParsedValue} objects, which can be obtained as int, long, char, or String
     */
    public static List<ParsedValue> parse(String str, String pattern) {
        var groupPatterns = RegexUtils.findAll("%.", pattern);

        var regex = pattern.replace("%d", "(\\d+)")
                .replace("%c", "(.)")
                .replace("%s", "(.*)");

        var result = new ArrayList<ParsedValue>();
        var matcher = Pattern.compile(regex).matcher(str);
        if (matcher.matches()) {
            if (matcher.groupCount() == groupPatterns.size()) {
                for (int i = 0; i < groupPatterns.size(); i++) {
                    var group = matcher.group(i + 1); // 0-th group is the entire match
                    result.add(ParsedValue.parse(group, groupPatterns.get(i)));
                }
            } else {
                throw new IllegalArgumentException(String.format(
                        "Input string '%s' has %d groups instead of expected %d for regular expression '%s'"
                                + " (created from pattern '%s').",
                        str, matcher.groupCount(), groupPatterns.size(), regex, pattern));
            }
        } else {
            throw new IllegalArgumentException(String.format(
                    "Input string '%s' does not match the regular expression '%s' (created from pattern '%s').",
                    str, regex, pattern));
        }

        return result;
    }

    /**
     * Represents a value parsed by {@link #parse(String, String)}.
     */
    public final static class ParsedValue {

        private final Object value;

        private ParsedValue(Object value) {
            this.value = value;
        }

        private static ParsedValue parse(String s, String pattern) {
            return switch (pattern) {
                case "%d" -> new ParsedValue(Long.parseLong(s));
                case "%c" -> new ParsedValue(s.charAt(0));
                default -> new ParsedValue(s);
            };
        }

        public boolean isLong() {
            return value.getClass().equals(Long.class);
        }

        public boolean isChar() {
            return value.getClass().equals(Character.class);
        }

        public boolean isString() {
            return value.getClass().equals(String.class);
        }

        public String get() {
            return String.valueOf(value);
        }

        public int toInt() {
            return (int) toLong();
        }

        public long toLong() {
            return (long) value;
        }

        public char toChar() {
            return (char) value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

    }

}
