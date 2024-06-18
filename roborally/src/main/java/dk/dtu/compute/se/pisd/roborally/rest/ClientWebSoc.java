package dk.dtu.compute.se.pisd.roborally.rest;

//import jakarta.websocket.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.EncodeException;

@ClientEndpoint
public class ClientWebSoc {

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected to server");
    }

    public void sendMessage(String message) throws IOException, EncodeException {
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(message);
        } else {
            System.err.println("Session is not open");
        }
    }

    public static void main(String[] args) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://192.168.0.208:8080/websocket-endpoint"; // Replace with your WebSocket endpoint

        try {
            // Connect to WebSocket server
            container.connectToServer(ClientWebSoc.class, java.net.URI.create(uri));

            // Read JSON file
            String filePath = "src/main/resources/activeGames/demo1.json";
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)));

            // Send JSON data
            ClientWebSoc client = new ClientWebSoc();
            client.sendMessage(jsonData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect(String uri) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, java.net.URI.create(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

