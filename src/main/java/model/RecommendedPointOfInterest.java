package model;

// Recommended Point of Interest
public class RecommendedPointOfInterest implements ProfileItem {

	private final long id;
	private final String name;
	private final String address;
	private final String openingHours;
	private final POIProfile profile;
	private final int distanceToUser;
	private float recommendationValue;
	private final static int UNSET_RECOMMENDATION_VALUE = 0;
	
	public RecommendedPointOfInterest(long id, String name, String streetName, String houseNumber, int distance,
			String openingHours, POIProfile profile){
		this(id,name,streetName,houseNumber,distance,openingHours, profile,UNSET_RECOMMENDATION_VALUE);
	}

	public RecommendedPointOfInterest(long id, String name, String streetName, String houseNumber, int distance,
			String openingHours, POIProfile profile, int recommendationValue) {
		this.id = id;
		this.name = name != null? name: "";
		this.address = parseAddress(streetName, houseNumber);
		this.distanceToUser = distance;
		this.openingHours = openingHours != null? openingHours: "";
		this.profile = profile;
		this.recommendationValue = recommendationValue;
	}

	public final String parseAddress(String streetName, String houseNumber) {
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

	@Override
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public POIProfile getProfile() {
		return profile;
	}

	public int getDistance() {
		return distanceToUser;
	}

	public String getOpeningHours() {
		return openingHours;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + distanceToUser;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		result = prime * result + ((openingHours == null) ? 0 : openingHours.hashCode());
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
		if (distanceToUser != other.distanceToUser)
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
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
	
	public String getFormattedString(){
		String ret = name +": distance to your current location - " + distanceToUser + " m, ";
		if(!address.isEmpty()){
			ret += address + ", ";
		}
		if(!openingHours.isEmpty()){
			ret += openingHours +", ";
		}
		if(recommendationValue != 0.0){
			ret += "your computed preference value: " + recommendationValue + ", ";
		}

		return ret.substring(0, ret.length()-2) +".";
	}

	public float getRecommendationValue() {
		return recommendationValue;
	}

	public void setRecommendationValue(float recommendationValue) {
		this.recommendationValue = recommendationValue;
	}

}
