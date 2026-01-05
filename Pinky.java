package model;

import java.awt.Color;
import model.MazeGraph.MazeVertex;

/**
 * Ghosts that targets the vertex 3 in front of PacMann in its direction of travel in CHASE state.
 * Targets northeast corner of board in FLEE state.
 */
public class Pinky extends Ghost {

    /**
     * Constructs a Pinky with color pink and an initial delay of 4 seconds
     */
    public Pinky(GameModel model) {
        super(model, new Color(255, 192, 203), 4000);
    }

    /**
     * Determines target vertex for a Pinky. If in CHASE state targets vertex 3 away from PacMann in
     * its direction of travel. If in FLEE state targets vertex (model.width() - 3, 2).
     */
    @Override
    protected MazeVertex target() {
        // determined vertex 3 in front of PacMann's direction of travel
        if (state() == GhostState.CHASE) {
            MazeVertex src = model.pacMann().location().edge().src();
            MazeVertex dst = model.pacMann().location().edge().dst();
            int x = dst.loc().i() - src.loc().i();
            int y = dst.loc().j() - src.loc().j();

            // handles out of bounds vertices
            double pacX = model.pacMann().getBoundingBoxUL().i();
            double pacY = model.pacMann().getBoundingBoxUL().j();

            double targetX = pacX + 3 * x;
            double targetY = pacY + 3 * y;

            return model.graph().closestTo((int) targetX, (int) targetY);
            // northeast corner of board while fleeing
        } else if (state() == GhostState.FLEE) {
            int x = model.width() - 3;
            int y = 2;
            return model.graph().closestTo(x, y);
            // do nothing if waiting
        } else if (state() == GhostState.WAIT) {
            return null;
        } else {
            throw new IllegalStateException();
        }
    }
}


