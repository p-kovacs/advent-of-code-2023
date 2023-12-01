package com.github.pkovacs.util.data;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorTest {

    @Test
    void testBasicMethodsTwoDim() {
        var a = Vector.origin(2);
        var b = new Vector(42, 12);

        assertEquals(b.x(), 42);
        assertEquals(b.get(0), 42);
        assertEquals(b.y(), 12);
        assertEquals(b.get(1), 12);
        assertThrows(IndexOutOfBoundsException.class, b::z);
        assertThrows(IndexOutOfBoundsException.class, () -> b.get(2));

        assertEquals(b, a.add(b));
        assertEquals(new Vector(44, 15), b.add(2, 3));
        assertEquals(new Vector(-1, 12), b.set(0, -1));
        assertEquals(new Vector(42, 100), b.set(1, 100));
        assertThrows(IndexOutOfBoundsException.class, () -> b.set(2, 0));

        a = a.add(b).subtract(new Vector(2, 2));
        assertEquals(new Vector(40, 10), a);
        assertEquals(50, a.dist1());
        assertEquals(new Vector(-40, -10), a.opposite());
        assertEquals(50, a.opposite().dist1());

        var e = new Vector(42, 12);
        assertEquals(new Vector(0, 0), e.multiply(0));
        assertEquals(e, e.multiply(1));
        assertEquals(e.add(e), e.multiply(2));
        assertEquals(e.add(e).add(e).add(e).add(e), e.multiply(5));

        assertEquals("(42, 12)", new Vector(42, 12).toString());
        assertEquals("(42, -12)", new Vector(42, -12).toString());
    }

    @Test
    void testDistanceMethodsTwoDim() {
        var a = new Vector(42, 12);
        var b = new Vector(12, -42);

        assertEquals(54 + 30, a.dist1(b));
        assertEquals(54, a.distMax(b));
        assertEquals(54 * 54 + 30 * 30, a.distSq(b));
        assertEquals(Math.sqrt(54 * 54 + 30 * 30), a.dist2(b), 1e-10);
        a = new Vector(-12, 42);
        assertEquals(a.dist1() + b.dist1(), a.dist1(b));
        assertEquals(a.distMax() + b.distMax(), a.distMax(b));
        assertNotEquals(a.distSq() + b.distSq(), a.distSq(b)); // does not satisfy the triangle inequality
        assertEquals(a.dist2() + b.dist2(), a.dist2(b), 1e-10);
        a = a.opposite();
        assertEquals(0, a.dist1(b));
        assertEquals(0, a.distMax(b));
        assertEquals(0, a.distSq(b));
        assertEquals(0, a.dist2(b), 1e-10);
    }

    @Test
    void testNeighborMethodsTwoDim() {
        var a = new Vector(42, 12);

        assertEquals(List.of(
                        new Vector(41, 12),
                        new Vector(42, 11),
                        new Vector(42, 12),
                        new Vector(42, 13),
                        new Vector(43, 12)),
                a.neighborsAndSelf().toList());
        assertEquals(4, a.neighbors().count());
        assertEquals(5, a.neighborsAndSelf().count());
        assertTrue(a.neighbors().allMatch(v -> v.dist1(a) == 1));
        assertTrue(a.neighborsAndSelf().allMatch(v -> v.dist1(a) <= 1));
        assertTrue(a.neighborsAndSelf().allMatch(v -> v.dist1(a) == 1 || v == a));
        assertEquals(a.neighbors().sorted().toList(), a.neighbors().toList());
        assertEquals(a.neighborsAndSelf().sorted().toList(), a.neighborsAndSelf().toList());

        assertEquals(List.of(
                        new Vector(41, 11),
                        new Vector(41, 12),
                        new Vector(41, 13),
                        new Vector(42, 11),
                        new Vector(42, 12),
                        new Vector(42, 13),
                        new Vector(43, 11),
                        new Vector(43, 12),
                        new Vector(43, 13)),
                a.extendedNeighborsAndSelf().toList());
        assertEquals(8, a.extendedNeighbors().count());
        assertEquals(9, a.extendedNeighborsAndSelf().count());
        assertTrue(a.extendedNeighbors().allMatch(v -> v.distMax(a) == 1));
        assertTrue(a.extendedNeighborsAndSelf().allMatch(v -> v.distMax(a) <= 1));
        assertTrue(a.extendedNeighborsAndSelf().allMatch(v -> v.distMax(a) == 1 || v == a));
        assertEquals(a.extendedNeighbors().sorted().toList(), a.extendedNeighbors().toList());
        assertEquals(a.extendedNeighborsAndSelf().sorted().toList(), a.extendedNeighborsAndSelf().toList());
    }

    @Test
    void testBoxTwoDim() {
        assertEquals(List.of(),
                Vector.box(new Vector(10, 20), new Vector(10, 10)).toList());
        assertEquals(List.of(new Vector(10, 20)),
                Vector.box(new Vector(10, 20), new Vector(10, 20)).toList());
        assertEquals(List.of(new Vector(10, 20), new Vector(11, 20)),
                Vector.box(new Vector(10, 20), new Vector(11, 20)).toList());
        assertEquals(List.of(new Vector(10, 20), new Vector(10, 21), new Vector(10, 22)),
                Vector.box(new Vector(10, 20), new Vector(10, 22)).toList());
        assertEquals(List.of(
                        new Vector(10, 20), new Vector(10, 21), new Vector(10, 22),
                        new Vector(11, 20), new Vector(11, 21), new Vector(11, 22),
                        new Vector(12, 20), new Vector(12, 21), new Vector(12, 22),
                        new Vector(13, 20), new Vector(13, 21), new Vector(13, 22)),
                Vector.box(new Vector(10, 20), new Vector(13, 22)).toList());
    }

    @Test
    void testBasicMethodsThreeDim() {
        var a = Vector.origin(3);
        var b = new Vector(42, 12, 314);

        assertEquals(3, a.dim());
        assertEquals(3, b.dim());

        assertEquals(b.x(), 42);
        assertEquals(b.y(), 12);
        assertEquals(b.z(), 314);

        assertEquals(b, a.add(b));
        assertEquals(new Vector(44, 15, 214), b.add(2, 3, -100));

        a = a.add(b).subtract(new Vector(2, 2, 14));
        assertEquals(new Vector(40, 10, 300), a);
        assertEquals(new Vector(-40, -10, -300), a.opposite());

        var c = new Vector(42, 12, -3);
        assertEquals(Vector.origin(c.dim()), c.multiply(0));
        assertEquals(c, c.multiply(1));
        assertEquals(c.add(c), c.multiply(2));
        assertEquals(c.add(c).add(c).add(c).add(c), c.multiply(5));
        assertEquals(c.add(c.multiply(7)).subtract(c.multiply(4)), c.multiply(4));

        assertEquals("(42, 12, -3)", c.toString());
    }

    @Test
    void testDistanceMethodsThreeDim() {
        var a = new Vector(42, 12, -3);

        assertEquals(42 + 12 + 3, a.dist1());
        assertEquals(42, a.distMax());
        assertEquals(42 * 42 + 12 * 12 + 3 * 3, a.distSq());
        assertEquals(Math.sqrt(42 * 42 + 12 * 12 + 3 * 3), a.dist2(), 1e-10);

        assertEquals(a.dist1(), a.opposite().dist1());
        assertEquals(a.distMax(), a.opposite().distMax());
        assertEquals(a.distSq(), a.opposite().distSq());
        assertEquals(a.dist2(), a.opposite().dist2(), 1e-10);

        assertEquals(a.dist1() * 5, a.opposite().dist1(a.multiply(4)));
        assertEquals(a.distMax() * 5, a.opposite().distMax(a.multiply(4)));
        assertNotEquals(a.distSq() * 5, a.opposite().distSq(a.multiply(4))); // does not satisfy the triangle inequality
        assertEquals(a.dist2() * 5, a.opposite().dist2(a.multiply(4)), 1e-10);
    }

    @Test
    void testNeighborMethodsThreeDim() {
        var a = new Vector(42, 12, 5);

        assertEquals(List.of(
                        new Vector(41, 12, 5),
                        new Vector(42, 11, 5),
                        new Vector(42, 12, 4),
                        new Vector(42, 12, 5),
                        new Vector(42, 12, 6),
                        new Vector(42, 13, 5),
                        new Vector(43, 12, 5)),
                a.neighborsAndSelf().toList());
        assertEquals(6, a.neighbors().count());
        assertEquals(7, a.neighborsAndSelf().count());
        assertTrue(a.neighbors().allMatch(v -> v.dist1(a) == 1));
        assertTrue(a.neighborsAndSelf().allMatch(v -> v.dist1(a) <= 1));
        assertTrue(a.neighborsAndSelf().allMatch(v -> v.dist1(a) == 1 || v == a));
        assertEquals(a.neighbors().sorted().toList(), a.neighbors().toList());
        assertEquals(a.neighborsAndSelf().sorted().toList(), a.neighborsAndSelf().toList());

        assertEquals(26, a.extendedNeighbors().count());
        assertEquals(27, a.extendedNeighborsAndSelf().count());
        assertTrue(a.extendedNeighbors().allMatch(v -> v.distMax(a) == 1));
        assertTrue(a.extendedNeighborsAndSelf().allMatch(v -> v.distMax(a) <= 1));
        assertTrue(a.extendedNeighborsAndSelf().allMatch(v -> v.distMax(a) == 1 || v == a));
        assertEquals(a.extendedNeighbors().sorted().toList(), a.extendedNeighbors().toList());
        assertEquals(a.extendedNeighborsAndSelf().sorted().toList(), a.extendedNeighborsAndSelf().toList());
    }

    @Test
    void testBoxThreeDim() {
        assertEquals(List.of(),
                Vector.box(new Vector(10, 20, 30), new Vector(10, 10, 40)).toList());
        assertEquals(List.of(new Vector(10, 20, 30)),
                Vector.box(new Vector(10, 20, 30), new Vector(10, 20, 30)).toList());
        assertEquals(List.of(new Vector(10, 20, 30), new Vector(11, 20, 30)),
                Vector.box(new Vector(10, 20, 30), new Vector(11, 20, 30)).toList());
        assertEquals(List.of(new Vector(10, 20, 30), new Vector(10, 21, 30)),
                Vector.box(new Vector(10, 20, 30), new Vector(10, 21, 30)).toList());
        assertEquals(List.of(
                        new Vector(10, 20, 30), new Vector(10, 20, 31),
                        new Vector(10, 20, 32), new Vector(10, 20, 33)),
                Vector.box(new Vector(10, 20, 30), new Vector(10, 20, 33)).toList());
        assertEquals(List.of(
                        new Vector(10, 20, 30), new Vector(10, 20, 31), new Vector(10, 20, 32),
                        new Vector(10, 21, 30), new Vector(10, 21, 31), new Vector(10, 21, 32),
                        new Vector(11, 20, 30), new Vector(11, 20, 31), new Vector(11, 20, 32),
                        new Vector(11, 21, 30), new Vector(11, 21, 31), new Vector(11, 21, 32),
                        new Vector(12, 20, 30), new Vector(12, 20, 31), new Vector(12, 20, 32),
                        new Vector(12, 21, 30), new Vector(12, 21, 31), new Vector(12, 21, 32),
                        new Vector(13, 20, 30), new Vector(13, 20, 31), new Vector(13, 20, 32),
                        new Vector(13, 21, 30), new Vector(13, 21, 31), new Vector(13, 21, 32)),
                Vector.box(new Vector(10, 20, 30), new Vector(13, 21, 32)).toList());
    }

    @Test
    void testGeneral() {
        var a = Vector.origin(10);
        var b = new Vector(1, -2, 3, -4, 5, -6, 7, -8, 9, -10);
        var c = new Vector(1, 2, 3, 4, 5, 6, 7, 8);

        assertEquals(10, a.dim());
        assertEquals(10, b.dim());

        assertEquals(0, a.dist1());
        assertEquals(0, a.distMax());
        assertEquals(0, a.distSq());
        assertEquals(0, a.dist2(), 1e-10);

        assertEquals(55, b.dist1());
        assertEquals(10, b.distMax());
        assertEquals(1 + 4 + 9 + 16 + 25 + 36 + 49 + 64 + 81 + 100, b.distSq());
        assertEquals(Math.sqrt(1 + 4 + 9 + 16 + 25 + 36 + 49 + 64 + 81 + 100), b.dist2(), 1e-10);

        assertThrows(IllegalArgumentException.class, () -> b.add(new long[] { 10, 20, 30, 40, 50 }));
        assertThrows(IllegalArgumentException.class, () -> b.add(c));
        assertThrows(IllegalArgumentException.class, () -> c.subtract(b));
        assertThrows(IllegalArgumentException.class, () -> b.dist1(c));
        assertThrows(IllegalArgumentException.class, () -> b.distMax(c));
        assertThrows(IllegalArgumentException.class, () -> c.distSq(b));
        assertThrows(IllegalArgumentException.class, () -> c.dist2(b));

        assertEquals(b, a.add(b));
        assertEquals(a, b.subtract(b));

        assertEquals("(1, -2, 3, -4, 5, -6, 7, -8, 9, -10)", b.toString());
    }

    @Test
    void testOrdering() {
        var list = List.of(
                new Vector(42, 12),
                new Vector(41, 12),
                new Vector(42, 11),
                new Vector(42, 13),
                new Vector(43, 12),
                new Vector(42, 12, 3),
                new Vector(42, 12, 2),
                new Vector(42, 12, 1),
                new Vector(5, 8, 1, 0),
                new Vector(5, 8, 1, -1),
                new Vector(5, 8, 1, 1),
                new Vector(5, 8, 0, 0));
        var sortedList = List.of(
                new Vector(41, 12),
                new Vector(42, 11),
                new Vector(42, 12),
                new Vector(42, 13),
                new Vector(43, 12),
                new Vector(42, 12, 1),
                new Vector(42, 12, 2),
                new Vector(42, 12, 3),
                new Vector(5, 8, 0, 0),
                new Vector(5, 8, 1, -1),
                new Vector(5, 8, 1, 0),
                new Vector(5, 8, 1, 1));

        assertEquals(sortedList, list.stream().sorted().toList());
    }

}
