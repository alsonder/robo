package dk.dtu.compute.se.pisd.roborally.rest;

import java.util.List;

// Define the Space class
public class Space {
    private List<String> walls;
    private List<Action> actions;
    private int x;
    private int y;

    // Constructor
    public Space(List<String> walls, List<Action> actions, int x, int y) {
        this.walls = walls;
        this.actions = actions;
        this.x = x;
        this.y = y;
    }

    // Getters
    public List<String> getWalls() {
        return walls;
    }

    public List<Action> getActions() {
        return actions;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
