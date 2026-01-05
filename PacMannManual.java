package model;

import model.MazeGraph.Direction;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.MazeVertex;

/**
 * Subclass of PacMann responsible for determining the next edge PacMann will travel to
 */
public class PacMannManual extends PacMann {

    /**
     * Construct a PacMannManual with the associated GameModel
     */
    public PacMannManual(GameModel gm) {
        super(gm);

    }

    /**
     * Returns edge in direction of most recent player command, if exists. Else returns edge in
     * direction PacMann was traveling prior, if exists. Else returns null.
     */
    @Override
    public MazeEdge nextEdge() {
        MazeVertex currVertex = nearestVertex();

        // Obtains direction of most recent player command
        Direction dir = super.model.playerCommand();

        // Returns edge in the direction of most recent player command, if exists
        MazeEdge edge = currVertex.edgeInDirection(dir);
        if (edge != null) {
            return edge;
        }

        // If that fails, then returns edge in direction of prior travel or null if it is null
        dir = currentEdge().direction();
        edge = currVertex.edgeInDirection(dir);
        return edge;
    }
}
