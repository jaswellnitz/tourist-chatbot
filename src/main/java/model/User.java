package model;

public class User implements ProfileItem {

	private final long id;
	
	private final String name;
	private POIProfile profile;
	private int prefRecommendationRadius;
	private static final int DEFAULT_RECOMMENDATION_RADIUS = 3000;
	private Location currentLocation;
	
	
	// TODO refactor constructors

	public User(long id, String name){
		this(id, new POIProfile(), DEFAULT_RECOMMENDATION_RADIUS, null, name);
	}
	
	public User(long id, Location location){
		this(id, new POIProfile(), location);
	}
	
	public User(long id, POIProfile profile, Location location) {
		this(id,profile,DEFAULT_RECOMMENDATION_RADIUS, location, "");
	}
	
	public User(long id, POIProfile profile) {
		this(id,profile,DEFAULT_RECOMMENDATION_RADIUS, null, "");
	}

	public User(long id, POIProfile profile, int radius, Location location, String name){
		this.id = id;
		this.profile = profile;
		this.prefRecommendationRadius = radius;
		this.currentLocation = location;
		this.name = name;
	}
	
	public long getId() {
		return id;
	}

	public POIProfile getProfile() {
		return profile;
	}
	
	public void setProfile(POIProfile profile) {
		assert profile != null: "Precondition failed: profile != null";
		this.profile = profile;
	}

	public Location getCurrentLocation() {
		assert currentLocation != null: "Precondition failed: currentLocation != null";
		
		return currentLocation;
	}
	
	public void setCurrentLocation(Location currentLocation) {
		assert currentLocation != null: "Precondition failed: currentLocation != null";
		
		this.currentLocation = currentLocation;
	}
	
	public void setCurrentLocation(double latitude, double longitude) {
		this.currentLocation = new Location(latitude,longitude);
	}

	public String toString(){
		return "User:("+id+", " + profile+", " + prefRecommendationRadius +", " + currentLocation +")";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentLocation == null) ? 0 : currentLocation.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + prefRecommendationRadius;
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
		User other = (User) obj;
		if (currentLocation == null) {
			if (other.currentLocation != null)
				return false;
		} else if (!currentLocation.equals(other.currentLocation))
			return false;
		if (id != other.id)
			return false;
		if (prefRecommendationRadius != other.prefRecommendationRadius)
			return false;
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile))
			return false;
		return true;
	}
	
	public int getPrefRecommendationRadius() {
		return prefRecommendationRadius;
	}

	public void setPrefRecommendationRadius(int prefRecommendationRadius) {
		this.prefRecommendationRadius = prefRecommendationRadius;
	}

	public String getName() {
		return name;
	}
}