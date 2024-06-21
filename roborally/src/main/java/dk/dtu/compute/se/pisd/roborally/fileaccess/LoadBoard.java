/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.fileaccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.BoardTemplate;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.SpaceTemplate;
import dk.dtu.compute.se.pisd.roborally.model.*;

import java.io.*;
import java.util.List;

/**
 * Provides functionality to load and save game boards and active game states for the RoboRally board game.
 * This class includes methods to load boards from JSON files, save game boards and active games to JSON,
 * and retrieve lists of available game tracks and active games. This class utilizes Google Gson for JSON processing.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author William Aslak Tonning, s205838
 */
public class LoadBoard {


    private static final String DEFAULTBOARD = "defaultboard";
    private static final String JSON_EXT = "json";
    private static final String PATH_TO_RES ="roborally" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator;
    private static final String BOARDSFOLDER = PATH_TO_RES + "boards";
    private static final String ACTIVEGAMES = PATH_TO_RES + "activeGames";

    /**
     * Loads a board configuration from a JSON file. If the specified board name is not found, it defaults to loading
     * the defaultboard. This method creates the board from the templates shown under the model folder.
     *
     * @param boardName The name of the file you wish to load which is kept undre resources/boards/
     * @return An initialized {@link Board} object, or null if an error occurs during file loading.
     */
    public static Board loadBoard(String boardName) {

        if (boardName == null) {
            boardName = DEFAULTBOARD;
            System.out.println("printing default");
        }

        File file = new File( BOARDSFOLDER + File.separator + boardName + "." + JSON_EXT);

        if (!file.exists()) {
            System.out.println("File not found - printing default");
            file = new File(BOARDSFOLDER + File.separator + DEFAULTBOARD + "." + JSON_EXT);
        }

        GsonBuilder simpleBuilder = new GsonBuilder().registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>());
        Gson gson = simpleBuilder.create();

        Board result;
        JsonReader reader = null;

