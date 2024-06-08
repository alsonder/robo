package dk.dtu.compute.se.pisd.roborally.rest;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class BoardService implements  IBoard {
    //creating an object of ArrayList
    ArrayList<Space> spaces = new ArrayList<Space>();

    public BoardService() {
        //adding products to the List
        spaces.add(new Space(
                new ArrayList<>(), // no walls
                Arrays.asList(new CheckPoint(2)), // actions
                5, // x coordinate
                4  // y coordinate
        ));

    }

    @Override
    public List<Space> findAll() {
        return spaces;
    }

    @Override
    public Space getNuggetByTaste(int taste) {
        for(Space n : spaces) {
            if(n.getX() == taste) {
                return n;
            }
        }
        return null;
    }

    @Override
    public boolean addSpace(Space n) {
        spaces.add(n);
        return true;
    }
/*
    @Override
    public List<Nugget> findAll() {
        //returns a list of product
        return nuggets;
    }

    @Override
    public Nugget getNuggetByTaste(int taste) {
        for(Nugget n : nuggets) {
            if(n.getTaste() == taste) {
                return n;
            }
        }
        return null;
    }

    @Override
    public boolean addNugget(Nugget n) {
        nuggets.add(n);
        return true;
    }
*/
}
