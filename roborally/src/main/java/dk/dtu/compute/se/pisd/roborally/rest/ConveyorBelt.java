package dk.dtu.compute.se.pisd.roborally.rest;

// Define the ConveyorBelt class
public class ConveyorBelt implements Action {
    private String heading;

    // Constructor
    public ConveyorBelt(String heading) {
        this.heading = heading;
    }

    // Getter
    public String getHeading() {
        return heading;
    }
}
