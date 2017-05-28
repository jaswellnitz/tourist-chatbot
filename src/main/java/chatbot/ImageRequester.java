package chatbot;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.Location;
import util.ServiceRequester;

public class ImageRequester extends ServiceRequester {

	private String clientId;
	private String clientSecret;

	public ImageRequester(String clientId, String clientSecret) {
		super();
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	public String getImageURL(String name, Location location) {
		String url = "";
		String id = getVenueId(name, location);
		if(!id.isEmpty()){
			url = getImageForVenueId(id);
		}
		return url;
	}

	private String getVenueId(String name, Location location) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("v", "20170101");
		parameters.put("client_id", clientId);
		parameters.put("client_secret", clientSecret);
		parameters.put("ll", location.getLatitude() + "," + location.getLongitude());
		parameters.put("query", name);
		parameters.put("radius", String.valueOf(100));
		parameters.put("intent", "match");
		String query = buildQuery("https://api.foursquare.com/v2/venues/search", parameters);
		JsonObject jsonObject = sendQuery("", "", query);
		JsonArray venues = jsonObject.get("response").getAsJsonObject().get("venues").getAsJsonArray();
		String id = "";
		if(venues.size() > 0){
			id =  venues.get(0).getAsJsonObject().get("id").getAsString();
			
		}
		return id;
	}

	private String getImageForVenueId(String id){
		assert id != null: "Precondition failed: id != null";
		assert !id.isEmpty(): "Precondition failed: !id.isEmpty()";
		
		String url = "";
		Map<String,String> parameters = new HashMap<>();
		parameters.put("v","20170101");
		parameters.put("client_id", clientId);
		parameters.put("client_secret", clientSecret);
		String photos = "https://api.foursquare.com/v2/venues/"+id+"/photos";
		String query = buildQuery(photos, parameters);
		JsonObject jsonObject = sendQuery("","",query);
		JsonArray items = jsonObject.get("response").getAsJsonObject().get("photos").getAsJsonObject().get("items").getAsJsonArray();
		if(items.size() > 0){
			JsonObject item = items.get(0).getAsJsonObject();
			url = item.get("prefix").getAsString() + "width200" + item.get("suffix").getAsString();
		}
		return url;
		
	}
}
