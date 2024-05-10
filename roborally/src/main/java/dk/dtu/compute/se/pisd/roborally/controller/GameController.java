/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class GameController {

    final public AppController appController;

    final public Board board;

    public GameController(AppController appController, Board board) {
        this.appController = appController;
        this.board = board;
    }




    public void move1(@NotNull Player player) {
        if (player.board == board) {
            Space space = player.getSpace();
            Heading heading = player.getHeading();

            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                try {
                    moveToSpace(player, target, heading);
                } catch (ImpossibleMoveException e) {
                    // we don't do anything here  for now; we just catch the
                    // exception so that we do no pass it on to the caller
                    // (which would be very bad style).
                }
            }
        }
    }

    // TODO Assignment A3
    public void move2(@NotNull Player player) {
        for (int i = 0; i < 2; i++) {
            move1(player);
        }
    }

    public void move3(@NotNull Player player){
        for (int i = 0; i < 3; i++) {
            move1(player);
        }
    }

    // TODO Assignment A3
    public void turnRight(@NotNull Player player) {
        if(player.getHeading()==Heading.EAST){
            player.setHeading(Heading.SOUTH);
        }
        else if(player.getHeading()==Heading.SOUTH){
            player.setHeading(Heading.WEST);
        }
        else if(player.getHeading()==Heading.WEST){
            player.setHeading(Heading.NORTH);
        }
        else if(player.getHeading()==Heading.NORTH){
            player.setHeading(Heading.EAST);
        }
    }

    // TODO Assignment A3
    public void turnLeft(@NotNull Player player) {
        if(player.getHeading()==Heading.EAST){
            player.setHeading(Heading.NORTH);
        }
        else if(player.getHeading()==Heading.NORTH){
            player.setHeading(Heading.WEST);
        }
        else if(player.getHeading()==Heading.WEST){
            player.setHeading(Heading.SOUTH);
        }
        else if(player.getHeading()==Heading.SOUTH){
            player.setHeading(Heading.EAST);
        }
    }

    public void moveBack(@NotNull Player player) {
        if (player.board == board) {
            Space space = player.getSpace();
            Heading curheading = player.getHeading();
            for (int i = 0; i < 2; i++) {
                turnLeft(player);
            }
            Heading heading = player.getHeading();
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                try {
                    moveToSpace(player, target, heading);
                    player.setHeading(curheading);
                } catch (ImpossibleMoveException e) {
                    // we don't do anything here  for now; we just catch the
                    // exception so that we do no pass it on to the caller
                    // (which would be very bad style).
                }

            }
        }
    }
    public void uTurn(@NotNull Player player){
        player.setHeading(player.getHeading().next().next());
    }

    /**
     * Then a player chooses one of the options on a option command card
     * that option is executed through this method, which then continues the
     * game after the interaction.
     * @param option the command cards specific option name
     * @author Anders
     */
    public void executeCommandOptionAndContinue(Command option){
        int step = board.getStep();
        Player currentPlayer = board.getCurrentPlayer();
        executeCommand(currentPlayer, option, step);
        board.setPhase(Phase.ACTIVATION);
        int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
        if (nextPlayerNumber < board.getPlayersNumber()) {
            board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
        } else {
            step++;
            if (step < Player.NO_REGISTERS) {
                makeProgramFieldsVisible(step);
                board.setStep(step);
                board.setCurrentPlayer(board.getPlayer(0));
            } else {
                for(Player player:board.getPlayers()){
                    Space space = player.getSpace();
                    for(FieldAction action: space.getActions()){
                        action.doAction(this, space);
                    }
                }
                gameWon();
                startProgrammingPhase();
            }
        }
    }

    /**
     * Ends the game then called, should only be then a player
     * have been to all the checkpoint on the course,
     * then shows a popup window with the winner
     * @author Anders
     */
    private void gameWon(){
        if(board.getPhase() == Phase.GAME_WON){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("GAME Finished");
            String winningPlayer = board.getPlayers().stream()
                    .filter(p -> p.getCeckPoint() == board.numberOfCheckPoints)
                    .findFirst().get().getName();
            alert.setContentText(winningPlayer + " has won the game");
            Optional<ButtonType> result = alert.showAndWait();
            appController.stopGame();
        }
    }

    /**
     * Checks weather a player is moving into a wall or not
     *
     * @param player The player whose movement is being checked.
     * @param newx The new x-coordinate after the movement.
     * @param newy The new y-coordinate after the movement.
     * @return {@code true} if the movement results in collision with a wall, {@code false} otherwise.
     * @throws NullPointerException if {@code player} is {@code null}.
     * @author Uffe
     */
    public boolean wallCollition(@NotNull Player player, int newx, int newy) {
        if (player.getSpace().x + 1 <= player.board.width) {
            return (player.getSpace().x + 1 == newx && player.getSpace().y == newy); // if wall right return true
        } else if (player.getSpace().x - 1 <= player.board.width) {
            return (player.getSpace().x - 1 == newx && player.getSpace().y == newy); // if wall left
        } else if (player.getSpace().y + 1 <= player.board.height) {
            return (player.getSpace().x == newx && player.getSpace().y + 1 == newy); // if wall down
        } else if (player.getSpace().y - 1 <= player.board.height) {
            return (player.getSpace().x == newx && player.getSpace().y - 1 == newy); // if wall up
        }
        return false;
    }

    void moveToSpace(@NotNull Player player, @NotNull Space space, @NotNull Heading heading) throws ImpossibleMoveException {
        assert board.getNeighbour(player.getSpace(), heading) == space; // make sure the move to here is possible in principle
        Player other = space.getPlayer();
        if (other != null){
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                // XXX Note that there might be additional problems with
                //     infinite recursion here (in some special cases)!
                //     We will come back to that!
                moveToSpace(other, target, heading);

                // Note that we do NOT embed the above statement in a try catch block, since
                // the thrown exception is supposed to be passed on to the caller

                assert target.getPlayer() == null : target; // make sure target is free now
            } else {
                throw new ImpossibleMoveException(player, space, heading);
            }
        }
        player.setSpace(space);
    }

    public void moveCurrentPlayerToSpace(Space space) {
        // TODO: Import or Implement this method. This method is only for debugging purposes. Not useful for the game.
    }

    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    private void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null) {
                    Command command = card.command;
                    if(command.isInteractive()){
                        board.setPhase(Phase.PLAYER_INTERACTION);
                        return;
                    }
                    executeCommand(currentPlayer, command, step);  // Correctly include the step as the register index
                }
                int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()) {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                } else {
                    step++;
                    if (step < Player.NO_REGISTERS) {
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    } else {
                        for(Player player:board.getPlayers()){
                            Space space = player.getSpace();
                            for(FieldAction action: space.getActions()){
                                action.doAction(this, space);
                            }
                        }
                        gameWon();
                        startProgrammingPhase();
                    }
                }
            } else {
                // this should not happen
                assert false : "Step index out of bounds";
            }
        } else {
            // this should not happen
            assert false : "Invalid game phase or current player is null";
        }
    }

    private void executeCommand(@NotNull Player player, Command command, int registerIndex) {
        if (command == Command.AGAIN) {
            if (registerIndex == 0) {
                System.out.println("AGAIN cannot be used in the first register.");
                return;  // Exit if 'AGAIN' is used in the first register
            }

            // Fetch the previous command and execute it if it's not null
            Command previousCommand = getPreviousCommand(player, registerIndex - 1);
            if (previousCommand != null && previousCommand != Command.DAMAGE) {
                executeCommand(player, previousCommand, registerIndex); // Recursively execute the previous command
            }
            return; // Exit after handling AGAIN command
        }

        // Execute other commands
        switch (command) {
            case MOVE1:
                move1(player);
                break;
            case TURNRIGHT:
                turnRight(player);
                break;
            case TURNLEFT:
                turnLeft(player);
                break;
            case MOVE2:
                move2(player);
                break;
            case MOVEBACK:
                moveBack(player);
                break;
            case MOVE3:
                move3(player);
                break;
            case POWER_UP:
                player.addEnergyCubes(1);
                break;
            case UTURN:
                uTurn(player);
            default:
                // DO NOTHING (for now)
                break;
        }
    }

    /**
     * This method retrieves the command from the card in the previous register of the player's program.
     * If the previous register index is valid (greater than or equal to 0), it gets the card from the previous register.
     * If the card exists, it returns the command of the card.
     * If the previous register index is invalid or there is no card in the previous register, it returns null.
     *
     * @param player The player whose program's previous command is to be retrieved.
     * @param previousRegisterIndex The index of the previous register in the player's program.
     * @return The command of the card in the previous register if it exists, null otherwise.
     * @author Aleksander Sonder, s185289
     */
    private Command getPreviousCommand(@NotNull Player player, int previousRegisterIndex) {
        if (previousRegisterIndex >= 0) {
            CommandCardField previousField = player.getProgramField(previousRegisterIndex);
            CommandCard previousCard = previousField.getCard();
            if (previousCard != null) {
                return previousCard.getCommand();
            }
        }
        return null; // No previous command or invalid index
    }
    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }


    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    CommandCardField field = player.getCardField(j);
                    field.setCard(generateRandomCommandCard());
                    field.setVisible(true);
                }
            }
        }
    }

    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    /**
     * A method called when no corresponding controller operation is implemented yet. This
     * should eventually be removed.
     */
    public void notImplemented() {
        // XXX just for now to indicate that the actual method is not yet implemented
        assert false;
    }


    class ImpossibleMoveException extends Exception {

        private Player player;
        private Space space;
        private Heading heading;

        public ImpossibleMoveException(Player player, Space space, Heading heading) {
            super("Move impossible");
            this.player = player;
            this.space = space;
            this.heading = heading;
        }
    }

}
