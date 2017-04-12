package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

enum Preference{
	TRUE,FALSE,NOT_RATED
}

public class POIProfile{

	private final boolean  culture;
	private final boolean sightseeing;
	private final boolean food;
	private final boolean nightlife;
	private final boolean nature;
	private final boolean sports;
	private final List<Boolean> categories;
	public static final int CATEGORY_COUNT = 6;
	
	public POIProfile(boolean culture, boolean sightseeing, boolean food, boolean nightlife, boolean nature,
			boolean sports) {
		this.culture = culture;
		this.nightlife = nightlife;
		this.sightseeing = sightseeing;
		this.sports = sports;
		this.food = food;
		this.nature = nature;
		categories = new ArrayList<>();
		categories.addAll(Arrays.asList(culture,nightlife,sightseeing,sports,food,nature));
	}

	public boolean isPOI(){
		return culture || sightseeing || food || nightlife || nature || sports;
	}


	public boolean hasCulture() {
		return culture;
	}


	public boolean hasNightlife() {
		return nightlife;
	}


	public boolean hasSightseeing() {
		return sightseeing;
	}


	public boolean hasSports() {
		return sports;
	}


	public boolean hasFood() {
		return food;
	}


	public boolean hasNature() {
		return nature;
	}
	
	public List<Boolean> getAllCategories(){
		return categories;
	}
	
	public String toString(){
		String  cultureString = culture ? "1" : "0";
		String  sightseeingString  = sightseeing ? "1" : "0";
		String  foodString  = food ? "1" : "0";
		String  nightlifeString  = nightlife ? "1" :"0";
		String  natureString  = nature ? "1" : "0";
		String  sportsString  = sports ? "1" : "0";
		return cultureString+","+sightseeingString+","+foodString+","+nightlifeString+","+natureString+","+sportsString;
	}
}