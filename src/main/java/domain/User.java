package domain;

import java.util.ArrayList;
import java.util.List;

import recommender.POIProfile;
import recommender.ProfileItem;

/**
 * A user who interacts with the chatbot. Contains personal information of the user, his interests and information about his recommendations.
 * @author Jasmin Wellnitz
 *
 */
public class User implements ProfileItem {

	private static final long serialVersionUID = -8542663924097410197L;
	private static final int DEFAULT_RECOMMENDATION_RADIUS = 3000;
	
	private final long id;
	private final String name;
	private POIProfile profile;
	private int prefRecommendationRadius;
	private Location currentLocation;
	
	// TODO refactor ?
	private int lastRecommendedIndex;
	private List<RecommendedPointOfInterest> pendingRecommendations;
	private List<RecommendedPointOfInterest> unratedPOIs;
	private List<RecommendedPointOfInterest> positiveRecommendations;

	/**
	 * Creates a new user based on the information given by the messenger.
	 * @param id
	 * @param name
	 */
	public User(long id, String name) {
		this(id, new POIProfile(), DEFAULT_RECOMMENDATION_RADIUS, Location.UNSET, name);
	}
	
	/**
	 * Creates a user based on previously saved information, the user's current location is unknown
	 * @param id
	 * @param name
	 * @param radius
	 * @param profile
	 */
	public User(long id, String name, int radius, POIProfile profile) {
		this(id, profile, radius, Location.UNSET, name);
	}

	/**
	 * Creates a user based on all available information.
	 * @param id
	 * @param profile
	 * @param radius
	 * @param location
	 * @param name
	 */
	public User(long id, POIProfile profile, int radius, Location location, String name) {
		this.id = id;
		this.profile = profile;
		this.prefRecommendationRadius = radius;
		this.currentLocation = location;
		this.name = name;
		pendingRecommendations = new ArrayList<>();
		unratedPOIs = new ArrayList<>();
		positiveRecommendations = new ArrayList<>();
		lastRecommendedIndex = -1;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public POIProfile getProfile() {
		return profile;
	}

	/**
	 * Sets the user's POIProfile and therefore defines his interests
	 * @param profile
	 */
	public void setProfile(POIProfile profile) {
		assert profile != null : "Precondition failed: profile != null";
		this.profile = profile;
	}

	/**
	 * Gets the user's current location.
	 * @return the location
	 */
	public Location getCurrentLocation() {
		assert currentLocation != null : "Precondition failed: currentLocation != null";
		assert !currentLocation.equals(Location.UNSET) : "Precondition failed: location is unset";

		return currentLocation;
	}

	/**
	 * Sets the user's current location.
	 * @param currentLocation
	 */
	public void setCurrentLocation(Location currentLocation) {
		assert currentLocation != null : "Precondition failed: currentLocation != null";

		this.currentLocation = currentLocation;
	}
	
	/**
	 * Gets the user's recommendations radius.
	 * @return recommendation radius
	 */
	public int getPrefRecommendationRadius() {
		return prefRecommendationRadius;
	}

	/**
	 * Sets the recommendation radius
	 * @param prefRecommendationRadius
	 */
	public void setPrefRecommendationRadius(int prefRecommendationRadius) {
		this.prefRecommendationRadius = prefRecommendationRadius;
	}

	/**
	 * Gets the user's name.
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "User:(" + id + ", " + profile + ", " + prefRecommendationRadius + ", " + currentLocation + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentLocation == null) ? 0 : currentLocation.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + lastRecommendedIndex;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pendingRecommendations == null) ? 0 : pendingRecommendations.hashCode());
		result = prime * result + ((positiveRecommendations == null) ? 0 : positiveRecommendations.hashCode());
		result = prime * result + prefRecommendationRadius;
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		result = prime * result + ((unratedPOIs == null) ? 0 : unratedPOIs.hashCode());
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
		if (lastRecommendedIndex != other.lastRecommendedIndex)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pendingRecommendations == null) {
			if (other.pendingRecommendations != null)
				return false;
		} else if (!pendingRecommendations.equals(other.pendingRecommendations))
			return false;
		if (positiveRecommendations == null) {
			if (other.positiveRecommendations != null)
				return false;
		} else if (!positiveRecommendations.equals(other.positiveRecommendations))
			return false;
		if (prefRecommendationRadius != other.prefRecommendationRadius)
			return false;
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile))
			return false;
		if (unratedPOIs == null) {
			if (other.unratedPOIs != null)
				return false;
		} else if (!unratedPOIs.equals(other.unratedPOIs))
			return false;
		return true;
	}

	public void setPendingRecommendations(List<RecommendedPointOfInterest> recPOIs) {
		pendingRecommendations = recPOIs;
	}

	public List<RecommendedPointOfInterest> getPendingRecommendations() {
		return pendingRecommendations;
	}

	public int getLastRecommendedIndex() {
		return lastRecommendedIndex;
	}

	public void setLastRecommendedIndex(int index) {
		lastRecommendedIndex = index;
	}

	public void addUnratedPOI(RecommendedPointOfInterest poi) {
		unratedPOIs.add(poi);
	}

	public List<RecommendedPointOfInterest> getPositiveRecommendations() {
		return positiveRecommendations;
	}

	public void addPositiveRecommendations(RecommendedPointOfInterest recPOI) {
		positiveRecommendations.add(recPOI);
	}

	public List<RecommendedPointOfInterest> getUnratedPOIs() {
		return unratedPOIs;
	}
}
