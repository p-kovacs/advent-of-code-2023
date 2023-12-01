package com.github.pkovacs.util.alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result object for path search algorithms.
 *
 * @see Bfs
 * @see Dijkstra
 * @see BellmanFord
 */
public final class Path<T> {

    private final T endNode;
    private final long dist;
    private final Path<T> prev;

    private List<T> nodes;

    Path(T endNode, long dist, Path<T> prev) {
        this.endNode = endNode;
        this.dist = dist;
        this.prev = prev;
    }

    /**
     * Returns the end node of the path.
     */
    public T endNode() {
        return endNode;
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
     * is the end node of the path.
     */
    public List<T> nodes() {
        if (nodes == null) {
            // Lazy load: construct path
            var list = new ArrayList<T>();
            for (var e = this; e != null; e = e.prev) {
                list.add(e.endNode);
            }
            Collections.reverse(list);
            nodes = Collections.unmodifiableList(list);
        }
        return nodes;
    }

}
