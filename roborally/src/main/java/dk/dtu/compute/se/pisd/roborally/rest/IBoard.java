package dk.dtu.compute.se.pisd.roborally.rest;

import java.util.List;

public interface IBoard {
        List<Space> findAll();
        public Space getNuggetByTaste(int taste);
        boolean addSpace(Space n);
}
