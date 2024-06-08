package dk.dtu.compute.se.pisd.roborally.rest;

// Define the CheckPoint class
public class CheckPoint implements Action {
    private int number;

    // Constructor
    public CheckPoint(int number) {
        this.number = number;
    }

    // Getter
    public int getNumber() {
        return number;
    }
}
