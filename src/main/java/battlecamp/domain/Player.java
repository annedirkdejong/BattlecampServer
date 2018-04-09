package battlecamp.domain;

import java.util.HashMap;
import java.util.Map;

import battlecamp.Broadcaster;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Created by jkuiper on 1-6-2014.
 */
public class Player {

    private static Map<String, Player> players = new HashMap<>();

    public enum Type {PINGUIN, ZEELEEUW}

    private String id;
    private Type type;
    private boolean winner;
    private boolean dead;
    private boolean heeftBeurt;
    private int points = 0;
    private String color;

    private  Broadcaster broadcaster;

    @JsonBackReference
    private Tile location;

    public static synchronized Player newPlayer(String id, Type type, String color, Broadcaster broadcaster) {
        if (!players.containsKey(id)) {
            players.put(id, new Player(id, type, color, broadcaster));
        }
        Player player = players.get(id);
        player.setType(type);
        player.setColor(color);
        return player;
    }
    

    private Player(String id, Type type, String color, Broadcaster broadcaster) {
        System.out.println("new player " + id);
        this.id = id;
        this.type = type;
        this.color = color;
        this.broadcaster = broadcaster;
    }

    public void reset() {
        this.winner = false;
        this.dead = false;
        this.heeftBeurt = false;
    }

    public Type getType() {
        return type;
    }

    public Tile getLocation() {
        return location;
    }

    public void setLocation(Tile location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void win() {
        this.winner = true;
        this.points += 5;
        broadcaster.broadcast("updates", getLocation());
//        this.messagingTemplate.convertAndSend("/topic/updates", getLocation());
    }

    public void addPoints(int points) {
        this.points =+ points;
    }

    public void die() {
        this.points -= 1;
        this.dead = true;
        broadcaster.broadcast("updates", getLocation());
//        this.messagingTemplate.convertAndSend("/topic/updates", getLocation());
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isWinner() {
        return winner;
    }

    public int getX() {
        return location.getX();
    }

    public int getY() {
        return location.getY();
    }

    public void setHeeftBeurt(boolean heeftBeurt) {
        this.heeftBeurt = heeftBeurt;
    }

    public boolean isHeeftBeurt() {
        return heeftBeurt;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getPoints() {
        return points;
    }


	public void setType(Type type) {
		this.type = type;
	}
}
