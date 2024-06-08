package dk.dtu.compute.se.pisd.roborally.rest;

import java.util.List;

// Define the Board class
public class Board {
    private int width;
    private int height;
    private int numberOfCheckPoints;
    private List<Space> spaces;

    // Constructor
    public Board(int width, int height, int numberOfCheckPoints, List<Space> spaces) {
        this.width = width;
        this.height = height;
        this.numberOfCheckPoints = numberOfCheckPoints;
        this.spaces = spaces;
    }

    // Getters
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumberOfCheckPoints() {
        return numberOfCheckPoints;
    }

    public List<Space> getSpaces() {
        return spaces;
    }
}

