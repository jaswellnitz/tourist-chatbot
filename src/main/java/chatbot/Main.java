package chatbot;

import static spark.Spark.post;

import static spark.Spark.get;
import static spark.Spark.port;

import com.pengrad.telegrambot.request.SetWebhook;

import dataAccess.DatabaseAccess;
import dataAccess.PointConverter;
import dataAccess.UserDB;
import dataAccess.UserRatingHandler;
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
		String dbUrl = System.getenv("JDBC_DATABASE_URL");
		String clientAccess = System.getenv("API_AI_ACCESS_TOKEN");
		String telegramToken = System.getenv("TELEGRAM_TOKEN");
		String foursquareClientId =  System.getenv("F_CLIENT_ID");
		String foursquareClientSecret = System.getenv("F_CLIENT_SECRET");
		DatabaseAccess dbAccess = new DatabaseAccess(dbUrl);
		PointConverter pointConverter = new PointConverter(dbAccess);
		Recommender recommender = new Recommender(pointConverter);
		UserDB userDB = new UserDB(dbAccess,pointConverter);
		AgentHandler agentConnector = new AgentHandler(clientAccess);
		ImageRequester imageRequester = new ImageRequester(foursquareClientId, foursquareClientSecret);
		UserRatingHandler userRatingHandler = new UserRatingHandler("src/main/resources/ratings.csv");
		TouristChatbot touristChatbot = new TouristChatbot(agentConnector, imageRequester, recommender, userDB, userRatingHandler);
		TelegramBotHandler botHandler = new TelegramBotHandler(telegramToken, touristChatbot);
		return botHandler;
	}
}
