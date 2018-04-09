package battlecamp.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Created by jkuiper on 31-5-2014.
 */
public class Tile {

    public enum Type {WATER, ROTS, IJS, HUIS};

    private String id;
    private int x;
    private int y;
    private Type type;

    @JsonManagedReference
    private Player player;

    
    public Tile(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Tile x:" + this.x + ", y:" + this.y + ", type: " + type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Type getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        player.setLocation(this);
    }

    public void removePlayer() {
        this.player = null;
    }
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tile other = (Tile) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}

}
