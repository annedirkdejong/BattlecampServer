package battlecamp.domain;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RandomGameGenerator {

    public static String generate(int rows, int columns) throws IOException {
        SmileFactory smileFactory = new SmileFactory();

        JsonFactory jsonFactory = new MappingJsonFactory();
        Game game = new Game(null, null);
        File file = new File("c:/temp/game_"+ game.getId()+ ".json");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        JsonGenerator generator = jsonFactory.createGenerator(file, JsonEncoding.UTF8);
//        JsonGenerator generator = smileFactory.createGenerator(file, JsonEncoding.UTF8);

        generator.writeStartObject();
        generator.writeStringField("id", game.getId());
        generator.writeStringField("creation", game.getCreation());

        generator.writeObjectFieldStart("board");

        generateBoard(rows, columns, generator);

        generator.writeEndObject();

        generator.writeBooleanField("started", game.isStarted());
        generator.writeBooleanField("stopped", game.isStopped());
        generator.writeNumberField("moves", game.getMoves());
//        generator.writeObjectField("players", game.getPlayers());

        generator.writeEndObject();

        generator.close();

        return game.getId();
    }

    public static void generateBoard(int rows, int columns, JsonGenerator jsonGenerator) throws IOException {
        Board board = Board.emptyBoard(columns, rows);
        jsonGenerator.writeNumberField("rows", board.getRows());
        jsonGenerator.writeNumberField("columns", board.getColumns());

        jsonGenerator.writeArrayFieldStart("tiles");
        for (int x=0; x< columns; x++) {
            for (int y=0; y < rows; y++) {
                Tile.Type type = Tile.Type.WATER;
                int maxIceWidth = 26;
                if (x > (columns /2 - maxIceWidth/2) && x < (columns /2 + maxIceWidth/2)) {
                    int midden = Math.abs((columns /2) -x);
                    if (new Random().nextInt(maxIceWidth) > midden-1) {
                        type = Tile.Type.IJS;
                    }
                }
                if (new Random().nextInt(10) > 7) {
                    type = Tile.Type.ROTS;
                }
                if (x == (columns -1) && y == (rows -1)/2)  {
                    type = Tile.Type.HUIS;
                }

                Tile tile = new Tile(x, y, type);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeNumberField("x", tile.getX());
                jsonGenerator.writeNumberField("y", tile.getY());
                jsonGenerator.writeObjectField("type", tile.getType().name());
                if (tile.getPlayer() != null) {
                    jsonGenerator.writeObjectField("player", tile.getPlayer());
                }
                jsonGenerator.writeEndObject();
//                jsonGenerator.writeObject(tile);
            }
        }
        jsonGenerator.writeEndArray();
    }
}
