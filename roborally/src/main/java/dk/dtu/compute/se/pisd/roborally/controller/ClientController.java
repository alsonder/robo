package dk.dtu.compute.se.pisd.roborally.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientController {


    final public AppController appController;

    public ClientController(AppController appController) {
        this.appController = appController;
    }
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public void connectServer(String ip) {

    }

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
                .uri(URI.create("http://"+"10.209.103.216"+":8080/games/game1/board"))
                .build();

    }
}
