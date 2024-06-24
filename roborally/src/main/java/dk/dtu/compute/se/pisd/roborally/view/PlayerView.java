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
package dk.dtu.compute.se.pisd.roborally.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.ApiTask;
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static dk.dtu.compute.se.pisd.roborally.controller.GameController.board;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class PlayerView extends Tab implements ViewObserver {

    private Player player;

    private VBox top;

    private Label programLabel;
    private GridPane programPane;
    private Label cardsLabel;
    private GridPane cardsPane;

    private CardFieldView[] programCardViews;
    private CardFieldView[] cardViews;

    private VBox buttonPanel;

    //
    private  Button updateButton;
    //

    private Button finishButton;
    private Button executeButton;
    private Button stepButton;

    private VBox playerInteractionPanel;

    private GameController gameController;

    private Label energyCubesLabel;

    public PlayerView(@NotNull GameController gameController, @NotNull Player player) {
        super(player.getName());
        this.setStyle("-fx-text-base-color: " + player.getColor() + ";");

        top = new VBox();
        this.setContent(top);

        energyCubesLabel = new Label("Energy Cubes: " + player.getEnergyCubes());
        energyCubesLabel.getStyleClass().add("energy-cubes-label");

        this.gameController = gameController;
        this.player = player;

        programLabel = new Label("Program");

        programPane = new GridPane();
        programPane.setVgap(2.0);
        programPane.setHgap(2.0);
        programCardViews = new CardFieldView[Player.NO_REGISTERS];
        for (int i = 0; i < Player.NO_REGISTERS; i++) {
            CommandCardField cardField = player.getProgramField(i);
            if (cardField != null) {
                programCardViews[i] = new CardFieldView(gameController, cardField);
                programPane.add(programCardViews[i], i, 0);
            }
        }

        // XXX  the following buttons should actually not be on the tabs of the individual
        //      players, but on the PlayersView (view for all players). This should be
        //      refactored.
        updateButton = new Button("Update");
        updateButton.setOnAction( e -> {System.out.println("Calling API...");

            HttpURLConnection connection = null;
            try {
                //
                //
                //
                HttpClient client = HttpClient.newHttpClient();
                URL url = new URL(PlayerInfo.URLPath + "/data");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url.toString()))
                        .header("Cache-Control", "no-cache, no-store, must-revalidate")
                        .GET()
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        System.out.println("Response received successfully.");
                        try {
                            Path projectRootDir = Paths.get(System.getProperty("user.dir"));
                            Path customDir = projectRootDir.resolve("roborally/src/main/resources/activeGames");
                            Files.createDirectories(customDir); // Ensure the directory exists
                            Path filePath = customDir.resolve("data");

                            Files.writeString(filePath, response.body());
                            System.out.println("JSON saved to " + filePath);
                            System.out.println("saved:"+response.body());
                        } catch (IOException f) {
                            System.err.println("Failed to save data to file: " + f.getMessage());
                        }
                    }

                 else {
                        System.out.println("Failed to get a successful response: Status code = " + response.statusCode());
                    }
                } catch (IOException | InterruptedException f) {
                    System.err.println("Error during HTTP call: " + f.getMessage());
                }
                //
                //
                //

                //URL url = new URL(PlayerInfo.URLPath + "/data.json");
                //URL url = new URL(PlayerInfo.URLPath + "/data.json?nocache=" + System.currentTimeMillis());
                System.out.println("getting from "+url);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // U17
                connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                //U17

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Save JSON response to file
                    Path projectRootDir = Paths.get(System.getProperty("user.dir"));
                    Path customDir = projectRootDir.resolve("roborally/src/main/resources/activeGames");
                    //Files.createDirectories(customDir); // Ensure the directory exists
                    Path filePath = customDir.resolve("data.json");

                    // U15
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                        writer.write(response.toString());
                        //System.out.println("JSON saved to " + filePath.toString());
                        //System.out.println(response.toString());
                    } catch (Exception f) {
                        throw new RuntimeException();
                    }
                    // U15

                    // Read and process JSON data
                    ObjectMapper objectMapper = new ObjectMapper();
                    GameController.GameData gameData = objectMapper.readValue(filePath.toFile(), GameController.GameData.class);
                    System.out.println("Game data read successfully");
                    //System.out.println(gameData);

                    // Update players and player views
                    //uffes13
                    File jsonFile = Paths.get("roborally", "src", "main", "resources", "activeGames", "data.json").toFile().getAbsoluteFile();

                    // Create an ObjectMapper instance
                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        // Load the JSON file
                        JsonNode rootNode = mapper.readTree(jsonFile);

                        // Access the current player
                        JsonNode currentPlayer = rootNode.get("current");

                        if (currentPlayer != null) {
                            // Extract the name of the current player
                            JsonNode nameNode = currentPlayer.get("name");
                            if (nameNode != null) {
                                String currentPlayerName = nameNode.asText();
                                // Output the name of the current player
                                System.out.println("Current Player's Name: " + currentPlayerName);
                                for (int i = 0; i < board.getPlayers().size(); i++) {
                                    if (Objects.equals(board.getPlayer(i).getName(), currentPlayerName)) { //board.getCurrentPlayer()){
                                        if (i == board.getPlayers().size() - 1) {
                                            board.setCurrentPlayer(board.getPlayer(0));
                                            PlayerInfo.PlayerNumber = board.getPlayer(0).getName();
                                            gameData.setCurrentPlayer(board.getPlayer(0));
                                        } else {
                                            board.setCurrentPlayer(board.getPlayer(i + 1));
                                            PlayerInfo.PlayerNumber = board.getPlayer(i+1).getName();
                                            gameData.setCurrentPlayer(board.getPlayer(i + 1));
                                        }
                                    }
                                }
                            } else {
                                System.out.println("Name field is missing");
                            }
                        } else {
                            System.out.println("Current player data is missing");
                        }

                    } catch (IOException f) {
                        System.err.println("Failed to read or parse the JSON file: " + f.getMessage());
                    } catch (Exception f) {
                        System.err.println("An error occurred: " + f.getMessage());
                    }

                    //

                    //
                    gameController.updatePlayersFromGameData(gameData);
                    updatePlayerViews(gameData);

                }

            } catch (Exception f) {
                System.err.println("Failed to call API: " + f.getMessage());
                f.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect(); // Ensures the connection is closed
                }
            }}); // get from server

        finishButton = new Button("Finish Programming");
        finishButton.setOnAction( e -> gameController.finishProgrammingPhase());

        executeButton = new Button("Execute Program");
        executeButton.setOnAction( e-> {
            try {
                gameController.executePrograms();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        stepButton = new Button("Execute Current Register");
        stepButton.setOnAction( e-> {
            try {
                gameController.executeStep();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        buttonPanel = new VBox(updateButton, finishButton, executeButton, stepButton);
        buttonPanel.setAlignment(Pos.CENTER_LEFT);
        buttonPanel.setSpacing(3.0);
        // programPane.add(buttonPanel, Player.NO_REGISTERS, 0); done in update now

        playerInteractionPanel = new VBox();
        playerInteractionPanel.setAlignment(Pos.CENTER_LEFT);
        playerInteractionPanel.setSpacing(3.0);

        cardsLabel = new Label("Command Cards");
        cardsPane = new GridPane();
        cardsPane.setVgap(2.0);
        cardsPane.setHgap(2.0);
        cardViews = new CardFieldView[Player.NO_CARDS];
        for (int i = 0; i < Player.NO_CARDS; i++) {
            CommandCardField cardField = player.getCardField(i);
            if (cardField != null) {
                cardViews[i] = new CardFieldView(gameController, cardField);
                cardsPane.add(cardViews[i], i, 0);
            }
        }

        top.getChildren().add(programLabel);
        top.getChildren().add(programPane);
        top.getChildren().add(cardsLabel);
        top.getChildren().add(cardsPane);
        top.getChildren().add(energyCubesLabel);

        if (player.board != null) {
            player.board.attach(this);
            update(player.board);
        }
    }

    private void updatePlayerViews(GameController.GameData gameData) {
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == player.board) {
            energyCubesLabel.setText("Energy Cubes: " + player.getEnergyCubes() + "\n" +
                "Check Points: " + player.getCheckPoint());
            for (int i = 0; i < Player.NO_REGISTERS; i++) {
                CardFieldView cardFieldView = programCardViews[i];
                if (cardFieldView != null) {
                    if (player.board.getPhase() == Phase.PROGRAMMING ) {
                        cardFieldView.setBackground(CardFieldView.BG_DEFAULT);
                    } else {
                        if (i < player.board.getStep()) {
                            cardFieldView.setBackground(CardFieldView.BG_DONE);
                        } else if (i == player.board.getStep()) {
                            if (player.board.getCurrentPlayer() == player) {
                                cardFieldView.setBackground(CardFieldView.BG_ACTIVE);
                            } else if (player.board.getPlayerNumber(player.board.getCurrentPlayer()) > player.board.getPlayerNumber(player)) {
                                cardFieldView.setBackground(CardFieldView.BG_DONE);
                            } else {
                                cardFieldView.setBackground(CardFieldView.BG_DEFAULT);
                            }
                        } else {
                            cardFieldView.setBackground(CardFieldView.BG_DEFAULT);
                        }
                    }
                }
            }

            if (player.board.getPhase() != Phase.PLAYER_INTERACTION) {
                if (!programPane.getChildren().contains(buttonPanel)) {
                    programPane.getChildren().remove(playerInteractionPanel);
                    programPane.add(buttonPanel, Player.NO_REGISTERS, 0);
                }
                switch (player.board.getPhase()) {
                    case INITIALISATION:
                        finishButton.setDisable(true);
                        // XXX just to make sure that there is a way for the player to get
                        //     from the initialization phase to the programming phase somehow!
                        executeButton.setDisable(false);
                        stepButton.setDisable(true);
                        break;

                    case PROGRAMMING:
                        finishButton.setDisable(false);
                        executeButton.setDisable(true);
                        stepButton.setDisable(true);
                        break;

                    case ACTIVATION:
                        finishButton.setDisable(true);
                        executeButton.setDisable(false);
                        stepButton.setDisable(false);
                        break;

                    default:
                        finishButton.setDisable(true);
                        executeButton.setDisable(true);
                        stepButton.setDisable(true);
                }


            } else {
                if (!programPane.getChildren().contains(playerInteractionPanel)) {
                    programPane.getChildren().remove(buttonPanel);
                    programPane.add(playerInteractionPanel, Player.NO_REGISTERS, 0);
                }
                playerInteractionPanel.getChildren().clear();

                if (player.board.getCurrentPlayer() == player) {
                    Command command = player.getProgramField(player.board.getStep()).getCard().command;
                    if (command.isInteractive()){
                        for(Command option : command.getOptions()){
                            Button optionButton = new Button(option.displayName);
                            optionButton.setOnAction( e -> gameController.executeCommandOptionAndContinue(option));
                            optionButton.setDisable(false);
                            playerInteractionPanel.getChildren().add(optionButton);
                        }
                    }
                }
            }
        }
    }

}
