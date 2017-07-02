package domain;

import java.util.List;

import recommender.POIProfile;
import recommender.Preference;
import recommender.ProfileItem;

/**
 * A point of interest that is recommended to the user.
 * @author Jasmin Wellnitz
 *
 */
public class RecommendedPointOfInterest implements ProfileItem {

	private static final long serialVersionUID = -8542663924097410195L;
	private final long id;
	private final String name;
	private final String address;
	private final String openingHours;
	private final POIProfile profile;
	private final Location location;
	private final int distanceToUser;
	private float recommendationValue;
	private final static int UNSET_RECOMMENDATION_VALUE = 0;
	
	/**
	 * @param id the id given by OpenStreetMap
	 * @param name the OpenStreetMap name
	 * @param location coordinates of the POI
	 * @param streetName address - street name
	 * @param houseNumber address - house number
	 * @param distance distance to the user
	 * @param openingHours opening hours
	 * @param profile profile that characterizes the POI
	 */
	public RecommendedPointOfInterest(long id, String name, Location location, String streetName, String houseNumber, int distance,
			String openingHours, POIProfile profile){
		this.id = id;
		this.name = name != null? name: "";
		this.address = parseAddress(streetName, houseNumber);
		this.distanceToUser = distance;
		this.openingHours = openingHours != null? openingHours: "";
		this.profile = profile;
		this.recommendationValue = UNSET_RECOMMENDATION_VALUE;
		this.location = location;
	}

	/**
	 * Gets the POI's OSM id.
	 * @return id
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * Gets the POI's OSM name.
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the POI's address. 
	 * @return address; an empty string is returned if no address was specified.
	 */
	public String getAddress() {
		return address;
	}

	@Override
	public POIProfile getProfile() {
		return profile;
	}

	/**
	 * Gets the distance to the user to whom the POI was recommended.
	 * @return distance to user in meter
	 */
	public int getDistance() {
		return distanceToUser;
	}

	/**
	 * Gets the POI's opening hours.
	 * @return the opening hours;  an empty string is returned if no opening hours were specified.
	 */
	public String getOpeningHours() {
		return openingHours;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((openingHours == null) ? 0 : openingHours.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecommendedPointOfInterest other = (RecommendedPointOfInterest) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (openingHours == null) {
			if (other.openingHours != null)
				return false;
		} else if (!openingHours.equals(other.openingHours))
			return false;
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id + "(: " + name + ", " + address + ", " + distanceToUser + ", " + openingHours + "," + recommendationValue +")";
	}
	
	/**
	 * Gets a user readable string representation of the POI.
	 * @param showDistance indicates whether the distance to the user is shown or not
	 * @return a formatted representation of the POI
	 */
	public String getFormattedString(boolean showDistance){
		String ret = name+ ": ";
		if(showDistance && distanceToUser != -1){
			ret+="\ndistance to you -" + distanceToUser + " m, ";
		}
		if(!address.isEmpty()){
			ret += "\n"+address + ", ";
		}
		
		if(!openingHours.isEmpty()){
			ret += "\n"+openingHours +", ";
		}
		if(recommendationValue > 0.1){
			ret += "\nyour computed preference value: " + recommendationValue + ", ";
		}

		List<String> interestsFromProfile = profile.getInterestsFromProfile(Preference.TRUE);
		if(!interestsFromProfile.isEmpty()){
			ret += "\ncategory - ";
			for(String interest: interestsFromProfile){
				ret += interest + ", ";
			}
		}
		
		return ret.substring(0, ret.length()-2) +".";
	}

	/**
	 * Gets the recommendation value calculated by the recommender.
	 * @return recommedantion value
	 */
	public float getRecommendationValue() {
		return recommendationValue;
	}

	/**
	 * Sets the recommendation value
	 * @param recommendationValue the recommendation value
	 */
	public void setRecommendationValue(float recommendationValue) {
		this.recommendationValue = recommendationValue;
	}
	
	/**
	 * Shows the POI's location.
	 * @return
	 */
	public Location getLocation(){
		return location;
	}
	
	/**
	 * Combines the given street name and house number into an address string
	 * @param streetName street name
	 * @param houseNumber house number
	 * @return the address
	 */
	private final String parseAddress(String streetName, String houseNumber) {
		String s = "";
		String h = "";
		if (streetName != null && !streetName.equals("")) {
			s = streetName;
			if (houseNumber != null && !houseNumber.equals("")) {
				h = " " + houseNumber;
			}
		}
		return s + h;
	}

}
