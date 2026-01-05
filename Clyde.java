package model;

import java.awt.Color;
import java.util.Random;
import model.MazeGraph.MazeVertex;

/**
 * A ghost that targets PacMann's nearest vertex while distance >= 10. While distance < 10 chooses
 * random coordinates to target. Flees to southeast corner of board.
 */
public class Clyde extends Ghost {

    private final Random rand;

    /**
     * Creates a Clyde that is orange with a delay of 8 seconds.
     */
    public Clyde(GameModel model, Random rand) {
        super(model, new Color(255, 200, 0), 8000);
        this.rand = rand;
    }

    /**
     * Determined target vertex. While chasing and distance >= 10, targets PacMann's nearest vertex.
     * While chasing and distance < 10, chooses random coordinates to target. While fleeing goes to
     * southeast corner.
     */
    @Override
    protected MazeVertex target() {
        // does nothing while in wait state
        if (state() == GhostState.WAIT) {
            return null;
        }
        // determines vertex for chase state
        else if (state() == GhostState.CHASE) {
            MazeVertex clydeVertex = nearestVertex();
            MazeVertex pacVertex = model.pacMann().nearestVertex();
            double dst = Math.sqrt(Math.pow((clydeVertex.loc().i() -
                    pacVertex.loc().i()), 2) + Math.pow((
                    clydeVertex.loc().j() -
                            pacVertex.loc().j()), 2));
            if (dst >= 10) {
                return pacVertex;
            } else {
                int x = rand.nextInt(model.width());
                int y = rand.nextInt(model.height());
                return (model.graph().closestTo(x, y));
            }
            // flees to vertex (model.width() - 3, model.height() - 3)
        } else if (state() == GhostState.FLEE) {
            return model.graph().closestTo(model.width() - 3, model.height() - 3);
        } else {
            throw new IllegalStateException();
        }
    }
}

