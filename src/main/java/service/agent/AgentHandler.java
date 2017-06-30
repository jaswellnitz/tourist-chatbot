package service.agent;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import service.ServiceRequester;

/**
 * Sends requests to api.ai agent via its http api 
 * @author Jasmin Wellnitz
 *
 */
public class AgentHandler extends ServiceRequester{
	private String clientAccessToken;

	/**
	 * Creates the API.AI agent handler.
	 * @param clientAccess API.AI client access token
	 */
	public AgentHandler(String clientAccess) {
		this.clientAccessToken = clientAccess;
	}

	/**
	 * Sends an API.AI event
	 * @param event
	 * @param sessionId specifies the conversation
	 * @param resetContext boolean that indicates whether the current context should be reset
	 * @return the agent response
	 */
	public AgentResponse sendEvent(String event, long sessionId, boolean resetContext) {
		return sendQuery(event, "", "", sessionId, resetContext);
	}

	/**
	 * Sends the user input to API.AI
	 * @param userInput
	 * @param sessionId specifies the conversation
	 * @return the agent response
	 */
	public AgentResponse sendUserInput(String userInput, long sessionId) {
		return sendQuery("", userInput, "", sessionId, false);
	}

	/**
	 * Resets the current context of the conversation.
	 * @param sessionId specifies the conversation
	 * @return the agent response
	 */
	public AgentResponse resetContext(long sessionId) {
		return sendQuery("", "reset", "", sessionId, true);
	}

	/**
	 * Sets the context of the conversation.
	 * @param context
	 * @param sessionId specifies the conversation
	 * @return the agent response
	 */
	public AgentResponse setContext(String context, long sessionId) {
		return sendQuery("", "dummy", context, sessionId, false);
	}

	/**
	 * Sends a query to API.AI
	 * @param event
	 * @param userInput
	 * @param context
	 * @param sessionId
	 * @param resetContext
	 * @return the agent response
	 */
	private AgentResponse sendQuery(String event, String userInput, String context, long sessionId,
			boolean resetContext) {
		
		System.out.println("AGENTHANDLER - Telegram: " + userInput);
		checkContexts(sessionId);
		String url = buildQuery(event, userInput, context, sessionId, resetContext);
		JsonObject jsonObject= sendQuery("Authorization", "Bearer " + clientAccessToken, url).getAsJsonObject();
		
		AgentResponse resp = null;
		if(jsonObject != null){
			resp = AgentResponseParser.fromJson(jsonObject);
		}
		System.out.println("AGENTHANDLER - Response: " + resp.getReply() + "," + resp.getContexts());
		checkContexts(sessionId);
		return resp;
	}

	public void checkContexts(long sessionId){
		Map<String,String> map= new HashMap<>();
		String sessionIdStr = String.valueOf(sessionId);
		if (sessionIdStr.length() < 10) {
			sessionIdStr = String.format("%10s", sessionIdStr).replace(" ", "0");
		}
		map.put("sessionId", sessionIdStr);
		String url = buildQuery("https://api.api.ai/v1/contexts?",map);
		
		JsonArray jsonObject= sendQuery("Authorization", "Bearer " + clientAccessToken, url).getAsJsonArray();
		System.out.println(jsonObject);
	}
	/**
	 * Builds a query based on API.AI's API documentation: 
	 * https://docs.api.ai/docs/query
	 * @param event
	 * @param userInput
	 * @param context
	 * @param sessionId
	 * @param resetContext
	 * @return the query
	 */
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
