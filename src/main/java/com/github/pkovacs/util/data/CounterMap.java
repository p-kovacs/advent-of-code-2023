package com.github.pkovacs.util.data;

import java.util.HashMap;
import java.util.function.UnaryOperator;
import java.util.stream.LongStream;

/**
 * A long-valued {@link HashMap} storing counters associated with keys. This class provides additional methods to
 * operate on the counter values conveniently, assuming the default initial value of each counter to be zero.
 * <p>
 * For example, if you call {@link #inc(Object)} on an empty map, it associates {@code 1} with the given key.
 * However, all standard methods of {@link java.util.Map} work identically, so {@link #get(Object)} returns
 * {@code null} for a key that is not contained in the map explicitly. Use {@link #getValue(Object)} when you
 * would like to exploit the default value.
 */
public class CounterMap<K> extends HashMap<K, Long> {

    public CounterMap() {
        super();
    }

    public CounterMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Returns the value associated with the given key or zero if the map does not contain the key.
     * This is equivalent to {@link #getOrDefault(Object, Long) getOrDefault(key, 0L)}.
     */
    public long getValue(K key) {
        return getOrDefault(key, 0L);
    }

    /**
     * Associates the given value with the given key. This method uses the standard {@link #put(Object, Long)}
     * method, but it can also be called with {@code int} parameters directly.
     */
    public void put(K key, long value) {
        put(key, Long.valueOf(value));
    }

    /**
     * Increments the value associated with the given key by one and returns the new value.
     * If the map did not contain the key, the old value is assumed to be zero.
     */
    public long inc(K key) {
        return update(key, v -> v + 1);
    }

    /**
     * Decrements the value associated with the given key by one and returns the new value.
     * If the map did not contain the key, the old value is assumed to be zero.
     */
    public long dec(K key) {
        return update(key, v -> v - 1);
    }

    /**
     * Adds {@code delta} to the value associated with the given key and returns the new value.
     * If the map did not contain the key, the old value is assumed to be zero.
     */
    public long add(K key, long delta) {
        return update(key, v -> v + delta);
    }

    /**
     * Multiplies the value associated with the given key by the given factor and returns the new value.
     * If the map did not contain the key, the old value is assumed to be zero.
     */
    public long multiply(K key, long factor) {
        return update(key, v -> v * factor);
    }

    /**
     * Divides the value associated with the given key by the given factor and returns the new value.
     * If the map did not contain the key, the old value is assumed to be zero.
     */
    public long divide(K key, long factor) {
        return update(key, v -> v / factor);
    }

    /**
     * Updates the value associated with the given key by applying the given function to the current value.
     * If the map did not contain the key, the old value is assumed to be zero.
     *
     * @return the new value associated with the key after the update
     */
    public long update(K key, UnaryOperator<Long> function) {
        long newValue = function.apply(getValue(key));
        put(key, newValue);
        return newValue;
    }

    /**
     * Returns the minimum of the values contained in this map explicitly.
     *
     * @throws java.util.NoSuchElementException if the map is empty
     */
    public long min() {
        return valueStream().min().orElseThrow();
    }

    /**
     * Returns the maximum of the values contained in this map explicitly.
     *
     * @throws java.util.NoSuchElementException if the map is empty
     */
    public long max() {
        return valueStream().max().orElseThrow();
    }

    /**
     * Returns the sum of the values stored in this map explicitly.
     */
    public long sum() {
        return valueStream().sum();
    }

    /**
     * Returns the count of the given value among all values contained in this map explicitly.
     */
    public long count(long value) {
        return valueStream().filter(v -> v == value).count();
    }

    /**
     * Returns a {@link LongStream} view of the values contained in this map explicitly.
     */
    public LongStream valueStream() {
        return values().stream().mapToLong(Long::longValue);
    }

}
