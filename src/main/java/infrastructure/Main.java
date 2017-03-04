package infrastructure;

import static spark.Spark.after;
import static spark.Spark.post;
import static spark.Spark.get;
import static spark.Spark.setPort;

import javax.annotation.PostConstruct;

import spark.Request;
import spark.Response;
import spark.Route;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import spark.ResponseTransformer;

public class Main {

	 public static void main(String[] args) {
		 setPort(Integer.valueOf(System.getenv("PORT")));
		 
		 get(new Route("/hello") {
             @Override
             public Object handle(Request request, Response response) { 
    			return "Hello World!";
             }
 });	 
		 
		 post(new Route("/test") {
             @Override
             public Object handle(Request request, Response response) {
     			Fulfillment fulfillment = new Fulfillment("Hello Speech", "Hello Display Test!", "touristbot test");
     			return JsonUtil.toJson(fulfillment);
             }
 });	
		
		 post(new Route("/webhook") {
             @Override
             public Object handle(Request request, Response response) {
//     			String jsonRequest = request.body().toString();
//     			JsonObject result = new JsonParser().parse(jsonRequest).getAsJsonObject().get("result").getAsJsonObject();
//     			String action = result.get("action").getAsString();
     			Fulfillment fulfillment = new Fulfillment("Hello Speech", "Hello Display Test!", "touristbot test");
     			return JsonUtil.toJson(fulfillment);
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

class JsonUtil {

	public static String toJson(Object object) {
		return new Gson().toJson(object);
	}

	public static ResponseTransformer json() {
		return JsonUtil::toJson;
	}

}
