package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Phase;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

public class CheckPoint extends FieldAction{
    private int number;

    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }


    /**
     * This is called then the activation phase are done
     * then it does the specific action it have been given
     * so here it checks if a player is on an eligible check point
     * and if true it gives then that number of check point.
     *
     * @param gameController the controller that uses all game logic
     * @param space the space there the checkpoint, is checked if a player is there
     * @return returns true if a player is there false otherwise
     * @author Anders
     */
    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player player = space.getPlayer();
        if(player.getCeckPoint() == number -1){
            player.updateCheckPoint();
            if(gameController.board.numberOfCheckPoints == player.getCeckPoint()){
                gameController.board.setPhase(Phase.GAME_WON);
            }
            return true;
        }

        return false;
    }
}
