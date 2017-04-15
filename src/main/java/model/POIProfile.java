package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class POIProfile {

	private final Preference culture;
	private final Preference sightseeing;
	private final Preference food;
	private final Preference nightlife;
	private final Preference nature;
	private final Preference sports;
	private final List<Preference> categories;
	public static final int CATEGORY_COUNT = 6;

	public POIProfile(Preference culture, Preference sightseeing, Preference food, Preference nightlife,
			Preference nature, Preference sports) {
		this.culture = culture;
		this.sightseeing = sightseeing;
		this.food = food;
		this.nightlife = nightlife;
		this.nature = nature;
		this.sports = sports;
		categories = new ArrayList<>();
		categories.addAll(Arrays.asList(culture, sightseeing, food, nightlife,nature, sports));
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

	public Preference hasSports() {
		return sports;
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

	public String toString() {
		String result = "";
		for (Preference category : categories) {
			result += category.getFieldName() + ",";
		}
		result = result.substring(0, result.length() - 1);
		return result;
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
		result = prime * result + ((sports == null) ? 0 : sports.hashCode());
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
		if (sports != other.sports)
			return false;
		return true;
	}
}