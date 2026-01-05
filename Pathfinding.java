package graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Pathfinding {

    /**
     * Represents a path ending at `lastEdge.end()` along with its total weight (distance).
     */
    record PathEnd<E extends Edge<?>>(double distance, E lastEdge) {

    }

    /**
     * Returns a list of `E` edges comprising the shortest non-backtracking path from vertex `src`
     * to vertex `dst`. A non-backtracking path never contains two consecutive edges between the
     * same two vertices (e.g., v -> w -> v). As a part of this requirement, the first edge in the
     * returned path cannot back-track `previousEdge` (when `previousEdge` is not null). If there is
     * not a non-backtracking path from `src` to `dst`, then null is returned. Requires that if `E
     * != null` then `previousEdge.dst().equals(src)`.
     */
    public static <V extends Vertex<E>, E extends Edge<V>> List<E> shortestNonBacktrackingPath(
            V src, V dst, E previousEdge) {

        Map<V, PathEnd<E>> paths = pathInfo(src, previousEdge);
        return paths.containsKey(dst) ? pathTo(paths, src, dst) : null;
    }

    /**
     * Returns a map that associates each vertex reachable from `src` along a non-backtracking path
     * with a `PathEnd` object. The `PathEnd` object summarizes relevant information about the
     * shortest non-backtracking path from `src` to that vertex. A non-backtracking path never
     * contains two consecutive edges between the same two vertices (e.g., v -> w -> v). As a part
     * of this requirement, the first edge in the returned path cannot backtrack `previousEdge`
     * (when `previousEdge` is not null). Requires that if `E != null` then
     * `previousEdge.dst().equals(src)`.
     */
    static <V extends Vertex<E>, E extends Edge<V>> Map<V, PathEnd<E>> pathInfo(V src,
            E previousEdge) {

        assert previousEdge == null || previousEdge.dst().equals(src);

        // Associate vertex labels with info about the shortest-known path from `start` to that
        // vertex.  Populated as vertices are discovered (not as they are settled).
        Map<V, PathEnd<E>> pathInfo = new HashMap<>();

        MinPQueue<V> frontier = new MinPQueue<>();
        pathInfo.put(src, new PathEnd<>(0, null));
        frontier.addOrUpdate(src, 0);

        //Extracts closest unexplored vertex
        while (!frontier.isEmpty()) {
            V v = frontier.remove();
            double vDistance = pathInfo.get(v).distance();
            E vLastEdge = pathInfo.get(v).lastEdge();
            //Explores each outgoing edge from vertex
            for (E e : v.outgoingEdges()) {
                V u = e.dst();

                //Avoids reversing direction through edge used to enter src
                if (v.equals(src) && previousEdge != null && e.dst().equals(previousEdge.src())) {
                    continue;
                }
                //Avoids reversing direction along the previous edge
                if (vLastEdge != null && vLastEdge.src().equals(u)) {
                    continue;
                }
                double uDistance = vDistance + e.weight();

                //Updates if u is not visited or if path is shorter than previously found
                if (!pathInfo.containsKey(u) || uDistance < pathInfo.get(u).distance()) {
                    pathInfo.put(u, new PathEnd<>(uDistance, e));
                    frontier.addOrUpdate(u, uDistance);
                }
            }
        }
        return pathInfo;
    }

    /**
     * Return the list of edges in the shortest non-backtracking path from `src` to `dst`, as
     * summarized by the given `pathInfo` map. Requires `pathInfo` conforms to the specification as
     * documented by the `pathInfo` method; it must contain backpointers for the shortest
     * non-backtracking paths from `src` to all reachable vertices.
     */
    static <V, E extends Edge<V>> List<E> pathTo(Map<V, PathEnd<E>> pathInfo, V src, V dst) {
        // Prefer a linked list for efficient prepend (alternatively, could append, then reverse
        // before returning)
        List<E> path = new LinkedList<>();

        //Start from destination and trace backward through each edge to the source
        V current = dst;
        while (!current.equals(src)) {
            PathEnd<E> pathEnd = pathInfo.get(current);
            E lastEdge = pathEnd.lastEdge();
            path.addFirst(lastEdge);
            current = lastEdge.src();
        }
        return path;


    }
}
