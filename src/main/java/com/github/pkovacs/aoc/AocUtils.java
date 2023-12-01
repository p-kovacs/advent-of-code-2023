package com.github.pkovacs.aoc;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import com.github.pkovacs.util.InputUtils;

/**
 * Provides a helper method to locate puzzle input files.
 */
public final class AocUtils {

    private static final Pattern classNamePattern =
            Pattern.compile("com[.]github[.]pkovacs[.]aoc[.]y20\\d\\d.Day\\d\\d");

    private AocUtils() {
    }

    /**
     * Returns a {@link Path} object that locates the input file corresponding to the caller class.
     * For example, if this method is called from class {@code Day05}, then {@code day05.txt} is located.
     */
    public static Path getInputPath() {
        var stacktrace = Thread.currentThread().getStackTrace();
        var path = Arrays.stream(stacktrace)
                .map(StackTraceElement::getClassName)
                .filter(name -> classNamePattern.matcher(name).matches())
                .findFirst()
                .map(name -> "/" + name.toLowerCase(Locale.ENGLISH).replace(".", "/") + ".txt")
                .orElseThrow(() -> new RuntimeException("Input file not found."));
        return InputUtils.getPath(AocUtils.class, path);
    }

}
