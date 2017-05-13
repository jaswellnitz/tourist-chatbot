package chatbot;

import static spark.Spark.post;

import static spark.Spark.get;
import static spark.Spark.port;

import com.pengrad.telegrambot.request.SetWebhook;

import data_access.DatabaseAccess;
import data_access.PointConverter;
import data_access.UserDB;
import recommender.Recommender;

// Entry Point: enables Telegram webhook
public class Main {

	public static void main(String[] args) {
		port(Integer.valueOf(System.getenv("PORT")));

		get("/hello", (req, res) -> "Hello World");

		TelegramBotHandler botHandler = initBotHandler();
		post("/" + botHandler.getToken(), botHandler);

		String serviceUrl = System.getenv("HEROKU_URL");
		botHandler.getTelegramBot().execute(new SetWebhook().url(serviceUrl + botHandler.getToken()));
	}

	private static TelegramBotHandler initBotHandler() {
		/*
		 * DatabaseAccess dbAccess = new
		 * DatabaseAccess(PropertyLoader.getProperty("db_name"),
		 * PropertyLoader.getProperty("db_user"),
		 * PropertyLoader.getProperty("db_pw")); PointConverter pointConverter =
		 * new PointConverter(dbAccess); Recommender recommender = new
		 * Recommender(pointConverter);
		UserDB userDB = new UserDB(dbAccess);
		 */
		String dbUrl = System.getenv("JDBC_DATABASE_URL");
		String clientAccess = System.getenv("API_AI_ACCESS_TOKEN");
		String telegramToken = System.getenv("TELEGRAM_TOKEN");
		DatabaseAccess dbAccess = new DatabaseAccess(dbUrl);
		UserDB userDB = new UserDB(dbAccess);
		PointConverter pointConverter = new PointConverter(dbAccess);
//		Recommender recommender = new Recommender(pointConverter);
		AgentHandler agentConnector = new AgentHandler(clientAccess);
		TouristChatbot touristChatbot = new TouristChatbot(agentConnector, userDB);
		TelegramBotHandler botHandler = new TelegramBotHandler(telegramToken, touristChatbot);
		return botHandler;
	}
}
