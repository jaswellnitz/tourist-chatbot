package data_access;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.POIProfile;
import model.PointOfInterest;
import model.Preference;
import util.PropertyLoader;

public class PointConverter {

	private DatabaseAccess databaseAccess;

	// TODO: dependency injection
	@Deprecated
	public PointConverter() {
		this(new DatabaseAccess(PropertyLoader.getProperty("db_name"), PropertyLoader.getProperty("db_user"),
				PropertyLoader.getProperty("db_pw")));
	}

	public PointConverter(DatabaseAccess db) {
		databaseAccess = db;
	}

	// TODO better return Set instead of List because order is not important
	public List<PointOfInterest> getPOIInRadius(String lat, String lon, int radius) {
		// Flip coordinates for PostGis, see:
		// http://postgis.net/2013/08/18/tip_lon_lat/
		String x = lon;
		String y = lat;

		String query = "SELECT ways.id, ways.tags-> 'name' as _name, ways.tags-> 'tourism' as tourism, ways.tags-> 'amenity' as amenity, ways.tags-> 'leisure' as leisure,ways.tags-> 'cuisine' as cuisine, "
				+ "ways.tags-> 'addr:street' as street, ways.tags->'addr:housenumber' as housenumber, ST_Distance(geography(geom), ST_SetSRID(geography(ST_Point("
				+ x + ", " + y + ")), 4326)) as distance, ways.tags-> 'opening_hours' as openingHours " 
				+ "FROM ways inner join nodes on ways.nodes[1]=nodes.id "
				+ "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
				+ ")), 4326), " + radius + ") and "
				+ "ways.tags ? 'name' and (ways.tags ? 'tourism' or ways.tags ? 'amenity' or ways.tags ? 'leisure' or ways.tags ? 'cuisine') "
				+ "UNION ALL "
				+ "SELECT nodes.id, nodes.tags-> 'name' as _name, nodes.tags-> 'tourism' as tourism, nodes.tags-> 'amenity' as amenity, nodes.tags-> 'leisure' as leisure,nodes.tags-> 'cuisine' as cuisine, "
				+ "nodes.tags-> 'addr:street' as street, nodes.tags->'addr:housenumber' as housenumber, ST_Distance(geography(geom), ST_SetSRID(geography(ST_Point("
				+ x + ", " + y + ")), 4326)) as distance, nodes.tags-> 'opening_hours' as openingHours " 
				+ "FROM nodes "
				+ "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
				+ ")), 4326), " + radius + ") and "
				+ "nodes.tags ? 'name' and (nodes.tags ? 'tourism' or nodes.tags ? 'amenity' or nodes.tags ? 'leisure' or nodes.tags ? 'cuisine');";

		return getPOIForQuery(query);
	}

	public List<PointOfInterest> getPOIForId(String lat, String lon, long itemId, int radius) {
		// Flip coordinates for PostGis, see:
		// http://postgis.net/2013/08/18/tip_lon_lat/
		String x = lon;
		String y = lat;

		String query = "SELECT ways.id as id, ways.tags-> 'name' as _name, ways.tags-> 'tourism' as tourism, ways.tags-> 'amenity' as amenity, ways.tags-> 'leisure' as leisure,ways.tags-> 'cuisine' as cuisine, "
				+ "ways.tags-> 'addr:street' as street, ways.tags->'addr:housenumber' as housenumber, ST_Distance(geography(geom), ST_SetSRID(geography(ST_Point("
				+ x + ", " + y + ")), 4326)) as distance, ways.tags-> 'opening_hours' as openingHours " 
				+ "FROM ways inner join nodes on ways.nodes[1]=nodes.id "
				+ "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
				+ ")), 4326), " + radius + ") and " + "ways.id=" + itemId + " UNION ALL "
				+ " SELECT nodes.id as id, nodes.tags-> 'name' as _name, nodes.tags-> 'tourism' as tourism, nodes.tags-> 'amenity' as amenity, nodes.tags-> 'leisure' as leisure,nodes.tags-> 'cuisine' as cuisine, "
				+ "nodes.tags-> 'addr:street' as street, nodes.tags->'addr:housenumber' as housenumber, ST_Distance(geography(geom), ST_SetSRID(geography(ST_Point("
				+ x + ", " + y + ")), 4326)) as distance, nodes.tags-> 'opening_hours' as openingHours " 
				+ "FROM nodes "
				+ "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
				+ ")), 4326), " + radius + ") and nodes.id=" + itemId + ";";

		List<PointOfInterest> pois = getPOIForQuery(query);
		assert pois.size() <= 1 : "Postcondition failed: Multiple Items with same id found.";
		return pois;
	}

