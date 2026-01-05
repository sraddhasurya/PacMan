package model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import model.MazeGraph.Direction;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.IPair;
import model.MazeGraph.MazeVertex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.GameMap;
import util.MazeGenerator.TileType;

public class MazeGraphTest {

    /* Note, to conform to the precondition of the `MazeGraph` constructor, make sure that any
     * TileType arrays that you construct contain a `PATH` tile at index [2][2] and represent a
     * single, orthogonally connected component of `PATH` tiles. */

    /**
     * Create a game map with tile types corresponding to the letters on each line of `template`.
     * 'w' = WALL, 'p' = PATH, and 'g' = GHOSTBOX.  The letters of `template` must form a rectangle.
     * Elevations will be a gradient from the top-left to the bottom-right corner with a horizontal
     * slope of 2 and a vertical slope of 1.
     */
    static GameMap createMap(String template) {
        Scanner lines = new Scanner(template);
        ArrayList<ArrayList<TileType>> lineLists = new ArrayList<>();

        while (lines.hasNextLine()) {
            ArrayList<TileType> lineList = new ArrayList<>();
            for (char c : lines.nextLine().toCharArray()) {
                switch (c) {
                    case 'w' -> lineList.add(TileType.WALL);
                    case 'p' -> lineList.add(TileType.PATH);
                    case 'g' -> lineList.add(TileType.GHOSTBOX);
                }
            }
            lineLists.add(lineList);
        }

        int height = lineLists.size();
        int width = lineLists.getFirst().size();

        TileType[][] types = new TileType[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                types[i][j] = lineLists.get(j).get(i);
            }
        }

