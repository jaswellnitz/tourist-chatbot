package infrastructure;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import util.JsonUtil;
import util.PropertyLoader;

// Sends requests to api.ai agent via its http api 
public class AgentConnector {
	
	private OkHttpClient client;

	public AgentConnector(){
		this.client = new OkHttpClient();
		
	}
	
	public String sendQuery(String text){
		String url = buildQuery(text);

		String clientAccessToken = PropertyLoader.getProperty("clientAccessToken");
		Request request = new Request.Builder().header("Authorization", "Bearer "+ clientAccessToken)
				.url(url).build();
		
		// TODO: split into methods
		String jsonResponse = null;
		try {
			Response response = client.newCall(request).execute();
			jsonResponse = response.body().string();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String answer = JsonUtil.parseToJson(jsonResponse).get("result").getAsJsonObject().get("fulfillment").getAsJsonObject().get("speech").getAsString();
		return answer;
	}
	
	private String buildQuery(String text){
		HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.api.ai/v1/query").newBuilder();
		urlBuilder.addQueryParameter("query", text);
		urlBuilder.addQueryParameter("v", "20150910");
		urlBuilder.addQueryParameter("lang", "en");
		urlBuilder.addQueryParameter("sessionId", "1234567890");
		return urlBuilder.build().toString();
	}

}
