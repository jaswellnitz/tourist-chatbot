package infrastructure;

import static spark.Spark.post;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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

	        Properties properties = new Properties();
	         try {
				properties.load(new FileInputStream("config.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
	         System.out.println(properties.get("serviceurl"));
	         
	        botHandler.getBot().execute(new SetWebhook().url(properties.getProperty("serviceurl") + botHandler.getToken()));
	}
}
