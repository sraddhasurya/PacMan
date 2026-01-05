package ui;

import static java.awt.event.KeyEvent.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import javax.swing.Timer;
import javax.swing.event.SwingPropertyChangeSupport;
import model.GameModel;
import model.GameModel.GameState;
import model.MazeGraph.Direction;

public class InteractiveGameController implements KeyListener {

    public enum GameState {RUNNING, PAUSED, LIFESTART, GAMEOVER}

    private GameModel model;
    private final Timer timer;
    private GameState state;

    /**
     * Helper object for managing property change notifications.
     */
    protected SwingPropertyChangeSupport propSupport;

    public InteractiveGameController(GameModel model) {
        state = GameState.LIFESTART;
        timer = new Timer(16, e -> nextFrame());

        boolean notifyOnEdt = true;
        propSupport = new SwingPropertyChangeSupport(this, notifyOnEdt);

        setModel(model);
    }

    public void setModel(GameModel newModel) {
        reset();
        model = newModel;
        model.addPropertyChangeListener("game_state", e -> {
            if (model.state() != GameModel.GameState.PLAYING) {
                stopGame();
            }
        });
    }

    private void stopGame() {
        timer.stop();
        setState(model.state() == GameModel.GameState.READY ? GameState.LIFESTART
                : GameState.GAMEOVER);
    }

    private void nextFrame() {
        // TODO: duration?
        model.updateActors(16);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Runs game when directional command is entered if game currently paused or between lives
        if (state == GameState.PAUSED || state == GameState.LIFESTART) {
            if (code == VK_A || code == VK_LEFT || code == VK_D || code == VK_RIGHT ||
                    code == VK_W || code == VK_UP || code == VK_S || code == VK_DOWN) {
                processStartPause();
            }
        }

        // Moves PacMann based on directional command entered
        switch (code) {
            case VK_A, VK_LEFT -> model.updatePlayerCommand(Direction.LEFT);
            case VK_D, VK_RIGHT -> model.updatePlayerCommand(Direction.RIGHT);
            case VK_W, VK_UP -> model.updatePlayerCommand(Direction.UP);
            case VK_S, VK_DOWN -> model.updatePlayerCommand(Direction.DOWN);
            case VK_SPACE -> processStartPause();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Do nothing
    }

    /**
     * Processes a press of the start/pause button. Toggles between the RUNNING and PAUSED
     * GameStates.
     */
    public void processStartPause() {
        if (state == GameState.PAUSED) {
            setState(GameState.RUNNING);
            timer.start();
        } else if (state == GameState.RUNNING) {
            timer.stop();
            setState(GameState.PAUSED);
        } else if (state == GameState.LIFESTART) {
//            model.useLife();
            setState(GameState.RUNNING);
            timer.start();
        }
    }

    public void pause() {
        if (state == GameState.RUNNING) {
            timer.stop();
            setState(GameState.PAUSED);
        }
    }

    public void reset() {
        timer.stop();
        setState(GameState.LIFESTART);
    }

    public GameState state() {
        return state;
    }

    private void setState(GameState newState) {
        GameState oldState = state;
        state = newState;
        propSupport.firePropertyChange("game_state", oldState, state);
    }

    /* Observation interface */

    /**
     * Register `listener` to be notified whenever any property of this model is changed.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    /**
     * Register `listener` to be notified whenever the property named `propertyName` of this model
     * is changed.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Stop notifying `listener` of property changes for this model (assuming it was added no more
     * than once).  Does not affect listeners who were registered with a particular property name.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    /**
     * Stop notifying `listener` of changes to the property named `propertyName` for this model
     * (assuming it was added no more than once).  Does not affect listeners who were not registered
     * with `propertyName`.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(propertyName, listener);
    }
}
