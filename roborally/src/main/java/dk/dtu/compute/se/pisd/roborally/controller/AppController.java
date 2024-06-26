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

import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;

import dk.dtu.compute.se.pisd.roborally.RoboRally;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.Board;

import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.PlayerInfo;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
//import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class AppController implements Observer {

    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);
    final private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");

    final private RoboRally roboRally;

    private GameController gameController;

    private ClientController clientController;

    private String gameName = "unnamed";
    public AppController(@NotNull RoboRally roboRally) {
        this.roboRally = roboRally;
    }




    public void joinServer(){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Insert Ip of server");
        Optional<String> result = dialog.showAndWait();
        ClientController clientController = new ClientController(this);

        if(result.isPresent()){
            clientController.connectServer(result.get());
            joinedServerChoices(result.get());
        }
    }
    private void joinedServerChoices(String ip) {
        // Custom dialog setup
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Server is up and running!");
        dialog.setHeaderText("Join an active game:");

        // Create the content area with games list and button
        VBox vbox = new VBox(10);
        Label label = new Label("Select a game to join:");

        // List of games as example
        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(ClientController.getListOfGames(ip));
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double-click
                String selectedItem = listView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Handle the item selection (e.g., join the game)
                    joinGame(selectedItem);
                    dialog.close();
                }
            }
        });

        vbox.getChildren().addAll(label, listView);
        dialog.getDialogPane().setContent(vbox);

        // Add button type
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        // Show dialog and handle results
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String selectedGame = listView.getSelectionModel().getSelectedItem();
            if (selectedGame != null) {
                joinGame(selectedGame);
            }
        } else {
            System.out.println("No selection made or dialog closed.");
        }
    }

    private void joinGame(String game) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setHeaderText("Players in " + game);

        // Create the content area with games list and button
        VBox vbox = new VBox(6);


        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(ClientController.getListOfPlayers(game));

        Button joinLobbyButton = new Button("Join Lobby");
        joinLobbyButton.setOnAction(e -> {
            try {
                clientController.addPlayer(game);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });


        Button refreshLobbyButton = new Button("Refresh Lobby");
        refreshLobbyButton.setOnAction(e -> {clientController.getListOfPlayers(game);
            listView.getItems().clear();
            listView.getItems().addAll(ClientController.getListOfPlayers(game));
        });

        Button startGameButton = new Button("Start Game");
        startGameButton.setOnAction(e -> {

            PlayerInfo.NumberOfPlayers = clientController.getListOfPlayers(game).size();
            startGame();
            dialog.close();

        });

        vbox.getChildren().addAll(listView, startGameButton,refreshLobbyButton,joinLobbyButton);
        dialog.getDialogPane().setContent(vbox);

        // Add button type
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        // Show the dialog and wait for user interaction
        dialog.showAndWait();
        dialog.close();
    }



    public void startGame(){

        if (PlayerInfo.NumberOfPlayers >= 2) {
            if (gameController != null) {
                // The UI should not allow this, but in case this happens anyway.
                // give the user the option to save the game or abort this operation!
                if (!stopGame()) {
                    return;
                }
            }

            // XXX the board should eventually be created programmatically or loaded from a file
            //     here we just create an empty board with the required number of players.
            Board board = LoadBoard.loadBoard("defaultboard");

            gameController = new GameController(this, board);
            int no = PlayerInfo.NumberOfPlayers;
            board.setSpawnSpacesDefault(no);
            for (int i = 0; i < no; i++) {
                Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
                board.addPlayer(player);
                player.setSpawnSpace(board.getSpawnSpaces().get(i));
                player.setSpace(player.getSpawnSpace());

            }

            // XXX: V2
            // board.setCurrentPlayer(board.getPlayer(0));
            gameController.startProgrammingPhase();

            roboRally.createBoardView(gameController);
        }
    }


    public void newGame() {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(PLAYER_NUMBER_OPTIONS.get(0), PLAYER_NUMBER_OPTIONS);
        dialog.setTitle("Player number");
        dialog.setHeaderText("Select number of players");
        Optional<Integer> result = dialog.showAndWait();

        if (result.isPresent()) {
            if (gameController != null) {
                // The UI should not allow this, but in case this happens anyway.
                // give the user the option to save the game or abort this operation!
                if (!stopGame()) {
                    return;
                }
            }

            // XXX the board should eventually be created programmatically or loaded from a file
            //     here we just create an empty board with the required number of players.
            Board board = LoadBoard.loadBoard("defaultboard");

            gameController = new GameController(this, board);
            int no = result.get();
            board.setSpawnSpacesDefault(no);
            for (int i = 0; i < no; i++) {
                Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
                board.addPlayer(player);
                player.setSpawnSpace(board.getSpawnSpaces().get(i));
                player.setSpace(player.getSpawnSpace());

            }

            // XXX: V2
            // board.setCurrentPlayer(board.getPlayer(0));
            gameController.startProgrammingPhase();

            roboRally.createBoardView(gameController);
        }
    }

    private Optional<ButtonType> showAlert(String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setHeaderText(message);
        return alert.showAndWait();
    }
    private String selectBoard(String[] boards) {

        if (boards == null || boards.length == 0) {
            showAlert("No tracks");
            return null;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(boards[0], boards);
        dialog.setTitle("Track selection");
        dialog.setHeaderText("Pick a track");
        Optional<String> result = dialog.showAndWait();
        String track = "";
        if (result.isPresent()) {
            track = result.get();
            System.out.println("Track chosen: " + track);
        }
        return track;
    }

    private Optional<String> showSaveGameDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save game");
        dialog.setHeaderText("Save As");
        return dialog.showAndWait();
    }
    public void saveGame() {
        Optional<String> result = Optional.empty();
        if(gameName.equals("unnamed")) {
            do {
                if(showAlert("Please enter a name for the saved game or cancel the save").get() != ButtonType.OK) return;
                result = showSaveGameDialog();
                //additional break condition if the user doesn't want to save.
                if(result.isEmpty()) return;
            } while(result.get().equals(""));
            gameName = result.get();
        }

        if (Arrays.asList(LoadBoard.getActiveGames()).contains(gameName)) {
            if(showAlert("Do you want to overwrite " + gameName + "?").get() != ButtonType.OK) return;
        }

        LoadBoard.saveCurrentGame(this.gameController.board, gameName);
        System.out.println("Saved as " + gameName);

    }



    public void loadGame() {

        String track = selectBoard(LoadBoard.getActiveGames());

        if (track == null || track.isEmpty()) {
            showAlert("Could not load game");
            return;
        }

        Board board = LoadBoard.loadActiveBoard(track);
        if (board == null) {
            showAlert("Could not load game");
            return;
        }

        gameController = new GameController(this, board);
        roboRally.createBoardView(gameController);
        gameName = track;
    }

    /**
     * Stop playing the current game, giving the user the option to save
     * the game or to cancel stopping the game. The method returns true
     * if the game was successfully stopped (with or without saving the
     * game); returns false, if the current game was not stopped. In case
     * there is no current game, false is returned.
     *
     * @return true if the current game was stopped, false otherwise
     */
    public boolean stopGame() {
        if (gameController != null) {

            // here we save the game (without asking the user).
            saveGame();

            gameController = null;
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }

    public void exit() {
        if (gameController != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Exit RoboRally?");
            alert.setContentText("Are you sure you want to exit RoboRally?");
            Optional<ButtonType> result = alert.showAndWait();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return; // return without exiting the application
            }
        }

        // If the user did not cancel, the RoboRally application will exit
        // after the option to save the game
        if (gameController == null || stopGame()) {
            Platform.exit();
        }
    }

    public boolean isGameRunning() {
        return gameController != null;
    }


    @Override
    public void update(Subject subject) {
        // XXX do nothing for now
    }

}
