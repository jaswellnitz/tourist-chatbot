package service;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An abstract class that defines the API access.
 * @author Jasmin Wellnitz
 *
 */
public abstract class ServiceRequester {
	private OkHttpClient httpClient;
	protected Logger logger = Logger.getLogger(this.getClass());

	protected ServiceRequester() {
		this.httpClient = new OkHttpClient();
	}

	/**
	 * Builds the HTTP query based on the query start and given parameters.
	 * @param query - the query start
	 * @param parameters Map with parameters in key value format
	 * @return the complete query
	 */
	protected String buildQuery(String query, Map<String, String> parameters) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(query).newBuilder();
		for (Entry<String, String> parameter : parameters.entrySet()) {
			urlBuilder.addQueryParameter(parameter.getKey(), parameter.getValue());
		}
		return urlBuilder.build().toString();
	};

	/**
	 * Sends the specified url by creating a HTTP request
	 * @param header
	 * @param headerValue
	 * @param url 
	 * @return the JSON response
	 */
	protected JsonObject sendQuery(String header, String headerValue, String url) {
		Request request;
		if (header.isEmpty() || headerValue.isEmpty()) {
			request = new Request.Builder().url(url).build();
		} else {
			request = new Request.Builder().header(header, headerValue).url(url).build();
		}
		String jsonResponse = null;
		try {
			Response response = httpClient.newCall(request).execute();
			jsonResponse = response.body().string();
		} catch (Exception e) {
			logger.error(e);
		}
		JsonObject jsonObject = null;
		if(jsonResponse != null){
			jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
		}
		return jsonObject;
	}

}
