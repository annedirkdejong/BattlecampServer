package battlecamp.domain;

public class Beurt {
    private String gameId;
    private Player player;
    private long start;

    public Beurt(String gameId, Player playerId) {
        this.gameId = gameId;
        this.player = playerId;
        start = System.currentTimeMillis();
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
}
