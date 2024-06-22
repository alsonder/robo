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
package dk.dtu.compute.se.pisd.roborally.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import org.jetbrains.annotations.NotNull;

import static dk.dtu.compute.se.pisd.roborally.model.Heading.SOUTH;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class Player extends Subject {

    final public static int NO_REGISTERS = 5;
    final public static int NO_CARDS = 8;

    @JsonProperty("turn")
    @Expose
    private boolean turn;

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    @JsonBackReference("board-players")
    final public Board board;
    @Expose
    private String name;
    @Expose
    private String color;
    @Expose
    @JsonBackReference
    private Space space;
    @Expose
    private Heading heading = SOUTH;
    @Expose
    private CommandCardField[] program;
    @Expose
    private CommandCardField[] cards;
    @Expose
    private int energyCubes;
    @Expose
    private Space spawnSpace;
    @Expose
    private int checkPoint = 0;

    // Default constructor for Jackson
    @JsonCreator
    public Player(
            @JsonProperty("board") Board board,
            @JsonProperty("color") String color,
            @JsonProperty("name") String name) {
        this.board = board;
        this.name = name;
        this.color = color;

        this.space = null;
        this.spawnSpace = null;

        program = new CommandCardField[NO_REGISTERS];
        for (int i = 0; i < program.length; i++) {
            program[i] = new CommandCardField(this);
        }

        cards = new CommandCardField[NO_CARDS];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new CommandCardField(this);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.equals(this.name)) {
            this.name = name;
            notifyChange();
            if (space != null) {
                space.playerChanged();
            }
        }
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        notifyChange();
        if (space != null) {
            space.playerChanged();
        }
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        Space oldSpace = this.space;
        if (space != oldSpace &&
                (space == null || space.board == this.board)) {
            this.space = space;
            if (oldSpace != null) {
                oldSpace.setPlayer(null);
            }
            if (space != null) {
                space.setPlayer(this);
            }
            notifyChange();
        }
    }

    public void setSpawnSpace(Space spawnSpace) {
        this.spawnSpace = spawnSpace;
    }
    /**
     * Returns the player's current spawn location.
     * @return a Space object representing the player's spawn location
     */
    public Space getSpawnSpace() {
        return spawnSpace;
    }

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(@NotNull Heading heading) {
        if (heading != this.heading) {
            this.heading = heading;
            notifyChange();
            if (space != null) {
                space.playerChanged();
            }
        }
    }

    /**
     * Returns the current total of energy cubes for the player.
     *
     * @return the number of energy cubes the player has
     * @author Aleksander Sonder, s185289
     */
    public int getEnergyCubes() {
        return energyCubes;
    }

    /**
     * Adds a specified amount of energy cubes to the player's current total.
     *
     * @param amount the number of energy cubes to add
     * @author Aleksander Sonder, s185289
     */
    public void addEnergyCubes(int amount) {
        this.energyCubes += amount;
        notifyChange();
    }

    /**
     * Uses a specified amount of energy cubes from the player's current total.
     * If the player does not have enough energy cubes, an IllegalArgumentException is thrown.
     *
     * @param amount the number of energy cubes to use
     * @throws IllegalArgumentException if the amount of energy cubes to use is greater than the player's current total
     * @author Aleksander Sonder, s185289
     */
    public void useEnergyCubes(int amount) {
        if (amount > this.energyCubes) {
            throw new IllegalArgumentException("Not enough energy cubes");
        }
        this.energyCubes -= amount;
        notifyChange();
    }

    /**
     * get the number of check points visited
     * by a player
     * @return The number of check points a player have
     * @author Anders
     */
    public int getCheckPoint(){return checkPoint;}

    /**
     * then a player lands on a check point,
     * this is called to update their number
     * of check points
     * @author Anders
     */
    public void updateCheckPoint(){
        checkPoint +=1;
    }

    public void setCheckPoint(int n){checkPoint = n;}

    public CommandCardField getProgramField(int i) {
        return program[i];
    }

    public CommandCardField getCardField(int i) {
        return cards[i];
    }

    public void setProgramField(int i, CommandCardField field) {
        program[i].setCard(field.getCard());
        program[i].setVisible(field.isVisible());
        notifyChange();
    }

    public void setCardField(int i, CommandCardField field) {
        cards[i].setCard(field.getCard());
        notifyChange();
    }

}
