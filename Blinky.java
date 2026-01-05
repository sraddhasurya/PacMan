package model;

import java.awt.Color;
import model.MazeGraph.MazeVertex;

/**
 * Ghost that directly targets PacMann in CHASE state and targets northwest corner of board in the
 * FLEE state.
 */
public class Blinky extends Ghost {

    /**
     * Constructs a Blinky which an initial delay of 2 seconds and the color red
     */
    public Blinky(GameModel model) {
        super(model, new Color(255, 0, 0), 2000);
    }

    /**
     * Targets the nearest vertex of PacMann in the CHASE state and the vertex (2, 2) while in the
     * FLEE state
     */
    @Override
    protected MazeVertex target() {
        // don't do anything if in waiting state
        if (state() == GhostState.WAIT) {
            return null;
            // if chasing target nearest vertex of PacMann
        } else if (state() == GhostState.CHASE) {
            return model.pacMann().nearestVertex();
            // if fleeing target vertex (2, 2)
        } else if (state() == GhostState.FLEE) {
            for (MazeVertex v : model.graph().vertices()) {
                if (v.loc().i() == 2 && v.loc().j() == 2) {
                    return v;
                }
            }
            throw new IllegalStateException();
        } else {
            throw new IllegalStateException();
        }
    }
}
