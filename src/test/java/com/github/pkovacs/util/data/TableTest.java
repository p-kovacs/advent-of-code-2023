package com.github.pkovacs.util.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableTest extends AbstractTableTest<String> {

    @Test
    void testConstructors() {
        var table1 = createTestTable(2, 3);
        var table2 = new Table<>(table1);

        assertEquals(table1, table2);

        var table3 = new Table<>(new String[][] { { "A1", "A2", "A3" }, { "B1", "B2", "B3" } });

        assertEquals(table1, table3);

        var table4 = new Table<>(2, 3, this::getTestValue);

        assertEquals(table1, table4);
    }

    @Test
    void testGettersAndSetters() {
        var table = new Table<String>(3, 4);

        assertEquals(3, table.rowCount());
        assertEquals(4, table.colCount());

        assertContentEquals(List.of(
                Arrays.asList(new String[4]),
                Arrays.asList(new String[4]),
                Arrays.asList(new String[4])), table);

        table.cells().forEach(c -> table.set(c, String.valueOf((char) ('A' + c.row())) + (c.col() + 1)));

        assertContentEquals(List.of(
                List.of("A1", "A2", "A3", "A4"),
                List.of("B1", "B2", "B3", "B4"),
                List.of("C1", "C2", "C3", "C4")), table);

        table.cells().forEach(c -> table.update(c, x -> x + "!"));
        table.set(0, 0, "xyz");
        table.set(new Cell(2, 2), "abc");

        assertContentEquals(List.of(
                List.of("xyz", "A2!", "A3!", "A4!"),
                List.of("B1!", "B2!", "B3!", "B4!"),
                List.of("C1!", "C2!", "abc", "C4!")), table);

        table.fill("A");

        assertContentEquals(List.of(
                List.of("A", "A", "A", "A"),
                List.of("A", "A", "A", "A"),
                List.of("A", "A", "A", "A")), table);
    }

    @Test
    void testStreamMethods() {
        var table = createTestTable(3, 4);

        assertEquals(table.values().toList(), table.cells().map(table::get).toList());
        assertEquals(table.rowValues(1).toList(), table.row(1).map(table::get).toList());
        assertEquals(table.colValues(2).toList(), table.col(2).map(table::get).toList());
    }

    @Test
    void testToString() {
        var table = createTestTable(3, 4);

        assertEquals("A1 A2 A3 A4\nB1 B2 B3 B4\nC1 C2 C3 C4\n", table.toString());
    }

    private static void assertContentEquals(List<List<String>> expected, Table<String> table) {
        var actual = IntStream.range(0, table.rowCount())
                .mapToObj(table::rowValues)
                .map(Stream::toList)
                .toList();
        assertEquals(expected, actual);
    }

    @Override
    Table<String> createTestTable(int rowCount, int colCount) {
        return new Table<>(rowCount, colCount, this::getTestValue);
    }

    private String getTestValue(int row, int col) {
        return String.valueOf((char) ('A' + row)) + (col + 1);
    }

}