        double[][] elevations = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                elevations[i][j] = (2.0 * i + j);
            }
        }
        return new GameMap(types, elevations);
    }

    @DisplayName("WHEN a GameMap with exactly one path tile in position [2][2] is passed into the "
            + "MazeGraph constructor, THEN a graph with one vertex is created.")
    @Test
    void testOnePathCell() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwpww
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(1, vertices.size());
        assertTrue(vertices.containsKey(new IPair(2, 2)));
    }

    @DisplayName("WHEN a GameMap with exactly two horizontally adjacent path tiles is passed into "
            + "the MazeGraph constructor, THEN a graph with two vertices is created in which the two "
            + "vertices are connected by two directed edges with weights determined by evaluating "
            + "`MazeGraph.edgeWeight` on their elevations.")
    @Test
    void testTwoPathCellsHorizontal() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwppw
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // graph contains two vertices with the correct locations
        assertEquals(2, vertices.size());
        IPair left = new IPair(2, 2);
        IPair right = new IPair(3, 2);
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex vl = vertices.get(left);
        MazeVertex vr = vertices.get(right);

        // left vertex has one edge to the vertex to its right
        assertNull(vl.edgeInDirection(Direction.LEFT));
        assertNull(vl.edgeInDirection(Direction.UP));
        assertNull(vl.edgeInDirection(Direction.DOWN));
        MazeEdge l2r = vl.edgeInDirection(Direction.RIGHT);
        assertNotNull(l2r);

        // edge from left to right has the correct fields
        double lElev = map.elevations()[2][2];
        double rElev = map.elevations()[3][2];
        assertEquals(vl, l2r.src());
        assertEquals(vr, l2r.dst());
        assertEquals(Direction.RIGHT, l2r.direction());
        assertEquals(MazeGraph.edgeWeight(lElev, rElev), l2r.weight());

        // right vertex has one edge to the vertex to its left with the correct fields
        assertNull(vr.edgeInDirection(Direction.RIGHT));
        assertNull(vr.edgeInDirection(Direction.UP));
        assertNull(vr.edgeInDirection(Direction.DOWN));
        MazeEdge r2l = vr.edgeInDirection(Direction.LEFT);
        assertNotNull(r2l);
        assertEquals(vr, r2l.src());
        assertEquals(vl, r2l.dst());
        assertEquals(Direction.LEFT, r2l.direction());
        assertEquals(MazeGraph.edgeWeight(rElev, lElev), r2l.weight());
    }

    @DisplayName("WHEN a GameMap with exactly two vertically adjacent path tiles is passed into "
            + "the MazeGraph constructor, THEN a graph with two vertices is created in which the two "
            + "vertices are connected by two directed edges with weights determined by evaluating "
            + "`MazeGraph.edgeWeight` on their elevations.")
    @Test
    void testTwoPathCellsVertical() {
        GameMap map = createMap("""
                wwwww
                wwpww
                wwpww
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(2, vertices.size());
        IPair top = new IPair(2, 1);
        IPair bottom = new IPair(2, 2);
        assertTrue(vertices.containsKey(top));
        assertTrue(vertices.containsKey(bottom));
        MazeVertex vertexTop = vertices.get(top);
        MazeVertex vertexBottom = vertices.get(bottom);

        // asserts only edge from top vertex points downwards
        assertNull(vertexTop.edgeInDirection(Direction.LEFT));
        assertNull(vertexTop.edgeInDirection(Direction.RIGHT));
        assertNull(vertexTop.edgeInDirection(Direction.UP));
        MazeEdge topToBottom = vertexTop.edgeInDirection(Direction.DOWN);
        assertNotNull(topToBottom);

        // asserts correct edge weight
        double topElevation = map.elevations()[2][1];
        double bottomElevation = map.elevations()[2][2];
        assertEquals(vertexTop, topToBottom.src());
        assertEquals(vertexBottom, topToBottom.dst());
        assertEquals(Direction.DOWN, topToBottom.direction());
        assertEquals(MazeGraph.edgeWeight(topElevation, bottomElevation), topToBottom.weight());

        // asserts only edge from bottom vertex points upwards
        assertNull(vertexBottom.edgeInDirection(Direction.LEFT));
        assertNull(vertexBottom.edgeInDirection(Direction.RIGHT));
        assertNull(vertexBottom.edgeInDirection(Direction.DOWN));
        MazeEdge bottomToTop = vertexBottom.edgeInDirection(Direction.UP);
        assertNotNull(bottomToTop);

        // asserts edge weight
        assertEquals(vertexBottom, bottomToTop.src());
        assertEquals(vertexTop, bottomToTop.dst());
        assertEquals(Direction.UP, bottomToTop.direction());
        assertEquals(MazeGraph.edgeWeight(bottomElevation, topElevation), bottomToTop.weight());

    }

    @DisplayName("WHEN a GameMap includes two path tiles in the first and last column of the same "
            + "row, THEN (tunnel) edges are created between these tiles with the correct properties.")
    @Test
    void testHorizontalTunnelEdgeCreation() {
        GameMap map = createMap("""
                pwwwp
                wwwww
                wwwww
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));
        assertEquals(2, vertices.size());
        IPair left = new IPair(0, 0);
        IPair right = new IPair(4, 0);

        // both path tiles contained in vertices
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex vertexRight = vertices.get(right);
        MazeVertex vertexLeft = vertices.get(left);

        // asserts only edge from the left vertex points left
        assertNull(vertexLeft.edgeInDirection(Direction.RIGHT));
        assertNull(vertexLeft.edgeInDirection(Direction.UP));
        assertNull(vertexLeft.edgeInDirection(Direction.DOWN));
        MazeEdge leftToRight = vertexLeft.edgeInDirection(Direction.LEFT);
        assertNotNull(leftToRight);

        // asserts correct edge weight
        double leftElevation = map.elevations()[0][0];
        double rightElevation = map.elevations()[4][0];
        assertEquals(vertexLeft, leftToRight.src());
        assertEquals(vertexRight, leftToRight.dst());
        assertEquals(Direction.LEFT, leftToRight.direction());
        assertEquals(MazeGraph.edgeWeight(leftElevation, rightElevation), leftToRight.weight());

        // asserts only edge from right vertex points right
        assertNull(vertexRight.edgeInDirection(Direction.LEFT));
        assertNull(vertexRight.edgeInDirection(Direction.UP));
        assertNull(vertexRight.edgeInDirection(Direction.DOWN));
        MazeEdge rightToLeft = vertexRight.edgeInDirection(Direction.RIGHT);

        // asserts correct edge weight
        assertEquals(vertexRight, rightToLeft.src());
        assertEquals(vertexLeft, rightToLeft.dst());
        assertEquals(Direction.RIGHT, rightToLeft.direction());
        assertEquals(MazeGraph.edgeWeight(rightElevation, leftElevation), rightToLeft.weight());

    }

    @DisplayName("WHEN a GameMap includes a cyclic connected component of path tiles with a "
            + "non-path tiles in the middle, THEN its graph includes edges between all adjacent "
            + "pairs of vertices.")
    @Test
    void testCyclicPaths() {
        GameMap map = createMap("""
                wwwwwww
                wwwwwww
                wwpppww
                wwpwpww
                wwpppww
                wwwwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(8, vertices.size());
        IPair topLeft = new IPair(2, 2);
        IPair topMiddle = new IPair(3, 2);
        IPair topRight = new IPair(4, 2);
        IPair middleRight = new IPair(4, 3);
        IPair bottomRight = new IPair(4, 4);
        IPair bottomMiddle = new IPair(3, 4);
        IPair bottomLeft = new IPair(2, 4);
        IPair middleLeft = new IPair(2, 3);

        assertTrue(vertices.containsKey(topLeft));
        assertTrue(vertices.containsKey(topMiddle));
        assertTrue(vertices.containsKey(topRight));
        assertTrue(vertices.containsKey(middleRight));
        assertTrue(vertices.containsKey(bottomRight));
        assertTrue(vertices.containsKey(bottomMiddle));
        assertTrue(vertices.containsKey(bottomLeft));
        assertTrue(vertices.containsKey(middleLeft));

        MazeVertex vertex = vertices.get(topLeft);
        assertNotNull(vertex.edgeInDirection(Direction.RIGHT));
        assertEquals(vertices.get(topMiddle), vertex.edgeInDirection(Direction.RIGHT).dst());
        assertNotNull(vertex.edgeInDirection(Direction.DOWN));
        assertEquals(vertices.get(middleLeft), vertex.edgeInDirection(Direction.DOWN).dst());
        assertNull(vertex.edgeInDirection(Direction.LEFT));
        assertNull(vertex.edgeInDirection(Direction.UP));

        vertex = vertices.get(topMiddle);
        assertNotNull(vertex.edgeInDirection(Direction.LEFT));
        assertEquals(vertices.get(topLeft), vertex.edgeInDirection(Direction.LEFT).dst());
        assertNotNull(vertex.edgeInDirection(Direction.RIGHT));
        assertEquals(vertices.get(topRight), vertex.edgeInDirection(Direction.RIGHT).dst());
        assertNull(vertex.edgeInDirection(Direction.UP));
        assertNull(vertex.edgeInDirection(Direction.DOWN));

        vertex = vertices.get(topRight);
        assertNotNull(vertex.edgeInDirection(Direction.LEFT));
        assertEquals(vertices.get(topMiddle), vertex.edgeInDirection(Direction.LEFT).dst());
        assertNotNull(vertex.edgeInDirection(Direction.DOWN));
        assertEquals(vertices.get(middleRight), vertex.edgeInDirection(Direction.DOWN).dst());
        assertNull(vertex.edgeInDirection(Direction.RIGHT));
        assertNull(vertex.edgeInDirection(Direction.UP));

        vertex = vertices.get(middleRight);
        assertNotNull(vertex.edgeInDirection(Direction.UP));
        assertEquals(vertices.get(topRight), vertex.edgeInDirection(Direction.UP).dst());
        assertNotNull(vertex.edgeInDirection(Direction.DOWN));
        assertEquals(vertices.get(bottomRight), vertex.edgeInDirection(Direction.DOWN).dst());
        assertNull(vertex.edgeInDirection(Direction.LEFT));
        assertNull(vertex.edgeInDirection(Direction.RIGHT));

        vertex = vertices.get(bottomRight);
        assertNotNull(vertex.edgeInDirection(Direction.UP));
        assertEquals(vertices.get(middleRight), vertex.edgeInDirection(Direction.UP).dst());
        assertNotNull(vertex.edgeInDirection(Direction.LEFT));
        assertEquals(vertices.get(bottomMiddle), vertex.edgeInDirection(Direction.LEFT).dst());
        assertNull(vertex.edgeInDirection(Direction.DOWN));
        assertNull(vertex.edgeInDirection(Direction.RIGHT));

        vertex = vertices.get(bottomMiddle);
        assertNotNull(vertex.edgeInDirection(Direction.RIGHT));
        assertEquals(vertices.get(bottomRight), vertex.edgeInDirection(Direction.RIGHT).dst());
        assertNotNull(vertex.edgeInDirection(Direction.LEFT));
        assertEquals(vertices.get(bottomLeft), vertex.edgeInDirection(Direction.LEFT).dst());
        assertNull(vertex.edgeInDirection(Direction.UP));
        assertNull(vertex.edgeInDirection(Direction.DOWN));

        vertex = vertices.get(bottomLeft);
        assertNotNull(vertex.edgeInDirection(Direction.RIGHT));
        assertEquals(vertices.get(bottomMiddle), vertex.edgeInDirection(Direction.RIGHT).dst());
        assertNotNull(vertex.edgeInDirection(Direction.UP));
        assertEquals(vertices.get(middleLeft), vertex.edgeInDirection(Direction.UP).dst());
        assertNull(vertex.edgeInDirection(Direction.LEFT));
        assertNull(vertex.edgeInDirection(Direction.DOWN));

        vertex = vertices.get(middleLeft);
        assertNotNull(vertex.edgeInDirection(Direction.DOWN));
        assertEquals(vertices.get(bottomLeft), vertex.edgeInDirection(Direction.DOWN).dst());
        assertNotNull(vertex.edgeInDirection(Direction.UP));
        assertEquals(vertices.get(topLeft), vertex.edgeInDirection(Direction.UP).dst());
        assertNull(vertex.edgeInDirection(Direction.LEFT));
        assertNull(vertex.edgeInDirection(Direction.RIGHT));


    }

    @DisplayName("WHEN a GameMap includes two path tiles in the first and last row of the same "
            + "column, THEN (tunnel) edges are created between these tiles with the correct properties.")
    @Test
    void testVerticalTunnelEdgeCreation() {
        GameMap map = createMap("""
                wpwww
                wwwww
                wwwww
                wwwww
                wpwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));
        assertEquals(2, vertices.size());
        IPair up = new IPair(1, 0);
        IPair down = new IPair(1, 4);

        // vertices contains the two path tiles
        assertTrue(vertices.containsKey(up));
        assertTrue(vertices.containsKey(down));

        MazeVertex vertexUp = vertices.get(up);
        MazeVertex vertexDown = vertices.get(down);

        // the only edge from the top vertex points up
        assertNull(vertexUp.edgeInDirection(Direction.LEFT));
        assertNull(vertexUp.edgeInDirection(Direction.RIGHT));
        assertNull(vertexUp.edgeInDirection(Direction.DOWN));
        MazeEdge upToDown = vertexUp.edgeInDirection(Direction.UP);
        assertNotNull(upToDown);

        // asserts correct edge weight
        double upElevation = map.elevations()[1][0];
        double downElevation = map.elevations()[1][4];
        assertEquals(vertexUp, upToDown.src());
        assertEquals(vertexDown, upToDown.dst());
        assertEquals(Direction.UP, upToDown.direction());
        assertEquals(MazeGraph.edgeWeight(upElevation, downElevation), upToDown.weight());

        // asserts only edge from bottom vertex points down
        assertNull(vertexDown.edgeInDirection(Direction.RIGHT));
        assertNull(vertexDown.edgeInDirection(Direction.UP));
        assertNull(vertexDown.edgeInDirection(Direction.LEFT));
        MazeEdge downToUp = vertexDown.edgeInDirection(Direction.DOWN);

        // asserts correct edge weight
        assertEquals(vertexDown, downToUp.src());
        assertEquals(vertexUp, downToUp.dst());
        assertEquals(Direction.DOWN, downToUp.direction());
        assertEquals(MazeGraph.edgeWeight(downElevation, upElevation), downToUp.weight());

    }

    @DisplayName("When the GameMap has vertical and horizontal tunneling the correct edges are"
            + "created between path tiles")
    @Test
    void testDisconnectPath() {
        GameMap map = createMap("""
                pwwwp
                pwwwp
                wwwww
                pwwwp
                pwwwp""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(8, vertices.size());

        // Define the locations
        IPair leftTop = new IPair(0, 0);
        IPair leftBottom = new IPair(0, 1);
        IPair leftLowerTop = new IPair(0, 3);
        IPair leftLowerBottom = new IPair(0, 4);

        IPair rightTop = new IPair(4, 0);
        IPair rightBottom = new IPair(4, 1);
        IPair rightLowerTop = new IPair(4, 3);
        IPair rightLowerBottom = new IPair(4, 4);

        // Obtain the MazeVertex for each location
        MazeVertex vLeftTop = vertices.get(leftTop);
        MazeVertex vLeftBottom = vertices.get(leftBottom);
        MazeVertex vLeftLowerTop = vertices.get(leftLowerTop);
        MazeVertex vLeftLowerBottom = vertices.get(leftLowerBottom);

        MazeVertex vRightTop = vertices.get(rightTop);
        MazeVertex vRightBottom = vertices.get(rightBottom);
        MazeVertex vRightLowerTop = vertices.get(rightLowerTop);
        MazeVertex vRightLowerBottom = vertices.get(rightLowerBottom);

        // assert the correct edges of vertex (0, 0)
        assertNotNull(vLeftTop.edgeInDirection(Direction.DOWN));
        assertEquals(vLeftBottom, vLeftTop.edgeInDirection(Direction.DOWN).dst());
        assertNotNull(vLeftTop.edgeInDirection(Direction.LEFT));
        assertEquals(vRightTop, vLeftTop.edgeInDirection(Direction.LEFT).dst());
        assertNotNull(vLeftTop.edgeInDirection(Direction.UP));
        assertEquals(vLeftLowerBottom, vLeftTop.edgeInDirection(Direction.UP).dst());
        assertNull(vLeftTop.edgeInDirection(Direction.RIGHT));

        // assert the correct edges of vertex (0, 1)
        assertNotNull(vLeftBottom.edgeInDirection(Direction.UP));
        assertEquals(vLeftTop, vLeftBottom.edgeInDirection(Direction.UP).dst());
        assertNotNull(vLeftBottom.edgeInDirection(Direction.LEFT));
        assertEquals(vRightBottom, vLeftBottom.edgeInDirection(Direction.LEFT).dst());
        assertNull(vLeftBottom.edgeInDirection(Direction.DOWN));
        assertNull(vLeftBottom.edgeInDirection(Direction.RIGHT));

        // assert the correct edges of vertex (0, 3)
        assertNotNull(vLeftLowerTop.edgeInDirection(Direction.DOWN));
        assertEquals(vLeftLowerBottom, vLeftLowerTop.edgeInDirection(Direction.DOWN).dst());
        assertNotNull(vLeftLowerTop.edgeInDirection(Direction.LEFT));
        assertEquals(vRightLowerTop, vLeftLowerTop.edgeInDirection(Direction.LEFT).dst());
        assertNull(vLeftLowerTop.edgeInDirection(Direction.UP));
        assertNull(vLeftLowerTop.edgeInDirection(Direction.RIGHT));

        // assert the correct edges of vertex (0, 4)
        assertNotNull(vLeftLowerBottom.edgeInDirection(Direction.UP));
        assertEquals(vLeftLowerTop, vLeftLowerBottom.edgeInDirection(Direction.UP).dst());
        assertNotNull(vLeftLowerBottom.edgeInDirection(Direction.DOWN));
        assertEquals(vLeftTop, vLeftLowerBottom.edgeInDirection(Direction.DOWN).dst());
        assertNotNull(vLeftLowerBottom.edgeInDirection(Direction.LEFT));
        assertEquals(vRightLowerBottom, vLeftLowerBottom.edgeInDirection(Direction.LEFT).dst());
        assertNull(vLeftLowerBottom.edgeInDirection(Direction.RIGHT));

        // assert the correct edges of vertex (4, 0)
        assertNotNull(vRightTop.edgeInDirection(Direction.RIGHT));
        assertEquals(vLeftTop, vRightTop.edgeInDirection(Direction.RIGHT).dst());
        assertNotNull(vRightTop.edgeInDirection(Direction.DOWN));
        assertEquals(vRightBottom, vRightTop.edgeInDirection(Direction.DOWN).dst());
        assertNotNull(vRightTop.edgeInDirection(Direction.UP));
        assertEquals(vRightLowerBottom, vRightTop.edgeInDirection(Direction.UP).dst());
        assertNull(vRightTop.edgeInDirection(Direction.LEFT));

        // assert the correct edges of vertex (4, 1)
        assertNotNull(vRightBottom.edgeInDirection(Direction.UP));
        assertEquals(vRightTop, vRightBottom.edgeInDirection(Direction.UP).dst());
        assertNotNull(vRightBottom.edgeInDirection(Direction.RIGHT));
        assertEquals(vLeftBottom, vRightBottom.edgeInDirection(Direction.RIGHT).dst());
        assertNull(vRightBottom.edgeInDirection(Direction.DOWN));
        assertNull(vRightBottom.edgeInDirection(Direction.LEFT));

        // assert the correct edges of vertex (4, 3)
        assertNotNull(vRightLowerTop.edgeInDirection(Direction.DOWN));
        assertEquals(vRightLowerBottom, vRightLowerTop.edgeInDirection(Direction.DOWN).dst());
        assertNotNull(vRightLowerTop.edgeInDirection(Direction.RIGHT));
        assertEquals(vLeftLowerTop, vRightLowerTop.edgeInDirection(Direction.RIGHT).dst());
        assertNull(vRightLowerTop.edgeInDirection(Direction.UP));
        assertNull(vRightLowerTop.edgeInDirection(Direction.LEFT));

        // assert the correct edges of vertex (4, 4)
        assertNotNull(vRightLowerBottom.edgeInDirection(Direction.UP));
        assertEquals(vRightLowerTop, vRightLowerBottom.edgeInDirection(Direction.UP).dst());
        assertNotNull(vRightLowerBottom.edgeInDirection(Direction.DOWN));
        assertEquals(vRightTop, vRightLowerBottom.edgeInDirection(Direction.DOWN).dst());
        assertNotNull(vRightLowerBottom.edgeInDirection(Direction.RIGHT));
        assertEquals(vLeftLowerBottom, vRightLowerBottom.edgeInDirection(Direction.RIGHT).dst());
        assertNull(vRightLowerBottom.edgeInDirection(Direction.LEFT));
    }

    @DisplayName("When a gamemap forms a zigzag path, then the graph connects properly across all "
            + "the turns and straights")
    @Test
    void testSnakePath() {
        GameMap map = createMap("""
                ppwww
                wppww
                wwppw
                wwwww
                """);

        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(6, vertices.size());

        IPair point00 = new IPair(0, 0);
        IPair point10 = new IPair(1, 0);
        IPair point11 = new IPair(1, 1);
        IPair point21 = new IPair(2, 1);
        IPair point22 = new IPair(2, 2);
        IPair point32 = new IPair(3, 2);

        assertTrue(vertices.containsKey(point00));
        assertTrue(vertices.containsKey(point10));
        assertTrue(vertices.containsKey(point11));
        assertTrue(vertices.containsKey(point21));
        assertTrue(vertices.containsKey(point22));
        assertTrue(vertices.containsKey(point32));

        MazeVertex vertex00 = vertices.get(point00);
        MazeVertex vertex10 = vertices.get(point10);
        MazeVertex vertex11 = vertices.get(point11);
        MazeVertex vertex21 = vertices.get(point21);
        MazeVertex vertex22 = vertices.get(point22);
        MazeVertex vertex32 = vertices.get(point32);

        assertNotNull(vertex00.edgeInDirection(Direction.RIGHT));
        assertEquals(vertex10, vertex00.edgeInDirection(Direction.RIGHT).dst());

        // (1,0) connects LEFT ← (0,0) and DOWN ↓ (1,1)
        assertNotNull(vertex10.edgeInDirection(Direction.LEFT));
        assertEquals(vertex00, vertex10.edgeInDirection(Direction.LEFT).dst());
        assertNotNull(vertex10.edgeInDirection(Direction.DOWN));
        assertEquals(vertex11, vertex10.edgeInDirection(Direction.DOWN).dst());

        // (1,1) connects UP ↑ (1,0) and RIGHT → (2,1)
        assertNotNull(vertex11.edgeInDirection(Direction.UP));
        assertEquals(vertex10, vertex11.edgeInDirection(Direction.UP).dst());
        assertNotNull(vertex11.edgeInDirection(Direction.RIGHT));
        assertEquals(vertex21, vertex11.edgeInDirection(Direction.RIGHT).dst());

        // (2,1) connects LEFT ← (1,1) and DOWN ↓ (2,2)
        assertNotNull(vertex21.edgeInDirection(Direction.LEFT));
        assertEquals(vertex11, vertex21.edgeInDirection(Direction.LEFT).dst());
        assertNotNull(vertex21.edgeInDirection(Direction.DOWN));
        assertEquals(vertex22, vertex21.edgeInDirection(Direction.DOWN).dst());

        // (2,2) connects UP ↑ (2,1) and RIGHT → (3,2)
        assertNotNull(vertex22.edgeInDirection(Direction.UP));
        assertEquals(vertex21, vertex22.edgeInDirection(Direction.UP).dst());
        assertNotNull(vertex22.edgeInDirection(Direction.RIGHT));
        assertEquals(vertex32, vertex22.edgeInDirection(Direction.RIGHT).dst());

        // (3,2) connects LEFT ← (2,2)
        assertNotNull(vertex32.edgeInDirection(Direction.LEFT));
        assertEquals(vertex22, vertex32.edgeInDirection(Direction.LEFT).dst());

        // (3,2) has no DOWN, RIGHT, UP
        assertNull(vertex32.edgeInDirection(Direction.DOWN));
        assertNull(vertex32.edgeInDirection(Direction.RIGHT));
        assertNull(vertex32.edgeInDirection(Direction.UP));
    }


}