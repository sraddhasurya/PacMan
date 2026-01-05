package model;

import model.MazeGraph.MazeVertex;
import java.awt.Color;

/**
 * Ghost that places PacMann in the middle of itself and Blinky while chasing. Flees to southwest
 * corner of the board.
 */
public class Inky extends Ghost {

    /**
     * Creates an Inky that is teal with a delay of 6 seconds.
     */
    public Inky(GameModel model) {
        super(model, new Color(0, 255, 255), 6000);
    }

    /**
     * Targets a vertex such that PacMann's nearestVertex() is the midpoint of it's vertex and
     * Blinky's nearestVertex() while chasing. In FLEE state targets southwest corner.
     */
    @Override
    protected MazeVertex target() {
        // do nothing while in the waiting state
        if (state() == GhostState.WAIT) {
            return null;
        }
        // determine target vertex for chase state while handling out of bound vertices
        else if (state() == GhostState.CHASE) {
            MazeVertex pacVertex = model.pacMann().nearestVertex();
            MazeVertex blinkyVertex = model.blinky().nearestVertex();

            double pacX = pacVertex.loc().i();
            double pacY = pacVertex.loc().j();

            double bx = blinkyVertex.loc().i();
            double by = blinkyVertex.loc().j();

            double targetX = 2 * pacX - bx;
            double targetY = 2 * pacY - by;
            return model.graph().closestTo((int) targetX, (int) targetY);
            // Targets vertex (2, model.height() - 3) while fleeing
        } else if (state() == GhostState.FLEE) {
            int x = 2;
            int y = model.height() - 3;
            return model.graph().closestTo(x, y);
        } else {
            throw new IllegalStateException();
        }

    }
}

