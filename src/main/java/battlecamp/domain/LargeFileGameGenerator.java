package battlecamp.domain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Random;

import battlecamp.domain.Tile.Type;

public class LargeFileGameGenerator {
	
	private static PrintWriter out;
	private LinkedHashSet<Tile> TilesToDo = new LinkedHashSet<>();

    public void generate(int rows, int columns) throws IOException {
        Game game = new Game(null, null);
        File file = new File("c:/temp/game_"+ game.getId()+ ".txt");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        
        out = new PrintWriter(new BufferedWriter(new FileWriter(file, true), 1048576*4));
        generateBoard(rows, columns);

        out.close();
    }

    private void toFile(Tile tile, String accesibleNeigbours){
    	String id = tile.getX() + "." + tile.getY();
    	String theTile =id + "|" + tile.getType().name() + "|" + accesibleNeigbours;
        out.println(theTile);
    }
    
    private Tile generateTile(int x, int y){
    	Type type = Type.WATER;
    	if (new Random().nextInt(10) > 7) {
            type = Tile.Type.ROTS;
        }
    	Tile tile = new Tile(x, y, type);
    	tile.setId(x + "." +y);
    	return tile;
    }
    
    public void generateBoard(int rows, int columns) throws IOException {
        //plaats iglo
        Random r = new Random(System.currentTimeMillis());
        int huis_x = columns - r.nextInt((int)(columns * 0.05) + 1) - 1;
        int huis_y = rows - r.nextInt(rows);
        
        //plaats tiles om iglo in hashmap
        toFile(new Tile(huis_x, huis_y, Type.HUIS),"TODO");
        TilesToDo.add(new Tile(huis_x-1, huis_y, Type.IJS));
        if(huis_x < columns-1){
        	TilesToDo.add(new Tile(huis_x+1, huis_y, Type.IJS));
        } if(huis_y > 0){
        	TilesToDo.add(new Tile(huis_x, huis_y-1, Type.IJS));
        } if(huis_y < rows -1){
        	TilesToDo.add(new Tile(huis_x, huis_y+1, Type.IJS));
        }
        
        //process hashmap
        while(TilesToDo.size() >0){
            //haal tile van hashmap
        	Tile tileToProcess = (Tile)TilesToDo.toArray()[0];
        	String accesibleNeigbours = "";
        	
        	TilesToDo.remove(tileToProcess);
        	//tiles links alleen genereren indien deze tile links van iglo in het veld ligt
        	if(tileToProcess.getX() <= huis_x && (tileToProcess.getX() > 0)){
        		Tile genTile = generateTile(tileToProcess.getX()-1, tileToProcess.getY());
        		TilesToDo.add(genTile);
        		if(genTile.getType().equals(Type.WATER) || genTile.getType().equals(Type.IJS)){
        			accesibleNeigbours = accesibleNeigbours + "@" + genTile.getId() + "@";
        		}
        	}
           	
        	//tiles rechts alleen genereren indien deze tile rechts van iglo in het veld ligt
        	if(tileToProcess.getX() >= huis_x && (tileToProcess.getX() < columns-1)){
        		Tile genTile = generateTile(tileToProcess.getX()+1, tileToProcess.getY());
        		TilesToDo.add(genTile);
        		if(genTile.getType().equals(Type.WATER) || genTile.getType().equals(Type.IJS)){
        			accesibleNeigbours = accesibleNeigbours + "@" + genTile.getId() + "@";
        		}
        	}
        	
        	//tiles boven alleen genereren indien deze tile boven iglo in het veld ligt
        	if(tileToProcess.getY() <= huis_y && (tileToProcess.getY() > 0)){
        		Tile genTile = generateTile(tileToProcess.getX(), tileToProcess.getY()-1);
        		TilesToDo.add(genTile);
        		if(genTile.getType().equals(Type.WATER) || genTile.getType().equals(Type.IJS)){
        			accesibleNeigbours = accesibleNeigbours + "@" + genTile.getId() + "@";
        		}
        	}
        	
        	//tiles onder alleen genereren indien deze tile onder van iglo in het veld ligt
        	if(tileToProcess.getY() >= huis_y && (tileToProcess.getY() < rows-1)){
        		Tile genTile = generateTile(tileToProcess.getX(), tileToProcess.getY()+1);
        		TilesToDo.add(genTile);
        		if(genTile.getType().equals(Type.WATER) || genTile.getType().equals(Type.IJS)){
        			accesibleNeigbours = accesibleNeigbours + "@" + genTile.getId() + "@";
        		}
        	}
         	
            //genereer colom met buren
            //schrijf weg naar file
        	toFile(tileToProcess, accesibleNeigbours);
        }
        
    }
    
//    public static void main(String[] args) throws Exception{
//    	LargeFileGameGenerator a = new LargeFileGameGenerator();
//    	a.generate(6000,6000);
//	}
}