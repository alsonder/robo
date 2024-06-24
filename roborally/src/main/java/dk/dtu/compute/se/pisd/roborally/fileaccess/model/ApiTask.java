package dk.dtu.compute.se.pisd.roborally.fileaccess.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.PlayerInfo;
import dk.dtu.compute.se.pisd.roborally.view.PlayerView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dk.dtu.compute.se.pisd.roborally.controller.GameController.board;
import static dk.dtu.compute.se.pisd.roborally.view.PlayersView.playerViews;

public class ApiTask  { // implements Runnable
    private final GameController gameController;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean shouldStop = false;

    public ApiTask(GameController gameController) {
        this.gameController = gameController;
    }

    /*@Override
    public void run() {
        scheduler.scheduleAtFixedRate(() -> {
            if (shouldStop) {
                scheduler.shutdown();
                return;
            }
            getApi();
        }, 0, 3, TimeUnit.SECONDS);
    }

     */

    public void getApi() {
        System.out.println("Calling API...");

        HttpURLConnection connection = null;
        try {

            //URL url = new URL(PlayerInfo.URLPath + "/data.json");
            URL url = new URL(PlayerInfo.URLPath + "/data.json?nocache=" + System.currentTimeMillis());
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
                } catch (Exception e) {
                    throw new RuntimeException(e);
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

                } catch (IOException e) {
                    System.err.println("Failed to read or parse the JSON file: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("An error occurred: " + e.getMessage());
                }

                //
                
                //
                gameController.updatePlayersFromGameData(gameData);
                updatePlayerViews(gameData);

            }

        } catch (Exception e) {
            System.err.println("Failed to call API: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect(); // Ensures the connection is closed
            }
        }
    }

    private void updatePlayerViews(GameController.GameData gameData) {
        // Disable all player views initially
        for (PlayerView playerView : playerViews) {
            playerView.setDisable(true);
        }

        // Enable the view of the current player
        if (gameData.getCurrentPlayer() != null) {
            int currentPlayerNumber = getPlayerNumber(gameData.getCurrentPlayer().getName());
            if (currentPlayerNumber != -1) {
                playerViews[currentPlayerNumber].setDisable(false);
                //System.out.println("Current player turn: " + gameData.getCurrentPlayer().getName());
            }
        }
    }


    // Helper method to get the player number based on the player name
    private int getPlayerNumber(String playerName) {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            if (board.getPlayer(i).getName().equals(playerName)) {
                return i;
            }
        }
        return -1; // Player not found
    }




    public void stopApiTask() {
        shouldStop = true;
    }
}