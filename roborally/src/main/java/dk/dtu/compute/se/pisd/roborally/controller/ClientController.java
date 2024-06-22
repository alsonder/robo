package dk.dtu.compute.se.pisd.roborally.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.ApiTask;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.PlayerInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static dk.dtu.compute.se.pisd.roborally.fileaccess.model.IP.ip;


public class ClientController {
    static public AppController appController = null;
    private static ApiTask apiTask;


    private static void startApiTask(GameController gameController) {
        if (apiTask != null) {
            apiTask.stopApiTask();
        }

        apiTask = new ApiTask(gameController);
        Thread apiThread = new Thread(apiTask);
        apiThread.start();
    }

    public ClientController(AppController appController) {
        ClientController.appController = appController;
    }

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static List<String> getListOfGames(String ip)  {

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://" + ip + ":8080/games"))
                .build();

        CompletableFuture<HttpResponse<String>> response =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        String result = null;
        try {
            result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        // Parse the JSON response into a list
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> gamesList;
        try {
            gamesList = objectMapper.readValue(result, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        return gamesList;
    }

    public static void startNewGame(String id){
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("id"))
                .uri(URI.create("http://"+ip+":8080/games/game1/board"))
                .build();

    }

    public void connectServer(String ip, String game) {
        /*try {
            URL url = new URL(PlayerInfo.URLPath+game+"/data");
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

                System.out.println(response.toString());

                // GET BOARD FROM SERVER *GET*
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("roborally/src/main/resources/activeGames/data.json"))) {
                    writer.write(response.toString());
                    System.out.println("JSON saved to " + "roborally/src/main/resources/activeGames/data.json");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Failed to write JSON file: " + e.getMessage());
                }

                // SEND BOARD TO SERVER *PUT*
                String jsonData = new String(Files.readAllBytes(Paths.get("roborally/src/main/resources/activeGames/data.json")));
                ClientController clientController = new ClientController(appController);
                clientController.putBoardJson(ip, jsonData);
                //2.2
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public void putBoardJson(String ip, String jsonData) {
        try {
            URL url = new URL(PlayerInfo.URLPath+"/board");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("PUT request successful.");
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println(response.toString());
            } else {
                System.out.println("PUT request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void putDataJson(String ip, String jsonData) {
        try {
            URL url = new URL(PlayerInfo.URLPath+"/data");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("PUT request successful.");
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println(response.toString());
            } else {
                System.out.println("PUT request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*
    public static List<String> getListOfPlayers(String game){
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://" + ip+ ":8080/games/" + game + "/playersv"))
                    .build();

            CompletableFuture<HttpResponse<String>> response =
                    httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

            String result = null;
            try {
                result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }

            // Parse the JSON response to extract player names
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> playerNames;
            try {
                JsonNode root = objectMapper.readTree(result);
                playerNames = objectMapper.convertValue(root.get("players"), new TypeReference<List<JsonNode>>() {})
                        .stream()
                        .map(node -> node.get("name").asText())
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse players.json", e);
            }

        return playerNames;
    }*/

    public static List<String> getListOfPlayers(String game) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://" + ip + ":8080/games/" + game + "/players"))
                .build();

        CompletableFuture<HttpResponse<String>> response =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        String result = null;
        try {
            result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        // Check if the result is empty
        if (result.trim().isEmpty()) {
            return List.of(); // Return an empty list if the result is empty
        }

        // Split the response by comma and newline to extract player names
        List<String> playerNames;
        try {
            playerNames = Arrays.stream(result.split(",\n"))
                    .map(String::trim)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse players.txt", e);
        }

        return playerNames;
    }



    public static String addPlayer(String game) {
        // Get the current list of players
        List<String> players = getListOfPlayers(game);

        // Determine the next player name
        int nextPlayerNumber = players.size() + 1;
        String newPlayerName = "Player " + nextPlayerNumber;
        PlayerInfo.URLPath = "http://"+ip+":8080/games/"+game;
        PlayerInfo.PlayerNumber = newPlayerName;
        // Initialize the board and game controller
        Board board = LoadBoard.loadBoard("defaultboard"); // Load the board here
        GameController gameController = new GameController(appController, board);

        // Initialize the board and game controller
// Start the ApiTask thread
        startApiTask(gameController);


// Start the ApiTask threadstartApiTask(gameController);


        // Create the POST request to add the new player
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("playerName=" + newPlayerName))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create("http://" + ip + ":8080/games/" + game + "/players"))
                .build();

        CompletableFuture<HttpResponse<String>> response =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        String result = null;
        try {
            result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        return newPlayerName;
    }
}