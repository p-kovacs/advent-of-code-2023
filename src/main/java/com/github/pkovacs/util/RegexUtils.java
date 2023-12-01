package com.github.pkovacs.util;

import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides simple utility methods for working with regular expressions.
 * They are just convenient wrappers for the features of {@link Pattern} and {@link Matcher}.
 */
public final class RegexUtils {

    private RegexUtils() {
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
        return Pattern.matches(regex, input);
    }

    /**
     * Returns the first match of the given regular expression within the given input sequence as a string.
     *
     * @return the first matching substring of the input
     * @throws java.util.NoSuchElementException if no matches found
     */
    public static String findFirst(String regex, CharSequence input) {
        return matcher(regex, input).results().map(MatchResult::group).findFirst().orElseThrow();
    }

    /**
     * Returns the first match of the given regular expression within the given input sequence as a {@link MatchResult}
     * object.
     *
     * @return the first {@link MatchResult}
     * @throws java.util.NoSuchElementException if no matches found
     */
    public static MatchResult findFirstMatch(String regex, CharSequence input) {
        return matcher(regex, input).results().findFirst().orElseThrow();
    }

    /**
     * Returns all matches of the given regular expression within the given input sequence as strings.
     */
    public static List<String> findAll(String regex, CharSequence input) {
        return matcher(regex, input).results().map(MatchResult::group).toList();
    }

    /**
     * Returns all matches of the given regular expression within the given input sequence as {@link MatchResult}
     * objects.
     */
    public static List<MatchResult> findAllMatches(String regex, CharSequence input) {
        return matcher(regex, input).results().toList();
    }

    /**
     * Replaces each match of the given regular expression within the given input sequence with the result of
     * applying the given replacer function to the match result.
     */
    public static String replaceAll(String regex, CharSequence input, Function<MatchResult, String> replacer) {
        return matcher(regex, input).replaceAll(replacer);
    }

}
