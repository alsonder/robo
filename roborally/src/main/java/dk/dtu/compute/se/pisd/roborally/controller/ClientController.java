package dk.dtu.compute.se.pisd.roborally.controller;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;



public class ClientController {
    final public AppController appController;

    public ClientController(AppController appController) {
        this.appController = appController;
    }

    public void connectServer(String ip) {
        try {
            URL url = new URL("http://" + ip + ":8080/games/game1/board");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            //2.1
            URL url2 = new URL("http://" + ip + ":8080/games/game1/players");
            HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
            connection2.setRequestMethod("GET");
            //2.2
            //3.1
            URL url3 = new URL("http://" + ip + ":8080/games");
            HttpURLConnection connection3 = (HttpURLConnection) url3.openConnection();
            connection3.setRequestMethod("GET");
            //3.2

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                //2.1
                BufferedReader in2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
                String inputLine2;
                StringBuilder response2 = new StringBuilder();
                //2.2
                //3.1
                BufferedReader in3 = new BufferedReader(new InputStreamReader(connection3.getInputStream()));
                String inputLine3;
                StringBuilder response3 = new StringBuilder();
                //3.2



                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                //2.1
                while ((inputLine2 = in2.readLine()) != null) {
                    response2.append(inputLine2);
                }
                in2.close();
                //2.2
                //3.1
                while ((inputLine3 = in3.readLine()) != null) {
                    response3.append(inputLine3);
                }
                in3.close();
                //3.2

                System.out.println(response.toString());
                //2.1

                System.out.println("\r\n----------------------------------------------------------------------------------------\r\n");
                System.out.println(response2.toString());
                System.out.println(response3);

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
}