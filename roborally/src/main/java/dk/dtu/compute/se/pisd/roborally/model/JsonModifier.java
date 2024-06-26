package dk.dtu.compute.se.pisd.roborally.model;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonModifier {

    /***
     * Takes the json data file from the clien and modifies it so its true
     * to the game state
     * @param jsonInput player data
     * @param board the game board it's played on
     * @return the new state of the game in json format
     * @author Anders J and Uffe B
     */
    public static String modifyJson(String jsonInput, Board board) {

        try {
            JSONObject jsonObject = new JSONObject(jsonInput);
            JSONObject current = jsonObject.getJSONObject("current");
            JSONArray players = jsonObject.getJSONArray("players");
            // Modifying phase, step, and specific player's properties
            jsonObject.put("phase", board.getPhase());
            jsonObject.put("step", board.getStep());

            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                // sets the new information for every player

                if (player.getString("name").equals("Player "+ (i+1))) {
                    JSONObject space = player.getJSONObject("space");
                    space.put("x", board.getPlayer(i).getSpace().x);
                    space.put("y", board.getPlayer(i).getSpace().y);
                    player.put("heading", board.getPlayer(i).getHeading());
                    player.put("checkPoint", board.getPlayer(i).getCheckPoint());
                }
            }

            int currentIndex = -1;
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                if (player.getString("name").equals(current.getString("name"))) {
                    currentIndex = i;
                    break;
                }
            }

            // Set current to the next player in the list, wrapping around if necessary
            currentIndex = (currentIndex + 1) % players.length();  // Use modulus to wrap around
            current = players.getJSONObject(currentIndex);

            // Update the 'current' player in the JSON object
            jsonObject.put("current", current);


            // Serialize back to JSON
            String modifiedJson = jsonObject.toString(4); // Pretty print with an indent of 4
            return modifiedJson;
        } catch (Exception e) {
        e.printStackTrace();
        }
      return null;
    }

    /***
     * Takes a json file and put the new data into the board state
     * @param js json file from the server
     * @param board the board the data is placed
     * @author Anders J and Uffe C
     */
    public static void getJson(String js, Board board){
        try {
            JSONObject jsonObject = new JSONObject(js);
            JSONObject current = jsonObject.getJSONObject("current");
            JSONArray players = jsonObject.getJSONArray("players");

            // Update board's phase and step
            board.setPhase(Phase.valueOf(jsonObject.getString("phase")));
            board.setStep(jsonObject.getInt("step"));

            // Update each player's information on the board
            for (int i = 0; i < players.length(); i++) {
                JSONObject playerJson = players.getJSONObject(i);
                Player player = board.getPlayer(i); // Assuming getPlayer(i) returns a Player object

                if (player.getName().equals("Player " + (i + 1))) {
                    JSONObject space = playerJson.getJSONObject("space");

                    board.getPlayer(i).setSpace(board.getSpace(space.getInt("x"), space.getInt("y")));
                    board.getPlayer(i).setHeading(Heading.valueOf(playerJson.getString("heading")));
                    board.getPlayer(i).setCheckPoint(playerJson.getInt("checkPoint"));

                }
            }

            // Update the 'current' player on the board
            for (int i = 0; i < players.length(); i++) {
                JSONObject playerJson = players.getJSONObject(i);
                if (playerJson.getString("name").equals(current.getString("name"))) {
                   board.setCurrentPlayer( board.getPlayer(i)); // Assuming setCurrentPlayerIndex updates the current player
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}