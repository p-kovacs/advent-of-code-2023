package com.github.pkovacs.util.data;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharTableTest extends AbstractTableTest<Character> {

    @Test
    void testConstructors() {
        var t1 = new CharTable(3, 2, 'a');

        var t2 = new CharTable(t1);
        t2.set(1, 1, '@');

        assertEquals('a', t1.get(1, 1));
        assertEquals('@', t2.get(1, 1));

        var t3 = new CharTable(t2.asArray());
        t3.set(0, 0, 'x');
        t2.set(0, 1, 'y');

        assertEquals('a', t1.get(0, 0));
        assertEquals('x', t2.get(0, 0));
        assertEquals('x', t3.get(0, 0));
        assertEquals('a', t1.get(0, 1));
        assertEquals('y', t2.get(0, 1));
        assertEquals('y', t3.get(0, 1));
    }

    @Test
    void testWrapMethods() {
        var cells = List.of(new Cell(12, 12), new Cell(13, 13), new Cell(11, 11), new Cell(14, 11));
        assertEquals(new CharTable(List.of("#  ", " # ", "  #", "#  ")), CharTable.wrap(cells, '#', ' '));

        var table1 = new CharTable(List.of("123", "abc", "def", "xyz"));
        var table2 = new CharTable(List.of(".2.", "a.c", ".e.", "x.z"));
        var map = table1.cells().filter(c -> (c.row() + c.col()) % 2 == 1)
                .collect(Collectors.toMap(c -> c, table1::get));
        assertEquals(table2, CharTable.wrap(map, '.'));
    }

    @Test
    void testGettersAndSetters() {
        var table = new CharTable(3, 4, ' ');

        assertEquals(3, table.rowCount());
        assertEquals(4, table.colCount());

        assertContentEquals(List.of("    ", "    ", "    "), table);

        var table2 = createTestTable(3, 4);
        table.cells().forEach(c -> table.set(c, table2.get(c))); // deliberately copied this way to test get-set

        assertContentEquals(List.of("0123", "abcd", "ABCD"), table);

        table.cells().forEach(cell -> table.update(cell, c -> (char) (c + 1)));
        table.set(0, 0, '#');
        table.set(new Cell(2, 2), '@');

        assertContentEquals(List.of("#234", "bcde", "BC@E"), table);
        assertEquals(1, table.count('@'));
        assertEquals(0, table.count('x'));

        table.fill('x');

        assertContentEquals(List.of("xxxx", "xxxx", "xxxx"), table);
        assertEquals(0, table.count('@'));
        assertEquals(12, table.count('x'));

        assertContentEquals(List.of("123", "abc", "def", "xyz"), new CharTable(List.of("123", "abc", "def", "xyz")));
    }

    @Test
    void testWrappedMatrix() {
        var matrix = new char[][] { { '0', '1', '2', '3' }, { 'a', 'b', 'c', 'd' }, { 'A', 'B', 'C', 'D' } };
        var table = new CharTable(matrix);

        assertContentEquals(List.of("0123", "abcd", "ABCD"), table);

        matrix[0][0] = '#';
        table.set(2, 2, '@');

        assertContentEquals(List.of("#123", "abcd", "AB@D"), table);

        assertThrows(IllegalArgumentException.class,
                () -> new CharTable(new char[][] { { '0', '1' }, { 'a', 'b' }, { 'A', 'B', 'C' } }));
    }

    @Test
    void testStreamMethods() {
        var table = createTestTable(3, 4);

        assertEquals(table.values().toList(), table.cells().map(table::get).toList());
        assertEquals(table.rowValues(1).toList(), table.row(1).map(table::get).toList());
        assertEquals(table.colValues(2).toList(), table.col(2).map(table::get).toList());
    }

    @Test
    void testRays() {
        var cell = new Cell(3, 5);

        var rookTable = new CharTable(8, 8, '.');
        rookTable.set(cell, 'R');
        List<Stream<Cell>> cells = rookTable.neighbors(cell)
                .map(other -> rookTable.ray(cell, other))
                .toList();
        for (int i = 0; i < cells.size(); i++) {
            char ch = (char) ('1' + i);
            cells.get(i).forEach(c -> rookTable.set(c, ch));
        }

        assertContentEquals(List.of(".....1..", ".....1..", ".....1..", "22222R33",
                ".....4..", ".....4..", ".....4..", ".....4.."), rookTable);

        var queenTable = new CharTable(8, 8, '.');
        queenTable.set(cell, 'Q');
        cells = queenTable.extendedNeighbors(cell)
                .map(other -> queenTable.ray(cell, other))
                .toList();
        for (int i = 0; i < cells.size(); i++) {
            char ch = (char) ('1' + i);
            cells.get(i).forEach(c -> queenTable.set(c, ch));
        }

        assertContentEquals(List.of("..1..2..", "...1.2.3", "....123.", "44444Q55",
                "....678.", "...6.7.8", "..6..7..", ".6...7.."), queenTable);
    }

    @Test
    void testFindMethods() {
        var table = new CharTable(List.of("123", "abc", "123", "xyz"));

        assertEquals(new Cell(1, 2), table.find('c'));
        assertEquals(new Cell(0, 1), table.find('2'));
        assertThrows(NoSuchElementException.class, () -> table.find('X'));

        assertEquals(List.of(new Cell(1, 2)), table.findAll('c').toList());
        assertEquals(List.of(new Cell(0, 1), new Cell(2, 1)), table.findAll('2').toList());
        assertEquals(List.of(), table.findAll('X').toList());
    }

    @Test
    void testToString() {
        var table = createTestTable(3, 4);

        assertEquals("0123\nabcd\nABCD\n", table.toString());
    }

    private static void assertContentEquals(List<String> expected, CharTable table) {
        var array = new char[expected.size()][];
        for (int i = 0; i < array.length; i++) {
            array[i] = expected.get(i).toCharArray();
        }
        assertTrue(Arrays.deepEquals(array, table.asArray()));
    }

    @Override
    CharTable createTestTable(int rowCount, int colCount) {
        Function<Integer, Character> baseChar = i -> switch (i) {
            case 0 -> '0';
            case 1 -> 'a';
            default -> 'A';
        };
        return new CharTable(rowCount, colCount, (r, c) -> (char) (baseChar.apply(r % 3) + c));
    }

}
