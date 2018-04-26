package battlecamp.domain;

import java.util.List;

public class Beurt {
    private String gameId;
    private Player player;
    private long start;
    private List<Player> players;

    public Beurt(String gameId, Player playerId, List<Player> players) {
        this.gameId = gameId;
        this.player = playerId;
        start = System.currentTimeMillis();
        this.players = players;
    }

    public String getGameId() {
        return gameId;
    }

    public Player getPlayer() {
        return player;
    }
    
    public boolean isExpired(long maxTime){
    	return System.currentTimeMillis()-start>maxTime;
    }

    public List<Player> getPlayers() { return players; }
}
