package battlecamp.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import battlecamp.Broadcaster;

public class Board {

    private int rows;
    private int columns;
    private Broadcaster broadcaster;
    private List<Tile> tiles = new ArrayList<Tile>();

    private Board() {

    }
   
    public Board(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public static Board emptyBoard(int columns, int rows) {
        Board board = new Board();
        board.rows = rows;
        board.columns = columns;
        return board;
    }

    public void resetBoard(){
        this.tiles.forEach(tile->{
            tile.removePlayer();
        });
    }

    public static Board random(int columns, int rows,Broadcaster broadcaster) {
        Board board = new Board(broadcaster);
        board.rows = rows;
        Random random = new Random(5662);
        board.columns = columns;
        for (int x=0; x<columns; x++) {
            for (int y=0; y < rows; y++) {
                Tile.Type type = Tile.Type.WATER;
                int maxIceWidth = 26;
                if (x > (columns/2 - maxIceWidth/2) && x < (columns/2 + maxIceWidth/2)) {
                    int midden = Math.abs((columns/2) -x);
                    if (random.nextInt(maxIceWidth) > midden-1) {
                        type = Tile.Type.IJS;
                    }
                }
                if (random.nextInt(10) > 7) {
                    type = Tile.Type.ROTS;
                }

                Tile tile = new Tile(x, y, type);
                board.getTiles().add(tile);
            }
        }
        board.addHouse();
        return board;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void movePlayer(Player player, String direction) {
        int nextX = player.getLocation().getX();
        int nextY = player.getLocation().getY();
        switch (direction) {
            case "N": {
                nextY--;
                break;
            }
            case "S": {
                nextY++;
                break;
            }
            case "E": {
                nextX++;
                break;
            }
            case "W": {
                nextX--;
                break;
            }
        }

        Tile nextTile = findTile(nextX, nextY);
        if (nextTile == null) {
            System.out.println("next position: x:"+ nextX + ",y:" + nextY + " is over de rand" );
            return;
        }

        if (nextTile.getType() == Tile.Type.ROTS) {
            System.out.println("next position: x:"+ nextTile.getX() + ",y:" + nextTile.getY() + " is een rots" );
            return;
        }
        if (nextTile.getPlayer() != null) {
            if (player.getType() == Player.Type.PINGUIN && nextTile.getPlayer().getType() == Player.Type.ZEELEEUW) {
                player.die();
            }
            if (player.getType() == Player.Type.ZEELEEUW && !nextTile.getPlayer().isDead() && nextTile.getPlayer().getType() == Player.Type.PINGUIN) {
                nextTile.getPlayer().die();
                player.addPoints(5);
            }
            System.out.println("next position: x:"+ nextTile.getX() + ",y:" + nextTile.getY() + " is a player" );
            return;
        }
        if (player.getType() == Player.Type.ZEELEEUW && nextTile.getType() == Tile.Type.WATER) {
            System.out.println("next position: x:"+ nextTile.getX() + ",y:" + nextTile.getY() + " is a water" );
            return;
        }

        Tile oldLocation = player.getLocation();
        player.getLocation().removePlayer();
      
        nextTile.setPlayer(player);
        broadcaster.broadcast("updates",nextTile );        
    }

    private Tile findTile(int x, int y) {
        for (Tile tile: tiles) {
            if (tile.getX() == x && tile.getY() == y) {
                return tile;
            }
        }
        return null;
    }

    public void addPlayer(Player player) {
        boolean added = false;
        while (!added) {
            int random = new Random().nextInt(tiles.size());
            Tile tile = tiles.get(random);
            if (!(tile.getType() == Tile.Type.ROTS) && tile.getPlayer() == null) {
                if (player.getType() == Player.Type.PINGUIN) {
                    if (tile.getX() < 5) {
                        tile.setPlayer(player);
                        added = true;
                    }
                }
                if (player.getType() == Player.Type.ZEELEEUW) {
                    if ((tile.getType() == Tile.Type.IJS) && (tile.getX() == columns/2))  {
                        tile.setPlayer(player);
                        added = true;
                    }
                }
            }

        }
    }
    public void addHouse() {
        boolean added = false;
        while (!added) {
            int random = new Random().nextInt(tiles.size());
            Tile tile = tiles.get(random);
            if (!(tile.getType() == Tile.Type.ROTS) && tile.getPlayer() == null && (tile.getX()>(columns-5))) {
                tile.setType(Tile.Type.HUIS);
                Tile north = findTile(tile.getX(), tile.getY()+1);
                Tile south = findTile(tile.getX(), tile.getY()-1);
                Tile east = findTile(tile.getX()+1, tile.getY());
                Tile west = findTile(tile.getX()-1, tile.getY());
                if (north != null) north.setType(Tile.Type.IJS);
                if (south != null) south.setType(Tile.Type.IJS);
                if (east != null) east.setType(Tile.Type.IJS);
                if (west != null) west.setType(Tile.Type.IJS);

                added = true;
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}