        try {
            reader = gson.newJsonReader(new FileReader(file));
            BoardTemplate template = gson.fromJson(reader, BoardTemplate.class);

            result = new Board(template.width, template.height, template.numberOfCheckPoints);

            for (SpaceTemplate spaceTemplate: template.spawnSpaces) {
                Space space = result.getSpace(spaceTemplate.x, spaceTemplate.y);
                if (space != null) {
                    result.addSpawnSpace(space);
                }
            }
            for (SpaceTemplate spaceTemplate: template.spaces) {
                Space space = result.getSpace(spaceTemplate.x, spaceTemplate.y);
                if (space != null) {
                    space.setActions(spaceTemplate.actions);
                    space.getWalls().addAll(spaceTemplate.walls);
                }
            }

            reader.close();
            result.setMap(boardName);

            return result;
        } catch (IOException e1) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2) {}
            }
        }
        return null;
    }


    /**
     * Loads an active board game state from a JSON file. This includes the boardName, players, their states, and the game
     * phase. If the board game file is not found, this method returns null.
     * It will call the previous function (loadBoard) with the loaded boardName which will return null if an error occured.
     *
     * @param activeGameName The name of the active game file to load.
     * @return An initialized {@link Board} with the current game state, or null if the file does not exist.
     */
    public static Board loadActiveBoard(String activeGameName){
        File file = new File(ACTIVEGAMES + File.separator + activeGameName + "." + JSON_EXT);

        if (!file.exists()) {
            return null;
        }
        GsonBuilder simpleBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>());
        Gson gson = simpleBuilder.create();

        String mapName = "";
        List<Player> playerList = null;
        Player current = null;
        Phase phase = null;
        Boolean stepmode = null;
        int step = 0;


        JsonReader reader = null;
        try {
            reader = gson.newJsonReader(new FileReader(file));
            Board template = gson.fromJson(reader, Board.class);

            mapName = template.getMap();
            playerList = template.getPlayers();
            current = template.getCurrentPlayer();
            phase = template.getPhase();
            stepmode = template.isStepMode();
            step = template.getStep();

            reader.close();

        } catch (IOException e1) {
            System.out.println(e1.toString());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2) {}
            }
        }

        Board board = loadBoard(mapName);

        for(Player p : playerList) {

            Player player = new Player(board, p.getColor(), p.getName());

            for(int i = 0; i < player.NO_REGISTERS; i++) player.setProgramField(i,p.getProgramField(i));

            for(int i = 0; i < player.NO_CARDS; i++) player.setCardField(i,p.getCardField(i));

            Space space = board.getSpace(p.getSpace().x,p.getSpace().y);
            player.setSpace(space);

            Space spawnSpace = board.getSpace(p.getSpawnSpace().x, p.getSpawnSpace().y);
            player.setSpawnSpace(spawnSpace);

            player.addEnergyCubes(p.getEnergyCubes());

            player.setCheckPoint(p.getCheckPoint());

            player.setHeading(p.getHeading());
            board.addPlayer(player);

            if(player.getName().equals(current.getName()))
                board.setCurrentPlayer(player);

        }
        board.setPhase(phase);
        board.setStepMode(stepmode);
        board.setStep(step);

        return board;
    }

    /**
     * Retrieves the names of all available tracks stored in the resources folder.
     *
     * @return An array of string representing the names of all stored boards, or null if no files are found.
     */
    public static String[] getTracks(){

        File folder = new File(BOARDSFOLDER);
        System.out.println(folder.getAbsolutePath());

        if(folder.listFiles() == null)
            return null;
        String[] files = new String[folder.listFiles().length];
        for(int i = 0; i < folder.listFiles().length; i++)
            files[i] = folder.listFiles()[i].getName().substring(0,folder.listFiles()[i].getName().length()-5);
        return files;
    }
    /**
     * Very similar to the previous function with it returning the activeGames instead.
     *
     * @return An array of string representing the names of all stored active games, or null if no files are found.
     */
    public static String[] getActiveGames(){
        File folder = new File(ACTIVEGAMES);

        if(folder.listFiles() == null)
            return null;
        String[] files = new String[folder.listFiles().length];
        for(int i = 0; i < folder.listFiles().length; i++)
            files[i] = folder.listFiles()[i].getName().substring(0,folder.listFiles()[i].getName().length()-5);
        return files;

    }

    /**
     * Saves the {@link Board} template as a JSON file in the 'boards' directory under resources. This includes
     * spaces and their properties such as walls and actions.
     * This is used when creating a new map.
     *
     * @param board The {@link Board} to save.
     * @param name The name under which to save the board file.
     */
    public static void saveBoard(Board board, String name) {
        BoardTemplate template = new BoardTemplate();
        template.width = board.width;
        template.height = board.height;

        for (Space space : board.getSpawnSpaces()) {
            SpaceTemplate spaceTemplate = new SpaceTemplate(space.x, space.y, space.getWalls(), space.getActions());
            template.spawnSpaces.add(spaceTemplate);
        }

        for (int i=0; i<board.width; i++) {
            for (int j=0; j<board.height; j++) {
                Space space = board.getSpace(i,j);
                if (!space.getWalls().isEmpty() || space.getActions() != null) {
                    SpaceTemplate spaceTemplate = new SpaceTemplate(space.x, space.y, space.getWalls(), space.getActions());
                    template.spaces.add(spaceTemplate);
                }
            }
        }

        String filename = BOARDSFOLDER + File.separator + name + "." + JSON_EXT;

        GsonBuilder simpleBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                setPrettyPrinting();
        Gson gson = simpleBuilder.create();

        writeFile(template, filename, gson);
    }
    /**
     * Saves the current game state as a JSON file in the 'activeGames' directory under resources. This includes the board's
     * state and all player details.
     *
     * @param board The {@link Board} representing the current game state.
     * @param name The name under which to save the active game file.
     */
    public static void saveCurrentGame(Board board, String name){
        String filename = ACTIVEGAMES + File.separator + name + "." + JSON_EXT;
        GsonBuilder simpleBuilder = new GsonBuilder().
                excludeFieldsWithoutExposeAnnotation().
                setPrettyPrinting();
        Gson gson = simpleBuilder.create();

        writeFile(board, filename, gson);
    }
    /**
     * Generic function to write an object to a file in the JSON format using Gson. Handles file operations and JSON writing.
     *
     * @param <T> The type of the object to write.
     * @param object The object to serialize to JSON.
     * @param filename The path and filename where the JSON will be saved.
     * @param gson The Gson instance configured for this serialization.
     */
    private static  <T> void writeFile(T object, String filename, Gson gson) {
        try (FileWriter fileWriter = new FileWriter(filename);
             JsonWriter writer = gson.newJsonWriter(fileWriter)) {
            gson.toJson(object, object.getClass(), writer);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
