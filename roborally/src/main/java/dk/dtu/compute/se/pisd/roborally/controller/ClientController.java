package dk.dtu.compute.se.pisd.roborally.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.PlayerInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
    static public AppController appController;

    public ClientController(AppController appController) {
        this.appController = appController;
    }

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static List<String> getListOfGames(String ip) {

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
            gamesList = objectMapper.readValue(result, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        return gamesList;
    }

    public static void startNewGame(String id) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("id"))
                .uri(URI.create("http://" + "10.209.140.39" + ":8080/games/game1/board"))
                .build();

    }

    public void connectServer(String ip) {
        try {
            URL url = new URL("http://" + ip + ":8080/games/game1/board");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            //2.1


            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                //2.1


                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                //2.1


                System.out.println(response.toString());
                //2.1

                // GET BOARD FROM SERVER *GET*
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/boards/test.json"))) {
                    writer.write(response.toString());
                    System.out.println("JSON saved to " + "src/main/resources/boards/test.json");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Failed to write JSON file: " + e.getMessage());
                }

                // SEND BOARD TO SERVER *PUT*
                String jsonData = new String(Files.readAllBytes(Paths.get("src/main/resources/boards/test.json")));
                ClientController clientController = new ClientController(appController);
                clientController.putBoardJson(ip, jsonData);
                //2.2
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void putBoardJson(String ip, String jsonData) {
        try {
            URL url = new URL("http://" + ip + ":8080/games/game1/board");
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

    public static String addPlayer(String game) throws IOException {
        // Get the current list of players
        List<String> players = getListOfPlayers(game);

        // Determine the next player name
        int nextPlayerNumber = players.size() + 1;
        String newPlayerName = "Player " + nextPlayerNumber;
        PlayerInfo.URLPath = "http://" + ip + ":8080/games/" + game;
        PlayerInfo.PlayerNumber = newPlayerName;
        //u18
        String startJson = ("""
                {
                  "players" : [ {
                    "name" : "Player 1",
                    "color" : "red",
                    "space" : {
                      "x" : 0,
                      "y" : 0
                    },
                    "heading" : "SOUTH",
                    "energyCubes" : 0,
                    "spawnSpace" : {
                      "x" : 0,
                      "y" : 0
                    },
                    "checkPoint" : 0
                  }, {
                    "name" : "Player 2",
                    "color" : "green",
                    "space" : {
                      "x" : 0,
                      "y" : 1
                    },
                    "heading" : "SOUTH",
                    "energyCubes" : 0,
                    "spawnSpace" : {
                      "x" : 0,
                      "y" : 1
                    },
                    "checkPoint" : 0
                  }\s""");
        if (getListOfPlayers(game).size() >= 2) {
            startJson += ("""
                    , {
                        "name" : "Player 3",
                        "color" : "red",
                        "space" : {
                          "x" : 0,
                          "y" : 2
                        },
                        "heading" : "SOUTH",
                        "energyCubes" : 0,
                        "spawnSpace" : {
                          "x" : 0,
                          "y" : 2
                        },
                        "checkPoint" : 0
                      }""");
        }
        if (getListOfPlayers(game).size() >= 3) {
            startJson += ("""
                    , {
                        "name" : "Player 4",
                        "color" : "red",
                        "space" : {
                          "x" : 0,
                          "y" : 3
                        },
                        "heading" : "SOUTH",
                        "energyCubes" : 0,
                        "spawnSpace" : {
                          "x" : 0,
                          "y" : 3
                        },
                        "checkPoint" : 0
                      }""");
        }
        if (getListOfPlayers(game).size() >= 4) {
            startJson += ("""
                    , {
                        "name" : "Player 5",
                        "color" : "red",
                        "space" : {
                          "x" : 0,
                          "y" : 4
                        },
                        "heading" : "SOUTH",
                        "energyCubes" : 0,
                        "spawnSpace" : {
                          "x" : 0,
                          "y" : 4
                        },
                        "checkPoint" : 0
                      }""");
        }
        if (getListOfPlayers(game).size() >= 5) {
            startJson += ("""
                    , {
                        "name" : "Player 6",
                        "color" : "red",
                        "space" : {
                          "x" : 0,
                          "y" : 5
                        },
                        "heading" : "SOUTH",
                        "energyCubes" : 0,
                        "spawnSpace" : {
                          "x" : 0,
                          "y" : 5
                        },
                        "checkPoint" : 0
                      }""");
        }
        startJson += (
                """
                        ],
                          "current" : {
                            "name" : "Player 1",
                            "color" : "red",
                            "space" : {
                              "x" : 0,
                              "y" : 0
                            },
                            "heading" : "SOUTH",
                            "energyCubes" : 0,
                            "spawnSpace" : {
                              "x" : 0,
                              "y" : 0
                            },
                            "checkPoint" : 0
                          },
                          "phase" : "PROGRAMMING",
                          "step" : 0,
                          "stepMode" : true,
                          "spawnSpaces" : [ {
                            "x" : 0,
                            "y" : 0
                          }, {
                            "x" : 0,
                            "y" : 1
                          }, {
                            "x" : 0,
                            "y" : 2
                          }, {
                            "x" : 0,
                            "y" : 3
                          } ],
                          "map" : "defaultboard"
                        }""");
        try {
            putDataJson(ip, startJson);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Initialize the board and game controller
        Board board = LoadBoard.loadBoard("defaultboard"); // Load the board here
        GameController gameController = new GameController(appController, board);

        // Initialize the board and game controller
// Start the ApiTask thread
        //startApiTask(gameController);


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

    public static void putDataJson(String ip, String jsonData) throws IOException, InterruptedException {
        try {
            System.out.println("Connecting to URL: " + PlayerInfo.URLPath + "/data");
            URL url = new URL(PlayerInfo.URLPath + "/data");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            System.out.println("Sending JSON data: " + jsonData);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);

            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("Server response: " + response.toString());
            } else {
                System.out.println("PUT request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error during HTTP PUT");
            e.printStackTrace();
        }
    }

    public void sendPlayersLocation() {

    }

    public static String getBoardJSON() {

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
                    /*
                    Path projectRootDir = Paths.get(System.getProperty("user.dir"));
                    Path customDir = projectRootDir.resolve("roborally/src/main/resources/activeGames");
                    Files.createDirectories(customDir); // Ensure the directory exists
                    Path filePath = customDir.resolve("data");

                    Files.writeString(filePath, response.body());

                    System.out.println("JSON saved to " + filePath);
                     */
                    System.out.println("saved:" + response.body());
                    return response.body();

                } else {
                    System.out.println("Failed to get a successful response: Status code = " + response.statusCode());
                }
            } catch (IOException | InterruptedException f) {
                System.err.println("Error during HTTP call: " + f.getMessage());
            }
            //
            //
            //


        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}