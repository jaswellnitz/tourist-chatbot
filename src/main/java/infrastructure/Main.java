package infrastructure;

import static spark.Spark.after;
import static spark.Spark.post;
import static spark.Spark.get;
import spark.Request;
import spark.Response;
import spark.Route;

import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Main {

	 public static void main(String[] args) {
		 get(new Route("/hello") {
             @Override
             public Object handle(Request request, Response response) { 
    			return "Hello World!";
             }
 });	 
		
		 post(new Route("/webhook") {
             @Override
             public Object handle(Request request, Response response) {
     			String jsonRequest = request.body().toString();
     			JsonObject result = new JsonParser().parse(jsonRequest).getAsJsonObject().get("result").getAsJsonObject();
     			String action = result.get("action").getAsString();
     			Fulfillment fulfillment = new Fulfillment("Hello Speech", "Hello Display Test!, action: " + action, "touristbot test");
     			return new Gson().toJson(fulfillment);
             }
 });	 
		after(new spark.Filter() {
			
			@Override
			public void handle(Request req, Response res) {
				res.type("application/json");
				
			}
		});
	 }
}