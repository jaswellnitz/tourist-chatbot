package messenger;

import static spark.Spark.post;

import static spark.Spark.port;

import com.pengrad.telegrambot.request.SetWebhook;

import chatbot.TouristChatbot;
import dataAccess.PointDB;
import dataAccess.RatingDB;
import dataAccess.UserDB;
import recommender.Recommender;
import service.ImageRequester;
import service.agent.AgentHandler;

/**
 * The application's entry point. Starts the web application, enables the Telegram webhook and initializes all of the system's components. 
 * @author Jasmin Wellnitz
 *
 */
public class Main {

	/**
	 * Starts the web application and enables the Telegram webhook.
	 * @param args
	 */
	public static void main(String[] args) {
		// Getting the system environment variables
		String port = System.getenv("PORT");
		String dbUrl = System.getenv("DATABASE_URL");
		String clientAccess = System.getenv("API_AI_ACCESS_TOKEN");
		String telegramToken = System.getenv("TELEGRAM_TOKEN");
		String foursquareClientId =  System.getenv("F_CLIENT_ID");
		String foursquareClientSecret = System.getenv("F_CLIENT_SECRET");
		String serviceUrl = System.getenv("HEROKU_URL");
		
		TelegramBotHandler botHandler = initBotHandler(dbUrl, clientAccess, telegramToken, foursquareClientId, foursquareClientSecret);
		
		port(Integer.valueOf(port));
		// Setting the webhook
		post("/" + telegramToken, botHandler);
		botHandler.getTelegramBot().execute(new SetWebhook().url(serviceUrl + telegramToken));
	}

	/**
	 * Initializes the application's components using dependency injection.
	 * @return
	 */
	private static TelegramBotHandler initBotHandler(String dbUrl, String clientAccess, String telegramToken, String foursquareClientId, String foursquareClientSecret) {
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
