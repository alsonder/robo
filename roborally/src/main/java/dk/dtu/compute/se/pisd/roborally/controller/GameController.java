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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.tools.javac.Main;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.ApiTask;
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dk.dtu.compute.se.pisd.roborally.fileaccess.model.IP.ip;
import static dk.dtu.compute.se.pisd.roborally.view.PlayersView.playerViews;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class GameController {
    final public AppController appController;
    public static volatile Board board;
    public GameController(AppController appController, Board board) {
        this.appController = appController;
        GameController.board = board;
    }

    /*public void updatePlayersFromGameData(GameData gameData) {
        if (board == null) {
            throw new IllegalStateException("Board is not initialized.");
        }

        for (Player jsonPlayer : gameData.getPlayers()) {
            for (Player boardPlayer : board.getPlayers()) {
                if (boardPlayer.getName().equals(jsonPlayer.getName())) {
                    boardPlayer.setSpace(jsonPlayer.getSpace());
                    boardPlayer.setHeading(jsonPlayer.getHeading());
                    boardPlayer.setCheckPoint(jsonPlayer.getCheckPoint());
                }
            }
        }
    }*/
    public void updatePlayersFromGameData(GameData gameData) {
        for (Player jsonPlayer : gameData.getPlayers()) {
            for (Player boardPlayer : board.getPlayers()) {
                if (boardPlayer.getName().equals(jsonPlayer.getName())) {
                    boardPlayer.setSpace(jsonPlayer.getSpace());
                    boardPlayer.setHeading(jsonPlayer.getHeading());
                    boardPlayer.setCheckPoint(jsonPlayer.getCheckPoint());
                    boardPlayer.setTurn(jsonPlayer.isTurn());
                    boardPlayer.setSpawnSpace(jsonPlayer.getSpawnSpace());
                }
            }
        }
        board.setCurrentPlayer(gameData.getCurrentPlayer());
        board.setPhase(gameData.getPhase());
        board.setStep(gameData.getStep());
        board.setStepMode(gameData.isStepMode());
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
                    .filter(p -> p.getCheckPoint() == board.numberOfCheckPoints)
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
                field.setVisible(true);
            }
        }
    }

    public void finishProgrammingPhase() {
        // UFFE s
        //for (int i = 0; i < board.getPlayers().size(); i++) {

        //}
        // UFFE e
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    public void executePrograms() throws IOException {
        board.setStepMode(false);
        continuePrograms();
    }

    public void executeStep() throws IOException {
        // Continue the program execution
        board.setStepMode(true);
        continuePrograms();
        // Above code used to be at bottom
        // Load the existing JSON data
        /*ClassLoader classLoader = Main.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("activeGames/data.json");
        if (inputStream == null) {
            throw new IOException("File not found: activeGames/data.json");
        }
        Path tempFile = Files.createTempFile("data", ".json");
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

        String jsonData = new String(Files.readAllBytes(tempFile));
        System.out.println("Read JSON data: " + jsonData);

        // Send the current state to the server
        ClientController clientController = new ClientController(appController);
        clientController.putBoardJson(ip, jsonData);

        // Update the current player information in the JSON file
        try {
            Path filePath = Paths.get("src/main/resources/activeGames/data.json");
            ObjectMapper objectMapper = new ObjectMapper();
            GameController.GameData gameData = objectMapper.readValue(filePath.toFile(), GameController.GameData.class);

            // Determine the next player number
            Player currentPlayer = board.getCurrentPlayer();
            System.out.println("CP1:"+board.getCurrentPlayer());
            int nextPlayerNumber = (board.getPlayerNumber(currentPlayer) + 1) % board.getPlayersNumber();
            Player nextPlayer = board.getPlayer(nextPlayerNumber);

            // Update the current player in game data
            gameData.setCurrentPlayer(nextPlayer);

            // Save the updated game data back to the JSON file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), gameData);

            System.out.println("Updated JSON data: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(gameData));

            // Update the current player in the board
            board.setCurrentPlayer(nextPlayer);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to update JSON file: " + e.getMessage());
        }*/
        ObjectMapper mapper = new ObjectMapper();

        // Define the path to the JSON file within the resources directory
        Path resourceDirectory = Paths.get("roborally","src", "main", "resources","activeGames", "data.json");
        File jsonFile = resourceDirectory.toFile();
        System.out.println("Path to JSON file: " + jsonFile.getAbsolutePath());
        //C:\Users\hanso\Desktop\websocket-prototype\websocket-prototype\src\main\resources\mike.json

        try (FileInputStream fis = new FileInputStream(jsonFile)) {
            JsonNode rootNode = mapper.readTree(fis);

            // Print out the entire JSON for inspection
            //System.out.println("Entire JSON content:");
            //System.out.println(rootNode.toPrettyString());

            // Check specifically for 'players'
            if (rootNode.has("players")) {
                //System.out.println("'players' node exists.");
                JsonNode playersNode = rootNode.get("players");

                if (playersNode.isArray()) {
                    //System.out.println("'players' node is an array.");
                    ArrayNode players = (ArrayNode) playersNode;
                    //ObjectNode player1 = null;
                    int player_num=-1;
                    for (int i = 0; i < board.getPlayers().size(); i++)
                    {
                        System.out.println("players : "+board.getPlayer(i).getName() );
                        if (Objects.equals(board.getPlayer(i).getName(), PlayerInfo.PlayerNumber)) {
                            if (i+1==board.getPlayers().size()){
                                PlayerInfo.GlobalnextPlayer = mapper.valueToTree(board.getPlayer(0));
                                player_num = 0;
                                System.out.println("Pnum0: "+board.getPlayer(0).getName());
                            }
                            else {
                                PlayerInfo.GlobalnextPlayer = mapper.valueToTree(board.getPlayer(i+1));
                                player_num = i+1;
                                System.out.println("Pnum non0: "+board.getPlayer(i).getName());
                            }
                            break;
                       }
                    }
                    System.out.println("uf15s boardplayername of next player "+board.getPlayer(player_num).getName());

                    for (JsonNode player : players) {
                        if (player.isObject() && board.getPlayer(player_num).getName().equals(player.get("name").asText())) { //"Player 1"
                            //player1 = (ObjectNode) player;
                            PlayerInfo.GlobalnextPlayer = (ObjectNode) player;
                            System.out.println("Player found and accessed." + player.toString());
                            System.out.println("GloalNex PL : "+PlayerInfo.GlobalnextPlayer.toString());
                            break;
                        }
                    }

                    // MOD current to next player
                    if (PlayerInfo.GlobalnextPlayer != null) { //player1
                        ObjectNode currentPlayer = (ObjectNode) rootNode.path("current");
                        currentPlayer.setAll(PlayerInfo.GlobalnextPlayer); //player1
                        //System.out.println("Current player's values set to Player 1.");
                        System.out.println("Entire JSON content after:");
                        //System.out.println(rootNode.toPrettyString());
                        try (FileOutputStream fos = new FileOutputStream(jsonFile)) {
                            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, rootNode);
                            System.out.println("JSON file has been updated successfully.");
                        }catch (Exception e){
                            System.out.println("ERROR HERE rootnode not correct");
                        }
                        //
                        ClientController clientController = new ClientController(appController);
                        //System.out.println("rootnode:"+rootNode.toPrettyString());
                        clientController.putDataJson(ip, rootNode.toPrettyString());
                        //
                    } else {
                        System.err.println("Player 1 not found in the array.");
                    }
                } else {
                    System.err.println("'players' node is not an array. It is: " + playersNode.getNodeType());
                }
            } else {
                System.err.println("'players' node does not exist.");
            }
        } catch (IOException e) {
            System.err.println("Error processing JSON file: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //
        // Continue the program execution
        //board.setStepMode(true);
        //continuePrograms();
        //
    }




    private void continuePrograms() throws IOException {
        do {
            executeNextStep();
            //UFFE s4
            /*ClassLoader classLoader = Main.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("activeGames/data.json");
            if (inputStream == null) {throw new IOException("File not found: activeGames/data.json");}
            Path tempFile = Files.createTempFile("data", ".json");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            String jsonData = new String(Files.readAllBytes(tempFile));
            System.out.println("Read JSON data: " + jsonData);

            // UFFE s1
            //String jsonData = new String(Files.readAllBytes(Paths.get("src/main/resources/activeGames/data.json")));
            ClientController clientController = new ClientController(appController);
            clientController.putBoardJson(ip, jsonData);*/
            // UFFE s9

            // U16
            /*
            for (int i = 0; i < board.getPlayers().size(); i++) {
                if(Objects.equals(board.getPlayer(i).getName(), PlayerInfo.PlayerNumber)){
                    playerViews[i].setDisable(true);
                    if (board.getPlayer(i+1) != null){
                        playerViews[i+1].setDisable(false);
                    } else playerViews[0].setDisable(false);
                }
            }*/
            //U16

            // UFFE e9
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    private void executeNextStep() throws IOException {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null) {
                    Command command = card.command;
                    if (command.isInteractive()) {
                        board.setPhase(Phase.PLAYER_INTERACTION);
                        return;
                    }
                    executeCommand(currentPlayer, command, step);  // Correctly include the step as the register index
                }
                int nextPlayerNumber = (board.getPlayerNumber(currentPlayer) + 1) % board.getPlayersNumber();

                // updates the local json
                //l
                // Update the JSON file with the new turn values

                /*try {
                    Path filePath = Paths.get("src/main/resources/activeGames/data.json");
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    GameController.GameData gameData = objectMapper.readValue(filePath.toFile(), GameController.GameData.class);


                    // Set the turn of the current player to false
                    for (Player player : gameData.getPlayers()) {
                        if (player.getName().equals(currentPlayer.getName())) {
                            player.setTurn(false);
                        }
                    }

                    // Set the turn of the next player to true
                    Player nextPlayer = board.getPlayer(nextPlayerNumber);
                    for (Player player : gameData.getPlayers()) {
                        if (player.getName().equals(nextPlayer.getName())) {
                            player.setTurn(true);
                        }
                    }

                    // Update the current player in game data
                    gameData.setCurrentPlayer(nextPlayer);

                    // Save the updated game data back to the JSON file
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), gameData);


                    // Update the current player in the board
                    board.setCurrentPlayer(nextPlayer);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Failed to update JSON file: " + e.getMessage());
                }

                 */


                //l
                //
                if (step + 1 < Player.NO_REGISTERS) {
                    board.setStep(step + 1);
                    board.setCurrentPlayer(board.getPlayer(0)); // Keep the same player
                } else {
                    for (Player player : board.getPlayers()) {
                        Space space = player.getSpace();
                        for (FieldAction action : space.getActions()) {
                            action.doAction(this, space);
                        }
                    }
                    gameWon();
                    startProgrammingPhase();
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


    static class ImpossibleMoveException extends Exception {

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

    //UFFE s3
    public static class GameData {
        private List<Player> players;

        @JsonProperty("current")
        private Player currentPlayer;

        private Phase phase;
        private int step;
        private boolean stepMode;
        private List<Space> spawnSpaces;
        private String map;

        // Getters and setters...

        public List<Player> getPlayers() {
            return players;
        }

        public void setPlayers(List<Player> players) {
            this.players = players;
        }
        @JsonProperty("current")

        public Player getCurrentPlayer() {
            return currentPlayer;
        }
        @JsonProperty("current")

        public void setCurrentPlayer(Player currentPlayer) {
            this.currentPlayer = currentPlayer;
        }

        public Phase getPhase() {
            return phase;
        }

        public void setPhase(Phase phase) {
            this.phase = phase;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }

        public boolean isStepMode() {
            return stepMode;
        }

        public void setStepMode(boolean stepMode) {
            this.stepMode = stepMode;
        }

        public List<Space> getSpawnSpaces() {
            return spawnSpaces;
        }

        public void setSpawnSpaces(List<Space> spawnSpaces) {
            this.spawnSpaces = spawnSpaces;
        }

        public String getMap() {
            return map;
        }

        public void setMap(String map) {
            this.map = map;
        }
    }
    //UFFE se3


}

