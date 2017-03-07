package infrastructure;

import static spark.Spark.post;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.*;

import com.pengrad.telegrambot.request.SetWebhook;


//test
public class Main {

	public static void main(String[] args) {
			port(Integer.valueOf(System.getenv("PORT")));

	        get("/hello", (req, res) -> "Hello World");
	        post("/test", (req, res) -> "Post Test");
	        get("/test", (req, res) ->  "Get Test");

	        TestBotHandler botHandler = new TestBotHandler();
	        post("/" + botHandler.getToken(), botHandler);

	        String appSite = System.getenv("OPENSHIFT_APP_DNS");
	        botHandler.getBot().execute(new SetWebhook().url(appSite + "/" + botHandler.getToken()));
	}
}
