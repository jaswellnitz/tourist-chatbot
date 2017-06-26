package service.agent;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import service.ServiceRequester;

// Sends requests to api.ai agent via its http api 
public class AgentHandler extends ServiceRequester{
	private String clientAccessToken;

	public AgentHandler(String clientAccess) {
		this.clientAccessToken = clientAccess;
	}

	public AgentResponse sendEvent(String event, long sessionId, boolean resetContext) {
		return sendQuery(event, "", "", sessionId, resetContext);
	}

	public AgentResponse sendEvent(String event, long sessionId) {
		return sendQuery(event, "", "", sessionId, false);
	}

	public AgentResponse sendUserInput(String userInput, long sessionId) {
		return sendQuery("", userInput, "", sessionId, false);
	}

	public AgentResponse resetContext(long sessionId) {
		return sendQuery("", "reset", "", sessionId, true);
	}

	public AgentResponse setContext(String context, long sessionId) {
		return sendQuery("", "dummy", context, sessionId, false);
	}

	private AgentResponse sendQuery(String event, String userInput, String context, long sessionId,
			boolean resetContext) {
		
		String url = buildQuery(event, userInput, context, sessionId, resetContext);
		JsonObject jsonObject= sendQuery("Authorization", "Bearer " + clientAccessToken, url);
		
		return AgentResponseParser.fromJson(jsonObject);
	}

	private String buildQuery(String event, String userInput, String context, long sessionId, boolean resetContext) {
		Map<String,String> map= new HashMap<>();
		if (!event.isEmpty()) {
			map.put("e", event);
		}
		if (!userInput.isEmpty()) {
			map.put("query", userInput);
		}
		map.put("v", "20150910");
		map.put("lang", "en");

		if (resetContext) {
			map.put("resetContexts", String.valueOf(resetContext));
		}
		if (!context.isEmpty()) {
			map.put("contexts", context);
		}

		String sessionIdStr = String.valueOf(sessionId);
		if (sessionIdStr.length() < 10) {
			sessionIdStr = String.format("%10s", sessionIdStr).replace(" ", "0");
		}
		map.put("sessionId", sessionIdStr);
		return buildQuery("https://api.api.ai/v1/query",map);
	}
}
