package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class POIProfile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1718108343723888942L;
	private final Preference sightseeing;
	private final Preference culture;
	private final Preference food;
	private final Preference nightlife;
	private final Preference nature;
	private final Preference shopping;
	private final List<Preference> categories;
	public static final int CATEGORY_COUNT = 6;

	public POIProfile() {
		this(Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED,
				Preference.NOT_RATED, Preference.NOT_RATED);
	}

	public POIProfile(Preference sightseeing, Preference culture, Preference food, Preference nightlife,
			Preference nature, Preference shopping) {
		this.sightseeing = sightseeing;
		this.culture = culture;
		this.food = food;
		this.nightlife = nightlife;
		this.nature = nature;
		this.shopping = shopping;
		categories = new ArrayList<>();
		categories.addAll(Arrays.asList(sightseeing, culture, food, nightlife, nature, shopping));
	}

	public boolean isPOI() {
		for (Preference category : categories) {
			if (category == Preference.TRUE) {
				return true;
			}
		}
		return false;
	}

	public Preference hasCulture() {
		return culture;
	}

	public Preference hasNightlife() {
		return nightlife;
	}

	public Preference hasSightseeing() {
		return sightseeing;
	}

	public Preference hasShopping() {
		return shopping;
	}

	public Preference hasFood() {
		return food;
	}

	public Preference hasNature() {
		return nature;
	}

	public List<Preference> getAllCategories() {
		return categories;
	}

	public static POIProfile getProfileForCategoryIndex(int categoryIndex) {
		Preference[] preference = new Preference[CATEGORY_COUNT];
		for (int i = 0; i < preference.length; i++) {
			if (i == categoryIndex) {
				preference[i] = Preference.TRUE;
			} else {
				preference[i] = Preference.NOT_RATED;
			}
		}
		return new POIProfile(preference[0], preference[1], preference[2], preference[3], preference[4], preference[5]);
	}

	
	public static POIProfile getProfileForInterests(List<String> interests){
		Preference[] preference = {Preference.NOT_RATED,Preference.NOT_RATED,Preference.NOT_RATED,Preference.NOT_RATED,Preference.NOT_RATED,Preference.NOT_RATED};
		List<String> categories = Arrays.asList(new String[]{"sightseeing","culture","food","nightlife","nature","shopping"});
		for(String interest:interests){
			if(categories.contains(interest)){
				int index = categories.indexOf(interest);
				preference[index] = Preference.TRUE;
			}
		}
		return new POIProfile(preference[0],preference[1],preference[2],preference[3],preference[4],preference[5]);
	}
	
	public List<String> getInterestsFromProfile(){
		List<String> interests = new ArrayList<>();
		
		if (hasSightseeing().toBoolean()) {
			interests.add("sightseeing");
		}
		if (hasCulture().toBoolean()) {
			interests.add("culture");
		}
		if (hasFood().toBoolean()) {
			interests.add("food");
		}
		if (hasNature().toBoolean()) {
			interests.add("nature");
		}
		if (hasNightlife().toBoolean()) {
			interests.add("nightlife");
		}
		if (hasShopping().toBoolean()) {
			interests.add("shopping");
		}
		return interests;
	}
	
	
	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		for (Preference category : categories) {
			bld.append(category.getFieldName());
			bld.append(",");
		}
		return bld.substring(0, bld.length()-1);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((categories == null) ? 0 : categories.hashCode());
		result = prime * result + ((culture == null) ? 0 : culture.hashCode());
		result = prime * result + ((food == null) ? 0 : food.hashCode());
		result = prime * result + ((nature == null) ? 0 : nature.hashCode());
		result = prime * result + ((nightlife == null) ? 0 : nightlife.hashCode());
		result = prime * result + ((sightseeing == null) ? 0 : sightseeing.hashCode());
		result = prime * result + ((shopping == null) ? 0 : shopping.hashCode());
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
		if (culture != other.culture)
			return false;
		if (food != other.food)
			return false;
		if (nature != other.nature)
			return false;
		if (nightlife != other.nightlife)
			return false;
		if (sightseeing != other.sightseeing)
			return false;
		if (shopping != other.shopping)
			return false;
		return true;
	}
}