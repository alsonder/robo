package dk.dtu.compute.se.pisd.roborally.test;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import org.testng.annotations.Test;

import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.assertEquals;

/**
 * This class tests the energy cube functionality in the Player class.
 */
public class EnergyCubesTest {
    /**
     * This test checks if the addEnergyCubes and useEnergyCubes methods in the Player class work as expected.
     * It first checks that a new player starts with 0 energy cubes.
     * Then it adds 5 energy cubes and checks that the player's energy cube count is updated correctly.
     * It uses 3 energy cubes and checks that the count is again updated correctly.
     * Finally, it attempts to use more energy cubes than the player has, which should throw an IllegalArgumentException.
     * @author Aleksander Sonder, s185289
     */
    @Test
    public void testEnergyCubes() {
        Board board = new Board(8, 8, 2);
        Player player = new Player(board, "black", "Aleksander");

        // 0 energy cubes
        assertEquals(0, player.getEnergyCubes());

        // Add 5 energy
        player.addEnergyCubes(5);
        assertEquals(5, player.getEnergyCubes());

        // Use 3 energy cubes
        player.useEnergyCubes(3);
        assertEquals(2, player.getEnergyCubes());

        // Use more cubes than available
        assertThrows(IllegalArgumentException.class, () -> player.useEnergyCubes(3));
    }
}