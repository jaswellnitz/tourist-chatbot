package infrastructure;

import static spark.Spark.after;
import static spark.Spark.post;
import static spark.Spark.get;
import static spark.Spark.port;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import spark.ResponseTransformer;
//test
public class Main {

	 public static void main(String[] args) {
		 port(Integer.valueOf(System.getenv("PORT")));
		 

	     get("/hello", (req, res) -> "Hello World");
		
		 
		 post("/test",(req,res)-> {
     			Fulfillment fulfillment = new Fulfillment("This is a test", "This is a test!");
     			return JsonUtil.toJson(fulfillment);
             }
 );	
		 
		 post("/webhook",(req,res)-> {
				String jsonRequest = req.body().toString();
     			JsonObject result = new JsonParser().parse(jsonRequest).getAsJsonObject().get("result").getAsJsonObject();
     			String resolvedQuery = result.get("resolvedQuery").getAsString();
     			String action = result.get("action").getAsString();
  			Fulfillment fulfillment = new Fulfillment("This is a test. Asked query" + resolvedQuery + ", asked action: " + action, "This is a test.");
  			return JsonUtil.toJson(fulfillment);
          }
);	
		
		 after((req, res) -> {
				res.type("application/json");
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
