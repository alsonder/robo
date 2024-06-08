package dk.dtu.compute.se.pisd.roborally.rest.game;
/*
import dk.dtu.compute.se.pisd.roborally.rest.IBoard;
import dk.dtu.compute.se.pisd.roborally.rest.Space;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GameController {
    @Autowired
    private IBoard boardService;

    @GetMapping("/game1")
    public ResponseEntity<List<Space>> getSpacesForGame1() {
        Long gameId = 1L; // This assumes the ID of Game1 is 1
        List<Space> spaces = boardService.findAll();
        return ResponseEntity.ok(spaces);
    }

    @GetMapping(value = "/board")
    public ResponseEntity<List<Space>> getSpace() {
        List<Space> spaces = boardService.findAll();
        return ResponseEntity.ok().body(spaces);
    }

    @PostMapping("/board")
    public ResponseEntity<String > addSpace(@RequestBody Space p) {
        boolean added = boardService.addSpace(p);
        if(added)
            return ResponseEntity.ok().body("added");
        else
            return ResponseEntity.internalServerError().body("not added");
    }

    @GetMapping("/board/{taste}")
    public ResponseEntity<Space> getNuggetBytaste(@PathVariable int taste) {
        Space p = boardService.getNuggetByTaste(taste);
        return ResponseEntity.ok().body(p);
    }
}
*/