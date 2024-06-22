package dk.dtu.compute.se.pisd.roborally.fileaccess.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.PlayerInfo;
import dk.dtu.compute.se.pisd.roborally.view.PlayerView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dk.dtu.compute.se.pisd.roborally.controller.GameController.board;
import static dk.dtu.compute.se.pisd.roborally.view.PlayersView.playerViews;

public class ApiTask implements Runnable {
    private final GameController gameController;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean shouldStop = false;

    public ApiTask(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void run() {
        scheduler.scheduleAtFixedRate(() -> {
            if (shouldStop) {
                scheduler.shutdown();
                return;
            }
            getApi();
        }, 0, 3, TimeUnit.SECONDS);
    }

    public void getApi() {
        System.out.println("Calling API...");

        try {
            URL url = new URL(PlayerInfo.URLPath + "/data.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

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
                Path customDir = projectRootDir.resolve("src/main/resources/activeGames");
                Files.createDirectories(customDir); // Ensure the directory exists
                Path filePath = customDir.resolve("data.json");

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                    writer.write(response.toString());
                    System.out.println("JSON saved to " + filePath.toString());
                } catch (Exception ignored) {}

                // Read and process JSON data
                ObjectMapper objectMapper = new ObjectMapper();
                GameController.GameData gameData = objectMapper.readValue(filePath.toFile(), GameController.GameData.class);
                System.out.println("Game data read successfully");

                // Update players and player views
                //uffes13

                for (int i = 0; i < board.getPlayers().size(); i++) {
                    if (board.getPlayer(i).getName()==localjson){ //board.getCurrentPlayer()){
                        board.setCurrentPlayer(board.getPlayer(i));
                    }
                }
                //
                gameController.updatePlayersFromGameData(gameData);
                updatePlayerViews(gameData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to call API: " + e.getMessage());
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