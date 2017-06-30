package service;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import domain.Location;

/**
 * Encapsulates the access of the Foursquare API. Requests image URLs for POI.
 * @author Jasmin Wellnitz
 *
 */
public class ImageRequester extends ServiceRequester {

	private String clientId;
	private String clientSecret;

	/**
	 * Creates an ImageRequester based on the Foursquare credentials
	 * @param clientId Foursquare client id
	 * @param clientSecret Foursquare client secret
	 */
	public ImageRequester(String clientId, String clientSecret) {
		super();
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	/**
	 * Gets the image url for a point of interest.
	 * @param name  the POI name
	 * @param location the POI location
	 * @return the image url; if no image was found, an empty string is returned
	 */
	public String getImageURL(String name, Location location) {
		String url = "";
		String id = getVenueId(name, location);
		if(!id.isEmpty()){
			url = getImageForVenueId(id);
		}
		return url;
	}
	/** Gets the corresponding Foursquare venue id for the given POI
	 * Uses Foursquare's venue search endpoint
	 * https://developer.foursquare.com/docs/venues/search
	 * @param name POI name
	 * @param location POI location
	 * @return venue id
	 */
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
		JsonObject jsonObject = sendQuery("", "", query).getAsJsonObject();
		JsonArray venues = jsonObject.get("response").getAsJsonObject().get("venues").getAsJsonArray();
		String id = "";
		if(venues.size() > 0){
			id =  venues.get(0).getAsJsonObject().get("id").getAsString();
			
		}
		return id;
	}

	/**
	 * Gets the image url for the venue id, using the following API endpoint:
	 * https://developer.foursquare.com/docs/venues/photos
	 * @param id Foursquare venue id
	 * @return image url
	 */
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
		JsonObject jsonObject = sendQuery("","",query).getAsJsonObject();
		JsonArray items = jsonObject.get("response").getAsJsonObject().get("photos").getAsJsonObject().get("items").getAsJsonArray();
		if(items.size() > 0){
			JsonObject item = items.get(0).getAsJsonObject();
			url = item.get("prefix").getAsString() + "width200" + item.get("suffix").getAsString();
		}
		return url;
		
	}
}
