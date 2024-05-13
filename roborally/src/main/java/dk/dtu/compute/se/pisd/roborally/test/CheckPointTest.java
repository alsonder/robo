package dk.dtu.compute.se.pisd.roborally.test;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import org.testng.annotations.Test;

import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.assertEquals;


public class CheckPointTest {

    /**
     * Small test to check if a players check points values can get updated
     * should be made better, so it can test if the last check point is reached
     * it can end the game.
     * @author Anders Jensen,
     */
    @Test
    public void testEnergyCubes() {
        Board board = new Board(8, 8, 2);
        Player player = new Player(board, "black", "Anders");

        assertEquals(0, player.getCheckPoint());

        player.updateCheckPoint();
        assertEquals(1, player.getCheckPoint());

        player.updateCheckPoint();
        assertEquals(2, player.getCheckPoint());




    }


}
