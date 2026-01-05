package graph;

import graph.Pathfinding.PathEnd;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGenerator.Simple;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static graph.SimpleGraph.*;

/**
 * Uses the `SimpleGraph` class to verify the functionality of the `Pathfinding` class.
 */
public class PathfindingTest {


    /*
     * Text graph format ([weight] is optional):
     * Directed edge: startLabel -> endLabel [weight]
     * Undirected edge (so two directed edges in both directions): startLabel -- endLabel [weight]
     */

    // a small, strongly-connected graph consisting of three vertices and four directed edges
    public static final String graph1 = """
            A -> B 2
            A -- C 6
            B -> C 3
            """;

    @DisplayName("WHEN we compute the `pathInfo` from a vertex `v`, THEN it includes an correct "
            + "entry for each vertex `w` reachable along a non-backtracking path from `v`.")
    @Nested
    class pathInfoTest {

        // Recall that "strongly connected" describes a graph that includes a (directed) path from
        // any vertex to any other vertex
        @DisplayName("In a strongly connected graph with no `previousEdge`.")
        @Test
        void testStronglyConnectedNoPrevious() {
            SimpleGraph g = SimpleGraph.fromText(graph1);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");

            // compute paths from source vertex "A"
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(0, paths.get(va).distance());
            // since the shortest path A -> A is empty, we can't assert anything about its last edge
            assertEquals(2, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(5, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            // compute paths from source vertex "B"
            paths = Pathfinding.pathInfo(vb, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(9, paths.get(va).distance());
            assertEquals(g.getEdge(vc, va), paths.get(va).lastEdge());
            assertEquals(0, paths.get(vb).distance());
            assertEquals(3, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            // compute paths from source vertex "C"
            paths = Pathfinding.pathInfo(vc, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(6, paths.get(va).distance());
            assertEquals(g.getEdge(vc, va), paths.get(va).lastEdge());
            assertEquals(8, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(0, paths.get(vc).distance());
        }

        @DisplayName("In a graph that is *not* strongly connected and `pathInfo` is computed "
                + "starting from a vertex that cannot reach all other vertices.")
        @Test
        void testNotStronglyConnected() {
            SimpleGraph g = SimpleGraph.fromText("B -> A 2");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(1, paths.size()); // only va is reachable
            assertTrue(paths.containsKey(va));
            assertFalse(paths.containsKey(vb));
        }

        @DisplayName("In a strongly connected graph with a `previousEdge` that prevents some vertex"
                + "from being reached.")
        @Test
        void testStronglyConnectedPreviousStillReachable() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 1
                    B -> C 1
                    C -> A 1""");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");

            SimpleEdge previousEdge = g.getEdge(vc, va);
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, previousEdge);
            //Confirm that all 3 vertices are reachable and included in the result
            assertEquals(3, paths.size());
            //A is the source, so its distance is 0 and no edge was used to reach it
            assertEquals(0, paths.get(va).distance());
            //B is reachable from A via edge A -> B, distance 1
            assertEquals(1, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            //C is reachable via A -> B -> C, total distance 2
            assertEquals(2, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());


        }

        @DisplayName("In a graph where the shortest path with backtracking is shorter than the "
                + "shortest non-backtracking path.")
        @Test
        void testBacktrackingShorter() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 1
                    B -> A 1
                    A -> C 5
                    B -> C 10""");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            // Confirm that the pathInfo map includes all 3 reachable vertices
            assertEquals(3, paths.size());
            // A is the source, so the distance is 0 and no edge leads to it
            assertEquals(0, paths.get(va).distance());
            // B is reachable from A via edge A -> B, with total cost 1
            assertEquals(1, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            // C is reachable directly from A via A -> C with cost 5
            assertEquals(5, paths.get(vc).distance());
            assertEquals(g.getEdge(va, vc), paths.get(vc).lastEdge());
        }

        @DisplayName("In a graph where some shortest path includes at least 3 edges.")
        @Test
        void testLongerPaths() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 2
                    B -> C 2
                    C -> D 2
                    A -> D 10""");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            // Ensure that all four vertices are included in the result
            assertEquals(4, paths.size());
            // Source A has distance 0 and no edge leading to it
            assertEquals(0, paths.get(va).distance());
            // Vertex B is reached via A -> B with distance 2
            assertEquals(2, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            // Vertex C is reached via A -> B -> C with total distance 4
            assertEquals(4, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());
            // Vertex D is reached via A -> B -> C -> D with total distance 6
            assertEquals(6, paths.get(vd).distance());
            assertEquals(g.getEdge(vc, vd), paths.get(vd).lastEdge());
        }
    }

    // Example graph from Prof. Myers's notes
    public static final String graph2 = """
            A -> B 9
            A -> C 14
            A -> D 15
            B -> E 23
            C -> E 17
            C -> D 5
            C -> F 30
            D -> F 20
            D -> G 37
            E -> F 3
            E -> G 20
            F -> G 16""";

    /**
     * Ensures `pathEdges` is a well-formed path: the `dst` of each edge equals the `src` of the
     * subsequent edge, and that the ordered list of all vertices in the path equals
     * `expectedVertices`. Requires `path` is non-empty.
     */
    private void assertPathVertices(List<String> expectedVertices, List<SimpleEdge> pathEdges) {
        ArrayList<String> pathVertices = new ArrayList<>();
        pathVertices.add(pathEdges.getFirst().src().label);
        for (SimpleEdge e : pathEdges) {
            assertEquals(pathVertices.getLast(), e.src().label);
            pathVertices.add(e.dst().label);
        }
        assertIterableEquals(expectedVertices, pathVertices);
    }

    @DisplayName("WHEN a weighted, directed graph is given, THEN `shortestNonBacktracking` returns"
            + "the list of edges in the shortest non-backtracking path from a `src` vertex to a "
            + "`dst` vertex, or null if no such path exists.")
    @Nested
    class testShortestNonBacktrackingPath {

        @DisplayName("When the shortest non-backtracking path consists of multiple edges.")
        @Test
        void testLongPath() {
            SimpleGraph g = SimpleGraph.fromText(graph2);
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("G"), null);
            assertNotNull(path);
            assertPathVertices(Arrays.asList("A", "C", "E", "F", "G"), path);
        }

        @DisplayName("When the shortest non-backtracking path consists of a single edge.")
        @Test
        void testOneEdgePath() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 2
                    B -> C 3
                    C -> D 4""");
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(
                    g.getVertex("A"),
                    g.getVertex("B"),
                    null);
            assertNotNull(path);
            assertEquals(1, path.size());
            SimpleEdge edge = path.get(0);
            assertEquals(g.getVertex("A"), edge.src());
            assertEquals(g.getVertex("B"), edge.dst());

        }

        @DisplayName("Path is empty when `src` and `dst` are the same.")
        @Test
        void testEmptyPath() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 2
                    B -> C 3""");
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(
                    g.getVertex("A"),
                    g.getVertex("A"),
                    null);
            assertNotNull(path);
            assertTrue(path.isEmpty());
        }

        @DisplayName("Path is null when there is not a path from `src` to `dst` (even without "
                + "accounting for back-tracking.")
        @Test
        void testNoPath() {
            SimpleGraph g = SimpleGraph.fromText("B -> A 2");
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("B"), null);
            assertNull(path);
        }

        @DisplayName("Path is null when the non-backtracking condition prevents finding a path "
                + "from `src` to `dst`.")
        @Test
        void testNonBacktrackingPreventsPath() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 1
                    B -> A 1""");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleEdge previousEdge = g.getEdge(va, vb);
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(vb, va, previousEdge);
            assertNull(path);
        }

        private List<String> extractVertexLabels(List<SimpleEdge> path) {
            List<String> labels = new LinkedList<>();
            if (path.isEmpty()) {
                return labels;
            }
            labels.add(path.getFirst().src().label());
            for (SimpleEdge e : path) {
                labels.add(e.dst().label());
            }
            return labels;
        }

        @DisplayName("When the graph includes multiple shortest paths from `src` to `dst`, one of "
                + "them is returned")
        @Test
        void testMultipleShortestPaths() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 1
                    A -> C 1
                    B -> D 1
                    C -> D 1""");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");

            // Convert the list of edges into a list of vertex labels
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vd, null);
            assertNotNull(path);
            List<String> pathVertices = extractVertexLabels(path);
            boolean firstOption = pathVertices.equals(List.of("A", "B", "D"));
            boolean secondOption = pathVertices.equals(List.of("A", "C", "D"));
            // Assert that the returned path matches one of the valid shortest paths
            assertTrue(firstOption || secondOption);
        }
    }

}