package dk.dtu.compute.se.pisd.roborally.model;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URL;

public class PlayerInfo {
    public static volatile String PlayerNumber;

    public static volatile int NumberOfPlayers;
    public static String URLPath;

    public static volatile int GlobalCurrentPlayer;

    public static volatile ObjectNode GlobalnextPlayer = null;
}


