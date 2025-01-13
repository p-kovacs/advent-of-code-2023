package com.github.pkovacs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of path search algorithms in a directed or undirected graph.
 *
 * @see Bfs
 * @see Dijkstra
 * @see BellmanFord
 */
public final class Path<T> {

    private final T end;
    private final long dist;
    private final Path<T> prev;

    private List<T> nodes;

    Path(T end, long dist, Path<T> prev) {
        this.end = end;
        this.dist = dist;
        this.prev = prev;
    }

    /**
     * Returns the end node of the path.
     */
    public T end() {
        return end;
    }

    /**
     * Returns the distance of the end node from the source node along the path. This is the sum of the edge weights
     * along the path or simply the number of edges along the path if they are not weighted.
     */
    public long dist() {
        return dist;
    }

    /**
     * Returns the list of nodes along the path. The first element of the list is the source node, and the last element
     * is the end node of the path. The returned list is constructed on demand and cached.
     */
    public List<T> nodes() {
        if (nodes == null) {
            // Lazy load: construct path
            var list = new ArrayList<T>();
            for (var e = this; e != null; e = e.prev) {
                list.add(e.end);
            }
            nodes = Collections.unmodifiableList(list.reversed());
        }
        return nodes;
    }

}
