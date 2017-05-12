package chatbot;

import java.io.IOException;

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

	
	public AgentResponse sendEvent(String event, long sessionId){
		return sendQuery(event, "", sessionId);
	}
	
	public AgentResponse sendUserInput(String userInput, long sessionId){
		return sendQuery("", userInput, sessionId);
	}
	// TODO error handling
	private AgentResponse sendQuery(String event, String userInput, long sessionId) {
		String url = buildQuery(event, userInput, sessionId);
		Request request = new Request.Builder().header("Authorization", "Bearer " + clientAccessToken).url(url).build();

		String jsonResponse = null;
		try {
			Response response = client.newCall(request).execute();
			jsonResponse = response.body().string();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return AgentResponseParser.fromJson(jsonResponse);
	}

	// TODO check sessionId and metadata
	private String buildQuery(String event, String userInput, long sessionId) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.api.ai/v1/query").newBuilder();
		if (!event.isEmpty()) {
			urlBuilder.addQueryParameter("e", event);
		}
		if (!userInput.isEmpty()) {
			urlBuilder.addQueryParameter("query", userInput);
		}
		urlBuilder.addQueryParameter("v", "20150910");
		urlBuilder.addQueryParameter("lang", "en");
		String sessionIdStr = String.valueOf(sessionId);
		if(sessionIdStr.length() < 10){
			 sessionIdStr = String.format("%10s", sessionIdStr).replace(" ", "0");
		}
		urlBuilder.addQueryParameter("sessionId", sessionIdStr);
		return urlBuilder.build().toString();
	}

}
