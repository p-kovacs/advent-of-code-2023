package com.github.pkovacs.util.data;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PointTest {

    @Test
    void testBasicMethods() {
        var a = new Point(42, 12);
        var b = new Point(12, 42);
        var c = new Point(12, 42);

        assertEquals(42, a.x());
        assertEquals(12, a.y());
        assertNotEquals(a, b);
        assertEquals(b, c);

        assertTrue(a.isValid(43, 13));
        assertFalse(a.isValid(43, 12));
        assertFalse(a.isValid(42, 13));

        assertEquals(new Point(30, 20), a.add(-12, 8));
        assertEquals(new Point(24, 84), b.add(c.x(), c.y()));

        assertEquals("(12, 42)", new Point(12, 42).toString());
        assertEquals("(-3, -5)", new Point(-3, -5).toString());
    }

    @Test
    void testNeighborMethods() {
        var a = new Point(42, 12);

        assertEquals(new Point(42, 11), a.neighbor(Direction.NORTH));
        assertEquals(new Point(43, 12), a.neighbor(Direction.EAST));
        assertEquals(new Point(42, 13), a.neighbor(Direction.SOUTH));
        assertEquals(new Point(41, 12), a.neighbor(Direction.WEST));

        assertEquals(new Point(42, 11), a.neighbor('n'));
        assertEquals(new Point(43, 12), a.neighbor('E'));
        assertEquals(new Point(42, 13), a.neighbor('s'));
        assertEquals(new Point(41, 12), a.neighbor('W'));

        assertEquals(new Point(42, 11), a.neighbor('u'));
        assertEquals(new Point(43, 12), a.neighbor('R'));
        assertEquals(new Point(42, 13), a.neighbor('d'));
        assertEquals(new Point(41, 12), a.neighbor('L'));

        assertEquals(new Point(42, 13), a.neighborWithUpwardY(Direction.NORTH));
        assertEquals(new Point(43, 12), a.neighborWithUpwardY(Direction.EAST));
        assertEquals(new Point(42, 11), a.neighborWithUpwardY(Direction.SOUTH));
        assertEquals(new Point(41, 12), a.neighborWithUpwardY(Direction.WEST));

        assertEquals(new Point(42, 13), a.neighborWithUpwardY('n'));
        assertEquals(new Point(43, 12), a.neighborWithUpwardY('E'));
        assertEquals(new Point(42, 11), a.neighborWithUpwardY('s'));
        assertEquals(new Point(41, 12), a.neighborWithUpwardY('W'));

        assertEquals(new Point(42, 13), a.neighborWithUpwardY('u'));
        assertEquals(new Point(43, 12), a.neighborWithUpwardY('R'));
        assertEquals(new Point(42, 11), a.neighborWithUpwardY('d'));
        assertEquals(new Point(41, 12), a.neighborWithUpwardY('L'));

        assertEquals(List.of(
                        new Point(41, 12),
                        new Point(42, 11),
                        new Point(42, 13),
                        new Point(43, 12)),
                a.neighbors().toList());
        assertEquals(List.of(
                        new Point(41, 12),
                        new Point(42, 11),
                        new Point(42, 12),
                        new Point(42, 13),
                        new Point(43, 12)),
                a.neighborsAndSelf().toList());
        assertEquals(a.neighbors().sorted().toList(), a.neighbors().toList());
        assertEquals(a.neighborsAndSelf().sorted().toList(), a.neighborsAndSelf().toList());

        assertEquals(List.of(
                        new Point(41, 11),
                        new Point(41, 12),
                        new Point(41, 13),
                        new Point(42, 11),
                        new Point(42, 13),
                        new Point(43, 11),
                        new Point(43, 12),
                        new Point(43, 13)),
                a.extendedNeighbors().toList());
        assertEquals(List.of(
                        new Point(41, 11),
                        new Point(41, 12),
                        new Point(41, 13),
                        new Point(42, 11),
                        new Point(42, 12),
                        new Point(42, 13),
                        new Point(43, 11),
                        new Point(43, 12),
                        new Point(43, 13)),
                a.extendedNeighborsAndSelf().toList());
        assertEquals(a.extendedNeighbors().sorted().toList(), a.extendedNeighbors().toList());
        assertEquals(a.extendedNeighborsAndSelf().sorted().toList(), a.extendedNeighborsAndSelf().toList());

        assertTrue(a.neighbors().allMatch(a::isNeighbor));
        assertTrue(a.neighbors().allMatch(a::isExtendedNeighbor));
        assertEquals(4, a.extendedNeighbors().filter(a::isNeighbor).count());

        assertTrue(a.neighbors().mapToInt(a::dist1).allMatch(d -> d == 1));
        assertTrue(a.neighbors().mapToInt(a::distMax).allMatch(d -> d == 1));

        assertTrue(a.extendedNeighbors().mapToInt(a::dist1).allMatch(d -> d <= 2));
        assertTrue(a.extendedNeighbors().mapToInt(a::distMax).allMatch(d -> d == 1));
        assertEquals(12, a.extendedNeighbors().mapToInt(a::dist1).sum());
        assertEquals(8, a.extendedNeighbors().mapToInt(a::distMax).sum());
    }

    @Test
    void testVectorMethods() {
        var a = new Point(42, 12);
        var b = new Point(10, 20);

        assertEquals(new Point(-42, -12), a.opposite());
        assertEquals(new Point(52, 32), a.add(b));
        assertEquals(new Point(52, 32), b.add(a));
        assertEquals(new Point(32, -8), a.subtract(b));
        assertEquals(new Point(-32, 8), b.subtract(a));
        assertEquals(a.add(b.opposite()), a.subtract(b));
        assertEquals(b.add(a.opposite()), b.subtract(a));
    }

    @Test
    void testTransformations() {
        var a = new Point(40, 10);

        assertEquals(new Point(-40, -10), a.opposite());

        assertEquals(new Point(-10, 40), a.rotateLeft());
        assertEquals(new Point(-40, -10), a.rotateLeft().rotateLeft());
        assertEquals(new Point(10, -40), a.rotateLeft().rotateLeft().rotateLeft());
        assertEquals(new Point(40, 10), a.rotateLeft().rotateLeft().rotateLeft().rotateLeft());

        assertEquals(new Point(10, -40), a.rotateRight());
        assertEquals(new Point(-40, -10), a.rotateRight().rotateRight());
        assertEquals(new Point(-10, 40), a.rotateRight().rotateRight().rotateRight());
        assertEquals(new Point(40, 10), a.rotateRight().rotateRight().rotateRight().rotateRight());

        assertEquals(new Point(-40, 10), a.mirrorHorizontally());
        assertEquals(new Point(40, -10), a.mirrorVertically());
        assertEquals(a, a.mirrorHorizontally().mirrorHorizontally());
        assertEquals(a, a.mirrorVertically().mirrorVertically());
        assertEquals(a.opposite(), a.mirrorHorizontally().mirrorVertically());
        assertEquals(a.opposite(), a.mirrorVertically().mirrorHorizontally());
    }

    @Test
    void testLines() {
        assertEquals(List.of(new Point(10, 42), new Point(11, 42), new Point(12, 42)),
                new Point(10, 42).lineTo(new Point(12, 42)).toList());
        assertEquals(List.of(new Point(12, 40), new Point(12, 41), new Point(12, 42)),
                new Point(12, 40).lineTo(new Point(12, 42)).toList());
        assertEquals(List.of(new Point(12, 42), new Point(11, 42), new Point(10, 42), new Point(9, 42)),
                new Point(12, 42).lineTo(new Point(9, 42)).toList());
        assertEquals(List.of(new Point(12, 42), new Point(13, 43), new Point(14, 44)),
                new Point(12, 42).lineTo(new Point(14, 44)).toList());
        assertEquals(List.of(new Point(12, 42), new Point(11, 43), new Point(10, 44)),
                new Point(12, 42).lineTo(new Point(10, 44)).toList());
        assertEquals(List.of(new Point(12, 42)),
                new Point(12, 42).lineTo(new Point(12, 42)).toList());

        assertThrows(IllegalArgumentException.class, () -> new Point(12, 42).lineTo(new Point(10, 45)));
    }

    @Test
    void testRays() {
        var a = new Point(12, 42);

        assertEquals(List.of(new Point(12, 41), new Point(12, 40), new Point(12, 39)),
                a.ray(a.neighbor(Direction.NORTH)).limit(3).toList());
        assertEquals(List.of(new Point(13, 42), new Point(14, 42), new Point(15, 42)),
                a.ray(a.neighbor(Direction.EAST)).limit(3).toList());
        assertEquals(List.of(new Point(12, 43), new Point(12, 44), new Point(12, 45)),
                a.ray(a.neighbor(Direction.SOUTH)).limit(3).toList());
        assertEquals(List.of(new Point(11, 42), new Point(10, 42), new Point(9, 42)),
                a.ray(a.neighbor(Direction.WEST)).limit(3).toList());

        assertEquals(List.of(new Point(11, 41), new Point(10, 40), new Point(9, 39)),
                a.ray(new Point(11, 41)).limit(3).toList());
        assertEquals(List.of(new Point(16, 46), new Point(17, 47), new Point(18, 48)),
                a.ray(new Point(13, 43)).skip(3).limit(3).toList());
        assertEquals(List.of(new Point(10, 52), new Point(8, 62), new Point(6, 72)),
                a.ray(new Point(10, 52)).limit(3).toList());
    }

    @Test
    void testDistanceMethods() {
        var a = new Point(42, 12);
        var b = new Point(30, 30);

        assertEquals(54, a.dist1());
        assertEquals(30, a.dist1(b));
        assertEquals(0, a.dist1(a));

        assertEquals(42, a.distMax());
        assertEquals(18, a.distMax(b));
        assertEquals(0, a.distMax(a));

        assertEquals(12 * 12 + 42 * 42, a.distSq());
        assertEquals(12 * 12 + 18 * 18, a.distSq(b));
        assertEquals(0, a.distSq(a));

        assertEquals(Math.sqrt(12 * 12 + 42 * 42), a.dist2(), 1e-10);
        assertEquals(Math.sqrt(12 * 12 + 18 * 18), a.dist2(b), 1e-10);
        assertEquals(0, a.dist2(a), 1e-10);
    }

    @Test
    void testRangeMethods() {
        var list = List.of(new Point(20, 30), new Point(10, 50), new Point(15, 25));
        assertEquals(new Range(10, 20), Point.xRange(list));
        assertEquals(new Range(25, 50), Point.yRange(list));
        assertThrows(NoSuchElementException.class, () -> Point.xRange(List.of()));
        assertThrows(NoSuchElementException.class, () -> Point.yRange(List.of()));
    }

    @Test
    void testBoxMethods() {
        assertEquals(List.of(), Point.box(0, 3).toList());
        assertEquals(List.of(), Point.box(1, -1).toList());
        assertEquals(List.of(Point.ORIGIN), Point.box(1, 1).toList());
        assertEquals(List.of(
                        new Point(0, 0), new Point(0, 1), new Point(0, 2),
                        new Point(1, 0), new Point(1, 1), new Point(1, 2)),
                Point.box(2, 3).toList());

        var list1 = List.of(
                new Point(40, 20), new Point(41, 20), new Point(42, 20));
        var list2 = List.of(
                new Point(42, 10), new Point(42, 11),
                new Point(43, 10), new Point(43, 11),
                new Point(44, 10), new Point(44, 11));

        assertEquals(List.of(),
                Point.box(new Point(40, 20), new Point(40, 19)).toList());
        assertEquals(list1,
                Point.box(new Point(40, 20), new Point(42, 20)).toList());
        assertEquals(list2, Point.box(new Point(42, 10), new Point(44, 11)).toList());

        assertEquals(List.of(), Point.box(new Point(42, 20), new Point(40, 20)).toList());
        assertEquals(list1, Point.boundingBox(new Point(42, 20), new Point(40, 20)).toList());
        assertEquals(list2, Point.boundingBox(List.of(new Point(44, 10), new Point(42, 11))).toList());
        assertEquals(list2, Point.boundingBox(new Point(44, 10), new Point(43, 11), new Point(42, 10)).toList());
    }

}
