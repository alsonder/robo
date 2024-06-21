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
package dk.dtu.compute.se.pisd.roborally.view;

import com.google.gson.*;
import com.sun.tools.javac.Main;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.ConveyorBelt;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

import com.google.gson.stream.JsonReader;

import java.io.*;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 60; // 75;
    final public static int SPACE_WIDTH = 60; // 75;

    public final Space space;


    public SpaceView(@NotNull Space space) {
        this.space = space;

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);

        /*      //for other maps later
        StartBoard.exexuteStartBoard(this, space);
        if (Objects.equals(Value.map, "GoldenStripe"))
            GoldStripe.executeGoldStripe(this, space);
        else if (Objects.equals(Value.map, "WhirlWind"))
            WhirlWind.executeWhirlWind(this, space);
        else if (Objects.equals(Value.map, "RingOfDeath"))
            RingOfDeath.executeRingOfDeath(this, space);
        else if (Objects.equals(Value.map, "Testing"))
            TestingMap.executeTestMap(this, space);
        else GoldStripe.executeGoldStripe(this,space);
        */

        if ((space.x + space.y) % 2 == 0) {
            paint(4,0);
        } else {
            paint(4,0);
        }

        // Path to the JSON file
        String filePath = "roborally\\src\\main\\resources\\boards\\defaultboard.json";
        /**
         * Reads a JSON file from the specified file path, parses it, and processes it for painting the tiles.
         *
         * @param filePath The path to the JSON file to be read and processed.
         * @author Uffe
         */
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Read the JSON file as a string
            StringBuilder jsonStringBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
            String jsonString = jsonStringBuilder.toString();

            // Parse the JSON string
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);

            // Get the "spaces" array
            JsonArray spacesArray = jsonObject.getAsJsonArray("spaces");
            if (spacesArray != null) {
                // Iterate over each element in the array
                for (JsonElement spaceElement : spacesArray) {
                    // Get the "actions" array for each space
                    JsonObject spaceObject = spaceElement.getAsJsonObject();
                    JsonArray actionsArray = spaceObject.getAsJsonArray("actions");
                    if (actionsArray != null) {
                        // Iterate over each element in the "actions" array
                        for (JsonElement actionElement : actionsArray) {
                            JsonObject actionObject = actionElement.getAsJsonObject();
                            String className = actionObject.getAsJsonPrimitive("CLASSNAME").getAsString();
                            className = className.substring(className.lastIndexOf('.') + 1);
                            JsonObject instanceObject = actionObject.getAsJsonObject("INSTANCE");
                            JsonArray wallsArray = spaceObject.getAsJsonArray("walls");
                            int x = spaceObject.getAsJsonPrimitive("x").getAsInt();
                            int y = spaceObject.getAsJsonPrimitive("y").getAsInt();

                            if (wallsArray !=null)
                                for (JsonElement wall : wallsArray) {
                                    if (Objects.equals(wall.getAsString(), "NORTH")){
                                        if (space.x == x && space.y+1 == y)
                                            paint(4,3);
                                    }
                                    else if (Objects.equals(wall.getAsString(), "SOUTH")){
                                        if (space.x == x && space.y == y+1)
                                            paint(6,3);
                                    }
                                    else if (Objects.equals(wall.getAsString(), "EAST")){
                                        if (space.x == x+1 && space.y == y)
                                            paint(5,3);
                                    }
                                    else if (Objects.equals(wall.getAsString(), "WEST")){
                                        if (space.x+1 == x && space.y == y  )
                                            paint(6,2);
                                    }
                                 //   System.out.println("set wall on " + wall.getAsString() + " facing me");
                                }
                            if (className.equals("ConveyorBelt")){
                               // System.out.println("set on "+x+","+y+" : "+ className+" "+instanceObject.getAsJsonPrimitive("heading").getAsString());
                                if (space.x == x && space.y == y){
                                    if (Objects.equals(instanceObject.getAsJsonPrimitive("heading").getAsString(), "SOUTH"))
                                        paint(1,6);
                                    else if (Objects.equals(instanceObject.getAsJsonPrimitive("heading").getAsString(), "EAST"))
                                        paint(3,6);
                                    else if (Objects.equals(instanceObject.getAsJsonPrimitive("heading").getAsString(), "WEST"))
                                        paint(2,6);
                                    else if (Objects.equals(instanceObject.getAsJsonPrimitive("heading").getAsString(), "NORTH"))
                                        paint(0,6);
                                }
                            }
                            else if (className.equals("CheckPoint")){
                                if (space.x == x && space.y == y) {
                                    if (instanceObject.getAsJsonPrimitive("number").getAsNumber().intValue() == 1)
                                        paint(1, 11);
                                    else if (instanceObject.getAsJsonPrimitive("number").getAsNumber().intValue() == 2)
                                        paint(2, 11);
                                    else if (instanceObject.getAsJsonPrimitive("number").getAsNumber().intValue() == 3)
                                        paint(3, 11);
                                    else if (instanceObject.getAsJsonPrimitive("number").getAsNumber().intValue() == 4)
                                        paint(1, 12);
                                    else if (instanceObject.getAsJsonPrimitive("number").getAsNumber().intValue() == 5)
                                        paint(0, 12);
                                }
                            }

                            // down right left up

                            // Print a separator for clarity
                           // System.out.println("-----------------------------------");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }




        /*
        if ((space.x + space.y) % 2 == 0) {
            this.setStyle("-fx-background-color: white;");
        } else {
            this.setStyle("-fx-background-color: black;");
        }*/

        // updatePlayer();

        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }

    private void updatePlayer() {
        this.getChildren().clear();

        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0 );
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90*player.getHeading().ordinal())%360);
            this.getChildren().add(arrow);
        }
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            updatePlayer();
        }
    }

    /**
     * Sets the background image and position for a JavaFX node based on the resource board_tiles.jpg.
     *
     * @param x The x-coordinate of the image.
     * @param y The y-coordinate of the image.
     * @author Uffe
     */
    private void paint(double x,double y){
        this.setStyle("-fx-background-image: url(board_tiles.jpg)" +
                "; -fx-background-position: " + 16.5*x + "% " + 8.25*y + "%" + //16.6, 8.25
                "; -fx-background-size: " + 430 + "px " + 793 + "px"
        );
    }
    /**
     * @deprecated This method has been deprecated and replaced with {@link #paint(double, double)}.
     *             Use {@link #paint(double, double)} instead.
     * @author Uffe
     */
    @Deprecated
    private void paintOn(double x,double y){
        ImageView overlayImageView = new ImageView(new Image("board_tiles.jpg"));
        overlayImageView.setFitWidth(430); // Set the width of the overlay image
        overlayImageView.setFitHeight(793); // Set the height of the overlay image
        overlayImageView.setX(x * getWidth() / 100); // Convert percentage to pixels
        overlayImageView.setY(y * getHeight() / 100); // Convert percentage to pixels
        this.getChildren().add(overlayImageView);
    }
}
