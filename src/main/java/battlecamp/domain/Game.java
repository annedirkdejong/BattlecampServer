package battlecamp.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import battlecamp.Broadcaster;

public class Game {

    private String id;
    private LocalDateTime creation;
    private Board board;
    private boolean started;
    private boolean stopped;
    private int moves = 0;

    private List<Player> players = new ArrayList<Player>();

    private  Broadcaster broadcaster;
    @JsonIgnore
	private Beurt beurt;
    
    private Object lock = new Object();
    
    public Game(Board board, Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
        this.id = UUID.randomUUID().toString();
        this.board = board;
        creation = LocalDateTime.now();
    }

    public  void start() {
        this.started = true;
        broadcaster.broadcast("games", this);
    }

    public  void stop() {
        this.stopped = true;
        players.stream().forEach(player -> player.reset());
        broadcaster.broadcast("games", this);
    }

    public  String getId() {
        return id;
    }

    public  String getCreation() {
        return creation.toString();
    }

    public  List<Player> getPlayers() {
        return players;
    }

    public  void addPlayer(Player player) {
        players.add(player);
        board.addPlayer(player);
        broadcaster.broadcast("games", this);
    }

    public  Board getBoard() {
        return board;
    }

    public  void setBoard(Board board) {
        this.board = board;
    }

    public  boolean isStarted() {
        synchronized (lock) {
			return started;
		}
    }

    public  boolean isStopped() {
        synchronized (lock) {
			return stopped;
		}
    }

    public  void volgendeBeurt() throws InterruptedException {
        Optional<Tile> randomWaterTile = board.getTiles()
                .stream()
                .filter(tile -> tile.getType() == Tile.Type.WATER)
                .skip(new Random().nextInt(board.getTiles().size()))
                .findFirst();
        randomWaterTile.ifPresent(tile -> {
            tile.setType(Tile.Type.IJS);
            //broadcaster.broadcast("updates", tile);
        });

        List<Player> livingPlayers = players.stream().filter(player -> !player.isDead()).collect(Collectors.toList());
        if (livingPlayers.isEmpty()){
        	stop();
        	return;
        }
        Player nextPlayer = null;
        for (Player player : livingPlayers) {
            if (player.isHeeftBeurt()) {
                player.setHeeftBeurt(false);
                int index = livingPlayers.indexOf(player);
                if (index < livingPlayers.size()-1) {
                    nextPlayer = livingPlayers.get(index+1);
                } else {
                    nextPlayer = livingPlayers.get(0);
                }
            }
        }
        if (nextPlayer == null) {
            nextPlayer = livingPlayers.get(0);
        }
        nextPlayer.setHeeftBeurt(true);
        beurt = new Beurt(this.id, nextPlayer, this.getPlayers());
		broadcaster.broadcast("beurt", beurt);
//        this.messagingTemplate.convertAndSend("/topic/beurt", new Beurt(this.id, nextPlayer));
//        int oldMoves = this.moves;
//        TimeUnit.MILLISECONDS.sleep(5000);
//        if (oldMoves == this.moves && nextPlayer.isHeeftBeurt() && !nextPlayer.isWinner()) {
//            nextPlayer.die();
//        }
    }

    public  boolean hasLivingPinguins() {
        return players.stream().anyMatch(player -> !player.isDead() && player.getType()== Player.Type.PINGUIN);
    }

    public  void movePlayer(String playerId, String direction) throws InterruptedException {
        synchronized (lock) {
			Player player = players.stream()
					.filter(p -> p.getId().equals(playerId)).findFirst().get();
			if (player.isDead()
					|| !player.getId().equals(beurt.getPlayer().getId())) {
				return;
			}
			board.movePlayer(player, direction);
			this.moves++;
			if (player.getType() == Player.Type.PINGUIN && player.getLocation().getType() == Tile.Type.HUIS) {
				player.win();
				stop();
			} else if (!hasLivingPinguins()) {
				stop();
			} else {
				TimeUnit.MILLISECONDS.sleep(10);
				volgendeBeurt();
			}
		}
    }
    
    public  Beurt getBeurt(){
    	return beurt;
    }
	public  void expireBeurt() {
		synchronized (lock) {
			beurt.getPlayer().die();
			try {
				volgendeBeurt();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int getMoves() {
		return moves;
	}
}
