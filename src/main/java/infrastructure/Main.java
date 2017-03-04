package infrastructure;

import static spark.Spark.after;
import static spark.Spark.post;
import static spark.Spark.get;
import static spark.Spark.port;


import com.google.gson.Gson;
import spark.ResponseTransformer;

public class Main {

	 public static void main(String[] args) {
		 port(Integer.valueOf(System.getenv("PORT")));
		 

	     get("/hello", (req, res) -> "Hello World");
		
		 
		 post("/test",(req,res)-> {
     			Fulfillment fulfillment = new Fulfillment("Hello Speech", "Hello Display Test!", "touristbot test");
     			return JsonUtil.toJson(fulfillment);
             }
 );	
		 
		 post("/webhook",(req,res)-> {
//				String jsonRequest = request.body().toString();
//     			JsonObject result = new JsonParser().parse(jsonRequest).getAsJsonObject().get("result").getAsJsonObject();
//     			String action = result.get("action").getAsString();
  			Fulfillment fulfillment = new Fulfillment("Hello Speech", "Hello Display Test!", "touristbot test");
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
