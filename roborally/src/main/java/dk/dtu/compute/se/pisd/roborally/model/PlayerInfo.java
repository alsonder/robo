package dk.dtu.compute.se.pisd.roborally.model;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URL;

/***
 * Made to save data we get from the server
 * @author Anders J and Uffe C
 */
public class PlayerInfo {
    public static volatile String PlayerNumber;

    public static volatile int NumberOfPlayers;
    public static String URLPath;
    public static ObjectNode GlobalnextPlayer;
}
