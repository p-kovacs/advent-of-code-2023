package com.github.pkovacs.aoc.y2023;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import com.github.pkovacs.util.data.Vector;

public class Day24 extends AbstractDay {

    private static final int PRECISION = 100; // precision for BigDecimal calculations (number of decimal digits)
    private static final MathContext MC = new MathContext(PRECISION, RoundingMode.HALF_EVEN);
    private static final BigDecimal EPS = BigDecimal.ONE.scaleByPowerOfTen(-PRECISION / 2);

    public static void main(String[] args) {
        var lines = readLines(getInputPath());

        var hailstones = lines.stream().map(Hailstone::fromString).toList();

        System.out.println("Part 1: " + solve1(hailstones));
        System.out.println("Part 2: " + solve2(hailstones));
    }

    private static long solve1(List<Hailstone> hailstones) {
        var min = BigDecimal.valueOf(200000000000000L);
        var max = BigDecimal.valueOf(400000000000000L);

        int count = 0;
        for (int i = 0; i < hailstones.size(); i++) {
            for (int j = i + 1; j < hailstones.size(); j++) {
                if (hasIntersectionInArea(hailstones.get(i), hailstones.get(j), min, max)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Checks if the future paths of the given two hailstones have an intersection within the test area. This
     * method is only used for part 1, so the z axis is ignored.
     * <p>
     * Given a hailstone with parameters p.x, p.y, v.x, v.y, its line can be formulated as an equation like
     * {@code y = m * x + b} provided that {@code v.x != 0}. The elapsed time for any point {@code (x,y)} along
     * the line of the hailstone can be calculated as {@code t = (x - p.x) / v.x}. Substituting this into
     * {@code y = p.y + t * v.y}, we get:
     * <pre>
     * y = (v.y / v.x) * x + p.y - (v.y / v.x) * p.x
     * </pre>
     * <p>
     * So we can assume that we have equations for the two hailstones like this:
     * <pre>
     * (1)   y = m1 * x + b1
     * (2)   y = m2 * x + b2
     * </pre>
     * If {@code m1 == m2}, then the lines are parallel, so we should check their starting positions. Otherwise,
     * the x coordinate of the intersection point can be calculated as {@code x = (b2 - b1) / (m1 - m2)}
     * (according to (1) - (2)). Then the y coordinate can be calculated according to (1).
     * <p>
     * For the sake of simplicity, we assume that {@code v.x != 0} for each hailstone, and two hailstones do not have
     * exactly the same line in terms of x and y coordinates. These conditions most likely hold for each input file,
     * so we simply throw an exception if they do not hold.
     * <p>
     * To avoid numerical issues, {@link BigDecimal} is used for the calculation instead of double.
     */
    private static boolean hasIntersectionInArea(Hailstone h1, Hailstone h2, BigDecimal min, BigDecimal max) {
        if (h1.v.x() == 0 || h2.v.x() == 0) {
            throw new IllegalArgumentException("Velocity vector with x coordinate equal to zero is not supported.");
        }

        var m1 = BigDecimal.valueOf(h1.v.y()).divide(BigDecimal.valueOf(h1.v.x()), MC);
        var b1 = BigDecimal.valueOf(h1.p.y()).subtract(m1.multiply(BigDecimal.valueOf(h1.p.x()), MC));
        var m2 = BigDecimal.valueOf(h2.v.y()).divide(BigDecimal.valueOf(h2.v.x()), MC);
        var b2 = BigDecimal.valueOf(h2.p.y()).subtract(m2.multiply(BigDecimal.valueOf(h2.p.x()), MC));

        if (isZero(m1.subtract(m2))) {
            // Parallel lines: check if the starting position of h2 is on the line of h1 or not
            var expectedY = m1.multiply(BigDecimal.valueOf(h2.p.x()), MC).add(b1);
            if (isZero(BigDecimal.valueOf(h2.p.y()).subtract(expectedY))) {
                throw new IllegalArgumentException("Two hailstones have the same line.");
            }
            return false;
        } else {
            // Otherwise: calculate and check intersection point
            var x = b2.subtract(b1).divide(m1.subtract(m2), MC);
            var y = m1.multiply(x, MC).add(b1);

            return x.subtract(BigDecimal.valueOf(h1.p.x())).signum() == Math.signum(h1.v.x()) // future for h1
                    && x.subtract(BigDecimal.valueOf(h2.p.x())).signum() == Math.signum(h2.v.x()) // future for h2
                    && min.compareTo(x) <= 0 && max.compareTo(x) >= 0 // x in the range
                    && min.compareTo(y) <= 0 && max.compareTo(y) >= 0; // y in the range
        }
    }

    /**
     * Solves part 2.
     * <p>
     * This puzzle can easily be described in terms of non-linear equations. With appropriate transformations,
     * however, these equations can be converted to a system of linear equations, which can be solved using
     * <a href="https://en.wikipedia.org/wiki/Gaussian_elimination">Gaussian elimination</a> or a suitable tool
     * or library (like {@code numpy.linalg.solve()} in Python).
     * <p>
     * Let x, y, z, vx, vy, vz denote the variables we would like to calculate. Given two hailstones (indexed by
     * 1 and 2), their parameters are denoted as x1, y1, z1, vx1, vy1, vz1, x2, y2, z2, vx2, vy2, vz2, respectively,
     * which have known values. Furthermore, two additional variables, t1 and t2 denote the elapsed time until the
     * collision of the rock and the two hailstones, respectively.
     * <p>
     * Let's consider only x and y coordinates first. The straightforward initial equations look like this:
     * <pre>
     * (1)   x + t1 * vx = x1 + t1 * vx1
     * (2)   y + t1 * vy = y1 + t1 * vy1
     * (3)   x + t2 * vx = x2 + t2 * vx2
     * (4)   y + t2 * vy = y2 + t2 * vy2
     * </pre>
     * We can reorganize these equations to express t1 and t2 as follows:
     * <pre>
     * (1')  t1 = (x - x1) / (vx1 - vx)        (provided that vx != vx1)
     * (2')  t1 = (y - y1) / (vy1 - vy)        (provided that vy != vy1)
     * (3')  t2 = (x - x2) / (vx2 - vx)        (provided that vx != vx2)
     * (4')  t2 = (y - y2) / (vy2 - vy)        (provided that vy != vy2)
     * </pre>
     * Combining (1') and (2'), we get the following equation (A), and after expanding the brackets, we get the
     * equation (A').
     * <pre>
     * (A)   (x - x1) * (vy1 - vy) = (y - y1) * (vx1 - vx)
     * (A')  x * vy1 - x * vy - x1 * vy1 + x1 * vy = y * vx1 - y * vx - y1 * vx1 + y1 * vx
     * </pre>
     * Similarly, combining (3') and (4'), we get the following equations.
     * <pre>
     * (B)   (x - x2) * (vy2 - vy) = (y - y2) * (vx2 - vx)
     * (B')  x * vy2 - x * vy - x2 * vy2 + x2 * vy = y * vx2 - y * vx - y2 * vx2 + y2 * vx
     * </pre>
     * Note that (A') and (B') are non-linear equations, but if we subtract one from the other, then the non-linear
     * elements {@code x * vy} and {@code y * vx} can be eliminated. In fact, (A') - (B') can be reformulated like
     * this:
     * <pre>
     * (vy1 - vy2) * x + (vx2 - vx1) * y + (y2 - y1) * vx + (x1 - x2) * vy = x1 * vy1 - x2 * vy2 + y2 * vx2 - y1 * vx1
     * </pre>
     * That is, considering a pair of hailstones, we can get a linear equation for the variables x, y, vx, vy.
     * Considering 4 pairs of hailstones, we can get 4 linear equations for these 4 variables, which can be solved
     * provided that the equations are linearly independent.
     * <p>
     * This way we can determine x and y coordinates of the rock. The z coordinate can be calculated e.g. by simply
     * switching the roles of y and z coordinates and repeating the calculation. For the sake of simplicity, we
     * assume that the system of equations derived from the first few hailstones is actually independent for both
     * calculations. This condition most likely holds for each input file, so we simply throw an exception if it does
     * not hold.
     * <p>
     * Furthermore, we should also take care of the preconditions for equations (1'), (2'), (3'), (4'). For example,
     * if {@code vx == vx1}, then (A) might hold even if the original equation (1) does not. Therefore, in such
     * special cases, we should check if {@code x == x1} also holds. And similarly for each coordinate of each
     * hailstone: if any coordinate of a hailstone's velocity is equal to the corresponding coordinate of the
     * rock's velocity, then their starting positions must also be equal at that coordinate.
     * <p>
     * Another issue to be considered is numerical stability. It turned out that using double values for internal
     * calculations occasionally results in off-by-one errors after rounding to long at the end (depending on the
     * input and the selection of the hailstone pairs). Therefore, {@link BigDecimal} is used instead of double.
     */
    private static long solve2(List<Hailstone> hailstones) {
        // Determine x, y, vx, vy
        var matrix = new BigDecimal[4][];
        for (int i = 0; i < 4; i++) {
            var a = hailstones.get(i);
            var b = hailstones.get(i + 1);
            matrix[i] = getMatrixRow(a.p.x(), a.p.y(), a.v.x(), a.v.y(), b.p.x(), b.p.y(), b.v.x(), b.v.y());
        }
        var result = gaussElimination(matrix);
        long x = Math.round(result[0].doubleValue());
        long y = Math.round(result[1].doubleValue());
        long vx = Math.round(result[2].doubleValue());
        long vy = Math.round(result[3].doubleValue());

        // Determine z and vz
        matrix = new BigDecimal[4][];
        for (int i = 0; i < 4; i++) {
            var a = hailstones.get(i);
            var b = hailstones.get(i + 1);
            matrix[i] = getMatrixRow(a.p.x(), a.p.z(), a.v.x(), a.v.z(), b.p.x(), b.p.z(), b.v.x(), b.v.z());
        }
        result = gaussElimination(matrix);
        long z = Math.round(result[1].doubleValue());
        long vz = Math.round(result[3].doubleValue());

        // Check compatibility with each hailstone
        if (hailstones.stream().anyMatch(a -> a.v.x() == vx && a.p.x() != x)
                || hailstones.stream().anyMatch(a -> a.v.y() == vy && a.p.y() != y)
                || hailstones.stream().anyMatch(a -> a.v.z() == vz && a.p.z() != z)) {
            throw new IllegalArgumentException("Necessary conditions do not hold.");
        }

        return x + y + z;
    }

    private static BigDecimal[] getMatrixRow(long x1, long y1, long vx1, long vy1,
            long x2, long y2, long vx2, long vy2) {
        return getMatrixRow(
                BigDecimal.valueOf(x1), BigDecimal.valueOf(y1), BigDecimal.valueOf(vx1), BigDecimal.valueOf(vy1),
                BigDecimal.valueOf(x2), BigDecimal.valueOf(y2), BigDecimal.valueOf(vx2), BigDecimal.valueOf(vy2));
    }

    private static BigDecimal[] getMatrixRow(BigDecimal x1, BigDecimal y1, BigDecimal vx1, BigDecimal vy1,
            BigDecimal x2, BigDecimal y2, BigDecimal vx2, BigDecimal vy2) {
        return new BigDecimal[] {
                vy1.subtract(vy2),
                vx2.subtract(vx1),
                y2.subtract(y1),
                x1.subtract(x2),
                x1.multiply(vy1, MC).subtract(x2.multiply(vy2, MC))
                        .add(y2.multiply(vx2, MC)).subtract(y1.multiply(vx1, MC))
        };
    }

    /**
     * A simple implementation of <a href="https://en.wikipedia.org/wiki/Gaussian_elimination">Gaussian elimination</a>.
     * It operates on the so-called "augmented matrix", which contains the coefficients and the right hand side values.
     * For the sake of simplicity, we assume that the number of equations is equal to the number of variables
     * (i.e. the matrix must have n rows and n + 1 columns), and the input matrix is transformed in place.
     *
     * @return the solution (the value of each variable)
     * @throws IllegalArgumentException if the number of equations is not the same as the number of variables,
     *         or if no solution is found because the rank of the matrix is lower than the number of variables.
     */
    private static BigDecimal[] gaussElimination(BigDecimal[][] matrix) {
        // Check the number of rows and columns in the matrix
        int n = matrix.length;
        if (Arrays.stream(matrix).anyMatch(row -> row.length != n + 1)) {
            throw new IllegalArgumentException("The number of equations is not the same as the number of variables.");
        }

        // Perform row operations
        for (int i = 0; i < n; i++) {
            // Select pivot element: the one with maximum absolute value in column i
            // (this selection attempts to avoid zero elements and also improves numerical stability of the method)
            int row = i;
            var pivot = matrix[i][i];
            for (int k = i + 1; k < n; k++) {
                if (matrix[k][i].abs().compareTo(pivot.abs()) > 0) {
                    pivot = matrix[k][i];
                    row = k;
                }
            }
            if (isZero(pivot)) {
                throw new IllegalArgumentException(
                        "Cannot be solved: the rank of the matrix is lower than the number of equations.");
            }

            // Swap rows
            var pivotRow = matrix[row];
            matrix[row] = matrix[i];
            matrix[i] = pivotRow;

            // Normalize row i
            for (int j = 0; j < n + 1; j++) {
                matrix[i][j] = matrix[i][j].divide(pivot, MC);
            }

            // Transform all rows except for i
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    var factor = matrix[k][i];
                    for (int j = 0; j < n + 1; j++) {
                        matrix[k][j] = matrix[k][j].subtract(matrix[i][j].multiply(factor, MC));
                    }
                }
            }
        }

        // Collect results (last column of the matrix)
        var result = new BigDecimal[n];
        for (int i = 0; i < n; i++) {
            result[i] = matrix[i][n];
        }
        return result;
    }

    /**
     * Returns if the given {@link BigDecimal} value is (most likely) zero, in spite of potentially inaccurate
     * calculations.
     */
    private static boolean isZero(BigDecimal a) {
        return a.abs().compareTo(EPS) <= 0;
    }

    private record Hailstone(Vector p, Vector v) {
        static Hailstone fromString(String line) {
            long[] c = parseLongs(line);
            return new Hailstone(new Vector(c[0], c[1], c[2]), new Vector(c[3], c[4], c[5]));
        }
    }

}
