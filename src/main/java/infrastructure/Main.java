package infrastructure;

import static spark.Spark.after;
import static spark.Spark.post;

import spark.Request;
import spark.Response;
import spark.Route;

import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Main {

	 public static void main(String[] args) {
		 post(new Route("/webhook") {
             @Override
             public Object handle(Request request, Response response) {
     			String jsonRequest = request.body().toString();
     			JsonObject result = new JsonParser().parse(jsonRequest).getAsJsonObject().get("result").getAsJsonObject();
     			System.out.println(result.getAsString());
     			AgentResponse agentResponse = new AgentResponse("Hello Speech", "Hello Display Test!", "touristbot test");
     			return new Gson().toJson(agentResponse);
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