	private List<PointOfInterest> getPOIForQuery(String query) {
		ResultSet resultSet = databaseAccess.sendQuery(query);
		List<PointOfInterest> pois = new ArrayList<>();
		
		if (resultSet == null) {
			return pois;
		}

		try {
			while (resultSet.next()) {

				POIProfile profile = mapTagsToCategories(resultSet);
				if (profile.isPOI()) {
					long id = resultSet.getLong("id");
					String name = resultSet.getString("_name");
					name = name == null? "": name;
					String street = resultSet.getString("street");
					street = street == null? "": street;
					String houseNumber = resultSet.getString("housenumber");
					houseNumber = houseNumber == null? "":houseNumber;
					int distance = (int) Math.round(resultSet.getDouble("distance"));
					String openingHours = resultSet.getString("openingHours");
					openingHours = openingHours == null? "":openingHours;
					pois.add(new PointOfInterest(id, name, street, houseNumber, distance, openingHours, profile));
				}
			}
			databaseAccess.close();
			// TODO close connection?
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pois;
	}

	private POIProfile mapTagsToCategories(ResultSet set) {
		Preference culture = Preference.FALSE;
		Preference sightseeing = Preference.FALSE;
		Preference food = Preference.FALSE;
		Preference nightlife = Preference.FALSE;
		Preference sports = Preference.FALSE;
		Preference nature = Preference.FALSE;

		try {

			String tourismTag;

			tourismTag = set.getString("tourism");
			if (tourismTag != null) {
				switch (tourismTag) {
				case "museum":
					culture = Preference.TRUE;
					break;
				case "artwork":
					culture = Preference.TRUE;
					break;
				case "attraction":
					sightseeing = Preference.TRUE;
					break;
				case "viewpoint":
					sightseeing = Preference.TRUE;
					break;
				case "picnic_site":
					nature = Preference.TRUE;
					break;
				}
			}

			String amenityTag = set.getString("amenity");
			if (amenityTag != null) {
				switch (amenityTag) {
				case "place_of_worship":
					culture = Preference.TRUE;
					break;
				case "theatre":
					culture = Preference.TRUE;
					break;
				case "restaurant":
					food = Preference.TRUE;
					break;
				case "cafe":
					food = Preference.TRUE;
					break;
				case "pub":
					nightlife = Preference.TRUE;
					break;
				case "nightclub":
					nightlife = Preference.TRUE;
					break;
				case "casino":
					nightlife = Preference.TRUE;
					break;
				}
			}

			String leisureTag = set.getString("leisure");
			if (leisureTag != null) {
				switch (leisureTag) {
				case "park":
					nature = Preference.TRUE;
					break;
				case "nature_reserve":
					nature = Preference.TRUE;
					break;
				case "sports_centre":
					sports = Preference.TRUE;
					break;
				case "stadium":
					sports = Preference.TRUE;
					break;
				}
			}
			String cuisineTag = set.getString("cuisine");
			if (cuisineTag != null) {
				food = Preference.TRUE;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new POIProfile(culture, sightseeing, food, nightlife, nature, sports);
	}
}
