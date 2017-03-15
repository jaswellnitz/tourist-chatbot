package util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import spark.ResponseTransformer;

// Utitily class to convert from and to JSON using Gson
public class JsonUtil {

	public static String toJson(Object object) {
		return new Gson().toJson(object);
	}
	
	public static JsonObject parseToJson(String text){
		return new JsonParser().parse(text).getAsJsonObject();
	}
	
	public static ResponseTransformer json() {
		return JsonUtil::toJson;
	}
}