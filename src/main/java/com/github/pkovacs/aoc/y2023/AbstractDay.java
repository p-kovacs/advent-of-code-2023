package com.github.pkovacs.aoc.y2023;

import java.lang.StackWalker.Option;
import java.nio.file.Path;
import java.util.Locale;

import com.github.pkovacs.util.Utils;

/**
 * Abstract base class of the Day classes.
 */
public abstract class AbstractDay extends Utils {

    protected AbstractDay() {
    }

    /**
     * Returns a {@link Path} object that locates the input file corresponding to the caller class.
     * For example, if this method is called from class {@code Day05}, then {@code "input/day05.txt"} is located.
     */
    public static Path getInputPath() {
        var cl = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        var fileName = cl.getSimpleName().toLowerCase(Locale.ROOT) + ".txt";
        return Path.of("input", fileName);
    }

}
