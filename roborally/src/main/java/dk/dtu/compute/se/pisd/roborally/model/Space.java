package dk.dtu.compute.se.pisd.roborally.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.gson.annotations.Expose;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;

import java.util.ArrayList;
import java.util.List;

public class Space extends Subject {


    private Player player;

    private List<Heading> walls = new ArrayList<>();
    private List<FieldAction> actions = new ArrayList<>();


    @Expose
    public Board board;
    @Expose
    public int x;
    @Expose
    public int y;

    // Default constructor for Jackson
    public Space() {
        this.board = null; // or some default value
        this.x = 0;        // or some default value
        this.y = 0;        // or some default value
        player = null;
    }

    // Constructor with parameters
    @JsonCreator
    public Space(
            @JsonProperty("board") Board board,
            @JsonProperty("x") int x,
            @JsonProperty("y") int y) {
        this.board = board;
        this.x = x;
        this.y = y;
        player = null;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        Player oldPlayer = this.player;
        if (player != oldPlayer &&
                (player == null || board == player.board)) {
            this.player = player;
            if (oldPlayer != null) {
                // this should actually not happen
                oldPlayer.setSpace(null);
            }
            if (player != null) {
                player.setSpace(this);
            }
            notifyChange();
        }
    }

    public List<Heading> getWalls() {
        return walls;
    }

    public void setWalls(List<Heading> walls) {
        this.walls = walls;
    }

    public List<FieldAction> getActions() {
        return actions;
    }

    public void setActions(List<FieldAction> newActions) {
        actions = newActions;
    }

    void playerChanged() {
        // This is a minor hack; since some views that are registered with the space
        // also need to update when some player attributes change, the player can
        // notify the space of these changes by calling this method.
        notifyChange();
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

