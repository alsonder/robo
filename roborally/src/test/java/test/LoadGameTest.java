package test;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

/**
 * This class tests the energy cube functionality in the Player class.
 */
public class LoadGameTest {
    /**
     * This test checks if the addEnergyCubes and useEnergyCubes methods in the Player class work as expected.
     * It first checks that a new player starts with 0 energy cubes.
     * Then it adds 5 energy cubes and checks that the player's energy cube count is updated correctly.
     * It uses 3 energy cubes and checks that the count is again updated correctly.
     * Finally, it attempts to use more energy cubes than the player has, which should throw an IllegalArgumentException.
     * @author Aleksander Sonder, s185289
     */
    @Test
    public void loadGameTest() {
        // Attempt to load a non-existing board to ensure the default board is loaded
        Board board = LoadBoard.loadBoard(null);

        assertNotNull(board); //The board should not be null when loading the default board.

        // Verify some properties of the default board to ensure it loaded correctly
        assertEquals(8, board.width); //defaultboard is 8x8
        assertEquals(8, board.height);

        Space space = board.getSpace(0,0);
        assertNotNull(space); //Space x,y = 0,0 should not be null and a wall heading south
        assertEquals(space.getWalls().size(), 1);
        assertNotEquals(space.getActions(), board.getSpace(5,4).getActions()); //0,0 should be a conveyorbelt, 5,4 should be a checkpoint
    }

    @Test
    public void testLoadSpecificBoardNotFound() {
        // Attempt to load a non-existing board to ensure the default board is loaded
        Board board = LoadBoard.loadBoard("nonExistingBoard");

        assertNotNull(board); //The board should not be null when the specified board is not found but should be 8x8
        assertEquals(8, board.width);
        assertEquals(8, board.height);

    }
}