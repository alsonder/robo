package dk.dtu.compute.se.pisd.roborally.controller;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static dk.dtu.compute.se.pisd.roborally.fileaccess.model.IP.ip;


public class ClientController {
    final public AppController appController;

    public ClientController(AppController appController) {
        this.appController = appController;
    }

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

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

        // Split the result by commas and return as a list
        List<String> gamesList = Arrays.asList(result.split(","));

        return gamesList;
    }

    public static void startNewGame(String id){
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("id"))
                .uri(URI.create("http://"+ip+":8080/games/game1/board"))
                .build();

    }

    public void connectServer(String ip) {
        try {
            URL url = new URL("http://" + ip + ":8080/games/game1/board");
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
}