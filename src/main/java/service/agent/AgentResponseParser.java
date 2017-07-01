package service.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import chatbot.Action;
/**
 * Utility class that parses the json agent response into a java representation.
 * @author Jasmin Wellnitz
 *
 */
public class AgentResponseParser {

	private static Map<String, Object> parseParameters(JsonObject resultObject) {
		Map<String, Object> parameters = new HashMap<>();
		if (resultObject.has("parameters")) {
			for (Entry<String, JsonElement> entry : resultObject.get("parameters").getAsJsonObject().entrySet()) {
				if (entry.getValue().isJsonArray()) {
					List<String> list = new ArrayList<>();
					for (JsonElement jsonElement : entry.getValue().getAsJsonArray()) {
						list.add(jsonElement.getAsString());
					}
					parameters.put(entry.getKey(), list);
				} else if (Parameter.getEnum(entry.getKey()) == Parameter.DISTANCE && entry.getValue().isJsonObject()) {
					int distance = parseDistance(entry.getValue());
					parameters.put(Parameter.DISTANCE.name(), distance);
				} else {
					parameters.put(entry.getKey(), entry.getValue().getAsString());
				}
			}
		}
		return parameters;
	}

	private static int parseDistance(JsonElement value) {
		JsonObject jsonObject = value.getAsJsonObject();
		double amount = jsonObject.get("amount").getAsDouble();
		String unit = jsonObject.get("unit").getAsString();
		if (unit.equals("km")) {
			amount *= 1000;
		} else if (unit.equals("m")) {
			amount *= 1;
		} else {
			amount = -1;
		}
		return (int) amount;

	}

	/**
	 * Parses the agent's response from JSON to the Java object AgentResponse
	 * @param responseObject json response
	 * @return the agent response as a Java object
	 */
	public static AgentResponse fromJson(JsonObject responseObject) {
		assert responseObject != null : "Precondition failed: responseObject != null";
		JsonObject resultObject = responseObject.get("result").getAsJsonObject();
		Map<String, Object> parameters = parseParameters(resultObject);

		String resolvedQuery = resultObject.get("resolvedQuery").getAsString();
		Action action = Action.getEnum(resultObject.get("action").getAsString());
		String reply = resultObject.get("fulfillment").getAsJsonObject().get("speech").getAsString();

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
		return new AgentResponse(resolvedQuery, action, parameters, contexts, reply);
	}

}
