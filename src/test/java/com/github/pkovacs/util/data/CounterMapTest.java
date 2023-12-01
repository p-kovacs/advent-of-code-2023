package com.github.pkovacs.util.data;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CounterMapTest {

    @Test
    void test() {
        var map = new CounterMap<String>();

        assertNull(map.get("hello"));
        assertNull(map.get("okay"));
        assertEquals(0, map.getValue("hello"));
        assertEquals(0, map.getValue("okay"));
        assertEquals(0, map.size());
        assertThrows(NoSuchElementException.class, map::min);
        assertThrows(NoSuchElementException.class, map::max);

        assertEquals(1, map.inc("hello"));
        assertEquals(-1, map.dec("okay"));
        assertEquals(2, map.size());

        assertNull(map.get("extra"));
        assertEquals(0, map.getValue("extra"));
        assertEquals(0, map.multiply("extra", 42));
        assertEquals(0, map.get("extra"));
        assertEquals(0, map.getValue("extra"));
        assertEquals(3, map.size());

        map.put("hello", 42);
        assertEquals(42, map.get("hello"));
        assertEquals(32, map.add("hello", -10));

        map.put("okay", 11);
        assertEquals(10, map.dec("okay"));
        assertEquals(120, map.multiply("okay", 12));
        assertEquals(40, map.divide("okay", 3));
        assertEquals(41, map.inc("okay"));
        assertEquals(42, map.inc("okay"));

        assertEquals(32, map.getValue("hello"));
        assertEquals(42, map.getValue("okay"));
        assertEquals(0, map.min());
        assertEquals(42, map.max());
        assertEquals(74, map.sum());

        assertEquals(1, map.count(42));
        assertEquals(1, map.count(32));
        assertEquals(0, map.count(22));
        assertEquals(1, map.count(0));

        assertEquals(2, map.valueStream().filter(v -> v < 42).count());
    }

}
