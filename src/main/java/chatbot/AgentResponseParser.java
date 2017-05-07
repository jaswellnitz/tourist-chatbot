package chatbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import util.JsonUtil;

public class AgentResponseParser {

	private static Map<String, Object> parseParameters(JsonObject resultObject) {
		Map<String, Object> parameters = new HashMap<>();
		if (resultObject.has("parameters")) {
			for (Entry<String, JsonElement> entry : resultObject.get("parameters").getAsJsonObject().entrySet()) {
				if (entry.getKey().equals("distance")) {
					int distance = parseDistance(entry.getValue());
					parameters.put(entry.getKey(), distance);
				} else {
					parameters.put(entry.getKey(), entry.getValue().getAsString());
				}
			}
		}
		return parameters;
	}

	// TODO error handling
	private static int parseDistance(JsonElement value) {
		JsonObject jsonObject = value.getAsJsonObject();
		double amount = jsonObject.get("amount").getAsDouble();
		String unit = jsonObject.get("unit").getAsString();
		if (unit.equals("km")) {
			amount *= 1000;
		} else if (!unit.equals("m")) {
			return 0;
		}
		return (int) amount;

	}

	public static AgentResponse fromJson(String jsonResponse) {
		JsonObject resultObject = JsonUtil.parseToJson(jsonResponse).get("result").getAsJsonObject();
		Map<String, Object> parameters = parseParameters(resultObject);
		String source = resultObject.get("source").getAsString();
		String resolvedQuery = resultObject.get("resolvedQuery").getAsString();
		Action action = Action.getEnum(resultObject.get("action").getAsString());
		String reply = resultObject.get("fulfillment").getAsJsonObject().get("speech").getAsString();
		double score = resultObject.get("score").getAsDouble();

		List<Context> contexts = new ArrayList<Context>();
		if (resultObject.has("contexts")) {
			JsonArray jsonArray = resultObject.get("contexts").getAsJsonArray();

			for (int i = 0; i < jsonArray.size(); i++) {
				JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
				String name = jsonObject.get("name").getAsString();
				Map<String, Object> contextParameters = parseParameters(jsonObject);
				int lifespan = jsonObject.get("lifespan").getAsInt();
				contexts.add(new Context(name, contextParameters, lifespan));
			}
		}
		return new AgentResponse(source, resolvedQuery, action, parameters, contexts, reply, score);
	}

}