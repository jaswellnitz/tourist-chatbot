package chatbot;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import util.JsonUtil;
import util.PropertyLoader;

// Sends requests to api.ai agent via its http api 
public class AgentHandler {

	private OkHttpClient client;
	private String clientAccessToken;

	public AgentHandler(String clientAccess) {
		this.client = new OkHttpClient();
		clientAccessToken = clientAccess;
	}

	
	public AgentResponse sendEvent(String event){
		return sendQuery(event, "");
	}
	
	public AgentResponse sendUserInput(String userInput){
		return sendQuery("", userInput);
	}
	// TODO error handling
	private AgentResponse sendQuery(String event, String userInput) {
		String url = buildQuery(event, userInput);
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
	private String buildQuery(String event, String userInput) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.api.ai/v1/query").newBuilder();
		if (!event.isEmpty()) {
			urlBuilder.addQueryParameter("e", event);
		}
		if (!userInput.isEmpty()) {
			urlBuilder.addQueryParameter("query", userInput);
		}
		urlBuilder.addQueryParameter("v", "20150910");
		urlBuilder.addQueryParameter("lang", "en");
		urlBuilder.addQueryParameter("sessionId", "1234567890");
		return urlBuilder.build().toString();
	}

}
