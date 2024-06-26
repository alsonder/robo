package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;


public class Walls {

    private static Heading heading;

    public static Heading getHeading() {
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }

    public static boolean getWalls(@NotNull Space space) {
        Player player = space.getPlayer();

        if(space.getPlayer() == player && getHeading() == player.getHeading()){

            player.setSpace(space);

            return true;
        }



        return false;
    }

}
