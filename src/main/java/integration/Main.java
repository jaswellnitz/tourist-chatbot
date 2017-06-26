package integration;

import static spark.Spark.post;

import static spark.Spark.get;
import static spark.Spark.port;

import com.pengrad.telegrambot.request.SetWebhook;

import chatbot.TouristChatbot;
import dataAccess.PointDB;
import dataAccess.RatingDB;
import dataAccess.UserDB;
import recommender.Recommender;
import service.ImageRequester;
import service.agent.AgentHandler;

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
		String dbUrl = System.getenv("DATABASE_URL");
		String clientAccess = System.getenv("API_AI_ACCESS_TOKEN");
		String telegramToken = System.getenv("TELEGRAM_TOKEN");
		String foursquareClientId =  System.getenv("F_CLIENT_ID");
		String foursquareClientSecret = System.getenv("F_CLIENT_SECRET");
		PointDB pointConverter = new PointDB(dbUrl);
		RatingDB ratingDB = new RatingDB(dbUrl);
		Recommender recommender = new Recommender(pointConverter, ratingDB);
		UserDB userDB = new UserDB(dbUrl,pointConverter);
		AgentHandler agentConnector = new AgentHandler(clientAccess);
		ImageRequester imageRequester = new ImageRequester(foursquareClientId, foursquareClientSecret);
		TouristChatbot touristChatbot = new TouristChatbot(agentConnector, imageRequester, recommender, userDB, ratingDB);
		TelegramBotHandler botHandler = new TelegramBotHandler(telegramToken, touristChatbot);
		return botHandler;
	}
}
