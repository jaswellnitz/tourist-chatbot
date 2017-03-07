package infrastructure;

import static spark.Spark.post;

import static spark.Spark.get;
import static spark.Spark.port;

import com.pengrad.telegrambot.request.SetWebhook;

import util.PropertyLoader;


public class Main {

	public static void main(String[] args) {
			port(Integer.valueOf(System.getenv("PORT")));

	        get("/hello", (req, res) -> "Hello World");
	        

	        TestBotHandler botHandler = new TestBotHandler();
	        post("/" + botHandler.getToken(), botHandler);

	        String serviceUrl = PropertyLoader.getProperty("serviceUrl");
	        botHandler.getBot().execute(new SetWebhook().url(serviceUrl + botHandler.getToken()));
	}
}
