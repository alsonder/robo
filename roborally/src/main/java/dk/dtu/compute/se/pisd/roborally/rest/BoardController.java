package dk.dtu.compute.se.pisd.roborally.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;





@RestController
public class BoardController {
    @Autowired
    private IBoard boardService;


    @GetMapping(value = "/game1", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getGame1Links() {
        String htmlContent = "<html><body>" +
                "<h1>Game 1 Links</h1>" +
                "<p><a href='game1/board'>Board Details</a></p>" +
                "<p><a href='game1/player'>Player Details</a></p>" +
                "</body></html>";
        return ResponseEntity.ok(htmlContent);
    }

    /*@GetMapping("/game1/board")
    public ResponseEntity<List<Space>> getBoard() {
        List<Space> spaces = boardService.findAll();
        return ResponseEntity.ok(spaces);
    }*/
    @GetMapping(value = "/game1/board", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getBoardJson() throws IOException {
        Resource resource = new ClassPathResource("boards/defaultboard.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping(value = "/game1/player", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getPlayerJson() throws IOException {
        Resource resource = new ClassPathResource("activeGames/demo1.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
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
