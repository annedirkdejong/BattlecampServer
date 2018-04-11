package battlecamp;

import battlecamp.domain.Board;
import battlecamp.domain.Game;
import battlecamp.domain.Player;
import battlecamp.domain.RandomGameGenerator;
import com.spotify.dns.DnsSrvResolver;
import com.spotify.dns.DnsSrvResolvers;
import com.spotify.dns.LookupResult;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import javax.jms.ConnectionFactory;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@ComponentScan
@EnableAutoConfiguration
@EnableWebSocket
@Configuration
@EnableScheduling
public class MainController implements SchedulingConfigurer {

	private static Map<String, Game> games = new HashMap<String, Game>();
	private Game currentGame;
	private Object lock = new Object();
	private Board myBoard = null;

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext applicationContext = SpringApplication
				.run(MainController.class, args);
		JmsTemplate jmsTemplate = applicationContext.getBean(JmsTemplate.class);
		jmsTemplate.setPubSubDomain(true);
	}

	@Bean
	ConnectionFactory connectionFactory() {
		DnsSrvResolver resolver = DnsSrvResolvers.newBuilder().build();
		List<LookupResult> results = resolver.resolve("activemq-61616.service.consul");

		String connectionString = "activemq:61616";

		if (!results.isEmpty()) {
			connectionString = results.get(0).host()+ ":" + results.get(0).port();
		}

		return new ActiveMQConnectionFactory("tcp://" + connectionString);
	}

	@Bean
	Broadcaster broadcaster(MessageSendingOperations<String> messagingTemplate,
			JmsTemplate jmsTemplate) {
		return new Broadcaster(messagingTemplate, jmsTemplate);
	}

	@Bean()
	public ThreadPoolTaskScheduler taskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setTaskScheduler(taskScheduler());
		taskRegistrar.addFixedRateTask(new Runnable() {
			public void run() {
				if (currentGame!=null && currentGame.getMoves()>currentGame.getPlayers().size()*500){
					currentGame.getPlayers().stream().forEach(p -> p.die());
					currentGame.stop();
				}
				if(currentGame!= null && currentGame.getBeurt()!=null && currentGame.getBeurt().isExpired(2000L)){
					currentGame.expireBeurt();
				}
			}
		}, 1000);
	}

	@Autowired
	private Broadcaster broadcaster;

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Battlecamp!";
	}

    @RequestMapping(value = "/generate", method = RequestMethod.GET)
    @ResponseBody
    public String generate(@RequestParam int rows, @RequestParam int columns) throws IOException {
        return RandomGameGenerator.generate(rows, columns);
    }

    @RequestMapping(value = "/file", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public FileSystemResource getFile(@RequestParam String id) {
        return new FileSystemResource("c:/java/game_" + id + ".json");
    }

    @RequestMapping(value = "/newEmptyGame", method = RequestMethod.POST)
	@ResponseBody
	public String newEmptyGame() {
		synchronized (lock) {
			if (currentGame == null || currentGame.isStopped()) {
				if(this.myBoard == null)
					myBoard = Board.random(20, 20, broadcaster);
				myBoard.resetBoard();
				Game game = new Game(myBoard, broadcaster);

				games.put(game.getId(), game);
				currentGame = game;

				broadcaster.broadcast("games", game);

				return game.getId();
			} else {
				broadcaster.broadcast("games", currentGame);
				return currentGame.getId();
			}

		}
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseBody
	public String newGame(@RequestParam String playerId,
			@RequestParam String playerColor,
			@RequestParam Player.Type playerType) {
		synchronized (lock) {
			if (currentGame == null || currentGame.isStopped()) {
				if(this.myBoard == null)
					myBoard = Board.random(20, 20, broadcaster);
				myBoard.resetBoard();
				Game game = new Game(myBoard, broadcaster);
				game.addPlayer(Player.newPlayer(playerId, playerType,
						playerColor, broadcaster));

				games.put(game.getId(), game);
				currentGame = game;
				broadcaster.broadcast("games", game);

				return game.getId();
			} else {
				broadcaster.broadcast("games", currentGame);
				return currentGame.getId();
			}
		}
	}

	@RequestMapping(value = "/join", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseStatus(value = HttpStatus.OK)
	public synchronized void join(@RequestParam String playerId,
			@RequestParam String playerColor,
			@RequestParam Player.Type playerType, @RequestParam String gameId) {
		Assert.isTrue(games.containsKey(gameId), "game " + gameId
				+ " bestaat niet");
		Game game = games.get(gameId);
		if (!game.isStarted() && !game.isStopped()) {
			if (game.getPlayers().stream()
					.noneMatch(player -> player.getId().equals(playerId))) {
				game.addPlayer(Player.newPlayer(playerId, playerType,
						playerColor, broadcaster));
			}
		} else {
			throw new IllegalArgumentException("game " + gameId
					+ " kan niet meer gejoined worden");
		}
	}

	@RequestMapping("/game")
	@ResponseBody
	public Game game(@RequestParam String id) {
		return games.get(id);
	}

	@RequestMapping(value = "/action", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseStatus(value = HttpStatus.OK)
	public void action(@RequestParam String gameId,
			@RequestParam String direction, @RequestParam String playerId)
			throws InterruptedException {

		Game game = games.get(gameId);
		if (game == null) {
			return;
		}
		if (game.isStopped()) {
			throw new IllegalArgumentException("game " + gameId
					+ " is al gestopt");
		}
		game.movePlayer(playerId, direction);
	}

	@RequestMapping(value = "/start", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void start(String gameId) throws InterruptedException {
		Assert.isTrue(games.containsKey(gameId), "game " + gameId
				+ " bestaat niet");
//		Game game = games.get(gameId);
		Game game = currentGame;
		if(game.getPlayers().isEmpty())
			return;
		if (!game.isStarted()) {
			game.start();
			if (!game.getPlayers().isEmpty()) {
				game.volgendeBeurt();
			}
		}
	}

	@RequestMapping(value = "/stop", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void stop(String gameId) throws InterruptedException {
		Assert.isTrue(games.containsKey(gameId), "game " + gameId
				+ " bestaat niet");
		Game game = games.get(gameId);
		game.stop();
	}
	@RequestMapping(value = "/stopCurrent", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void stop() throws InterruptedException {
		
		if (currentGame!=null && !currentGame.isStopped()){
			currentGame.stop();
		}
	}
	@RequestMapping(value = "/games", method = RequestMethod.GET)
	@ResponseBody
	public Collection<Game> games() {
		return games.values();
	}

	@RequestMapping(value = "/games", method = RequestMethod.DELETE)
	public void deleteGames() {
		// new ArrayList<Game>(games.values()).stream().filter(g ->
		// !g.isStopped()).forEach(g -> g.stop());
		games.clear();
	}

	@Bean
	public Filter simpleCORSFilter() {

		return new Filter() {

			public void doFilter(ServletRequest req, ServletResponse res,
					FilterChain chain) throws IOException, ServletException {
				HttpServletResponse response = (HttpServletResponse) res;
				response.setHeader("Access-Control-Allow-Origin", "*");
				response.setHeader("Access-Control-Allow-Methods",
						"POST, GET, OPTIONS, DELETE");
				response.setHeader("Allow", "POST, GET, OPTIONS, DELETE");
				response.setHeader("Access-Control-Max-Age", "3600");

				chain.doFilter(req, res);
			}

			public void init(FilterConfig filterConfig) {
			}

			public void destroy() {
			}
		};

	}

}
