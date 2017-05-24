package chatbot;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// Sends requests to api.ai agent via its http api 
public class AgentHandler {

	private OkHttpClient client;
	private String clientAccessToken;

	public AgentHandler(String clientAccess) {
		this.client = new OkHttpClient();
		clientAccessToken = clientAccess;
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

	// TODO error handling
	private AgentResponse sendQuery(String event, String userInput, String context, long sessionId,
			boolean resetContext) {
		String url = buildQuery(event, userInput, context, sessionId, resetContext);
		Request request = new Request.Builder().header("Authorization", "Bearer " + clientAccessToken).url(url).build();
		String jsonResponse = null;
		try {
			Response response = client.newCall(request).execute();
			jsonResponse = response.body().string();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return AgentResponseParser.fromJson(jsonResponse);
	}

	private String buildQuery(String event, String userInput, String context, long sessionId, boolean resetContext) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.api.ai/v1/query").newBuilder();
		if (!event.isEmpty()) {
			urlBuilder.addQueryParameter("e", event);
		}
		if (!userInput.isEmpty()) {
			urlBuilder.addQueryParameter("query", userInput);
		}
		urlBuilder.addQueryParameter("v", "20150910");
		urlBuilder.addQueryParameter("lang", "en");

		if (resetContext) {
			urlBuilder.addQueryParameter("resetContexts", String.valueOf(resetContext));
		}
		if (!context.isEmpty()) {
			urlBuilder.addQueryParameter("contexts", "Rate");
		}

		String sessionIdStr = String.valueOf(sessionId);
		if (sessionIdStr.length() < 10) {
			sessionIdStr = String.format("%10s", sessionIdStr).replace(" ", "0");
		}
		urlBuilder.addQueryParameter("sessionId", sessionIdStr);
		return urlBuilder.build().toString();
	}

	public AgentResponse setContext(String context, long sessionId) {
		return sendQuery("", "dummy", context, sessionId, false);
	}

}
