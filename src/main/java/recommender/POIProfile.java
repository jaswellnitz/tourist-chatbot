package recommender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the preference to the six tourist categories sightseeing, culture,
 * food, nightlife, nature and shopping.
 * 
 * @author Jasmin Wellnitz
 *
 */

public class POIProfile implements Serializable {

	private static final long serialVersionUID = -1718108343723888942L;
	/**
	 * The tourist category count.
	 */
	public static final int CATEGORY_COUNT = 6;

	private Map<TouristCategory, Preference> categories;

	/**
	 * Creates an undefined POIProfile.
	 */
	public POIProfile() {
		this(Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED,
				Preference.NOT_RATED, Preference.NOT_RATED);
	}

	/**
	 * Creates an POIProfile based on the known category preferences.
	 * 
	 * @param sightseeing
	 * @param culture
	 * @param food
	 * @param nightlife
	 * @param nature
	 * @param shopping
	 */
	public POIProfile(Preference sightseeing, Preference culture, Preference food, Preference nightlife,
			Preference nature, Preference shopping) {
		categories = new HashMap<>();
		categories.put(TouristCategory.SIGHTSEEING, sightseeing);
		categories.put(TouristCategory.CULTURE, culture);
		categories.put(TouristCategory.FOOD, food);
		categories.put(TouristCategory.NIGHTLIFE, nightlife);
		categories.put(TouristCategory.NATURE, nature);
		categories.put(TouristCategory.SHOPPING, shopping);
	}

	/**
	 * Checks if the profile shows the characteristic of a pof interest. An item
	 * is a POI if it is characterized by at least one category.
	 * 
	 * @return
	 */
	public boolean isPOI() {
		for (Preference category : categories.values()) {
			if (category == Preference.TRUE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Shows whether the profile contains the culture category.
	 * 
	 * @return boolean
	 */
	public Preference hasCulture() {
		return categories.get(TouristCategory.CULTURE);
	}

	/**
	 * Shows whether the profile contains the nightlife category.
	 * 
	 * @return boolean
	 */
	public Preference hasNightlife() {
		return categories.get(TouristCategory.NIGHTLIFE);
	}

	/**
	 * Shows whether the profile contains the sightseeing category.
	 * 
	 * @return boolean
	 */
	public Preference hasSightseeing() {
		return categories.get(TouristCategory.SIGHTSEEING);
	}

	/**
	 * Shows whether the profile contains the shopping category.
	 * 
	 * @return boolean
	 */
	public Preference hasShopping() {
		return categories.get(TouristCategory.SHOPPING);
	}

	/**
	 * Shows whether the profile contains the food category.
	 * 
	 * @return boolean
	 */
	public Preference hasFood() {
		return categories.get(TouristCategory.FOOD);
	}

	/**
	 * Shows whether the profile contains the nature category.
	 * 
	 * @return boolean
	 */
	public Preference hasNature() {
		return categories.get(TouristCategory.NATURE);
	}

	/**
	 * Returns a view of all profile categories.
	 * 
	 * @return list of preferences
	 */
	public List<Preference> getPreferenceValues() {
		return new ArrayList<Preference>(categories.values());
	}

	/**
	 * Creates a POIProfile with the given category.
	 * 
	 * @param categoryIndex
	 *            index that indicates the tourist category
	 * @return POIProfile
	 */
	public static POIProfile getProfileForCategory(TouristCategory category) {
		POIProfile poiProfile = new POIProfile();
		poiProfile.addToProfile(category.name());
		return poiProfile;
	}
	
	public Preference getPreferenceForCategory(TouristCategory category){
		return categories.get(category);
	}

	/**
	 * Converts the POIProfile into a list of interests
	 * 
	 * @return list of interests
	 */
	public List<String> getInterestsFromProfile() {
		List<String> interests = new ArrayList<>();
		for(Map.Entry<TouristCategory, Preference> cat: categories.entrySet()){
			if (cat.getValue().toBoolean()){
				interests.add(cat.getKey().name().toLowerCase());
			}
			
		}
		return interests;
	}

	/**
	 * Adds interests to the existing POIProfile
	 * @param interests
	 * @return indicates whether the action was successful
	 */
	public boolean addToProfile(String...interests) {
		return changeProfilePreference(Preference.TRUE, interests);
	}
	

	/**
	 * Deletes interests from the existing POIProfile
	 * @param dislikes
	 * @return indicates whether the action was successfull
	 */
	public boolean  deleteFromProfile(String... dislikes) {
		return changeProfilePreference(Preference.FALSE, dislikes);
	}
	
	private boolean changeProfilePreference(Preference newPreference, String... catStrings){
		Map<TouristCategory, Preference> originalPreferences = new HashMap<>(categories);
		try {
			for (String cat : catStrings) {
				TouristCategory touristCategory = TouristCategory.valueOf(cat.toUpperCase());
				Preference replace = categories.replace(touristCategory, newPreference);
				if (replace == null) {
					return false;
				}
			}
		} catch (Exception e) {
			categories = originalPreferences;
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		bld.append(categories.get(TouristCategory.SIGHTSEEING).getFieldName());
		bld.append(",");
		bld.append(categories.get(TouristCategory.CULTURE).getFieldName());
		bld.append(",");
		bld.append(categories.get(TouristCategory.FOOD).getFieldName());
		bld.append(",");
		bld.append(categories.get(TouristCategory.NIGHTLIFE).getFieldName());
		bld.append(",");
		bld.append(categories.get(TouristCategory.NATURE).getFieldName());
		bld.append(",");
		bld.append(categories.get(TouristCategory.SHOPPING).getFieldName());
		return bld.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((categories == null) ? 0 : categories.hashCode());
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
		POIProfile other = (POIProfile) obj;
		if (categories == null) {
			if (other.categories != null)
				return false;
		} else if (!categories.equals(other.categories))
			return false;
		return true;
	}
}