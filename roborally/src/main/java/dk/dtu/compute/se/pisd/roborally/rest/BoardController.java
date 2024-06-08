package dk.dtu.compute.se.pisd.roborally.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BoardController {
    @Autowired
    private IBoard boardService;

    @GetMapping(value = "/board")
    public ResponseEntity<List<Space>> getSpace()
    {
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
