package dk.dtu.compute.se.pisd.roborally.fileaccess.model;


import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Player;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static dk.dtu.compute.se.pisd.roborally.controller.GameController.board;
import static dk.dtu.compute.se.pisd.roborally.fileaccess.model.IP.ip;

public class ApiTask implements Runnable {
    private volatile boolean shouldStop = false;


    public ApiTask() {
    }





    public void getApi() {
        // Your API call logic here
        System.out.println("Calling API...");

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

                //if(!response.isEmpty()) System.out.println("Got Mily Response");

                // GET BOARD FROM SERVER *GET*
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("roborally/src/main/resources/boards/test.json"))) {
                    writer.write(response.toString());
                    //System.out.println("JSON saved to " + "src/main/resources/boards/test.json");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Failed to write JSON file: " + e.getMessage());
                }
                // UFFE s3
                ObjectMapper objectMapper = new ObjectMapper();
                GameController.GameData gameData = objectMapper.readValue(new File("roborally/src/main/resources/boards/test.json"), GameController.GameData.class);

                // Update players
                for (Player jsonPlayer : gameData.getPlayers()) {
                    for (Player boardPlayer : board.getPlayers()) {
                        if (boardPlayer.getName().equals(jsonPlayer.getName())) {
                            boardPlayer.setSpace(jsonPlayer.getSpace());
                            boardPlayer.setHeading(jsonPlayer.getHeading());
                            boardPlayer.setCheckPoint(jsonPlayer.getCheckPoint());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Implement the actual API call logic
    }

    @Override
    public void run() {
        while (!shouldStop) {
            getApi();
            try {
                Thread.sleep(3000); // Sleep for 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("ApiTask interrupted");
            }
        }
    }
    public void stopApiTask() {
        shouldStop = true;
    }
}