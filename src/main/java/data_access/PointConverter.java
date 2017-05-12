package data_access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.POIProfile;
import model.RecommendedPointOfInterest;
import model.Preference;

public class PointConverter {

	private DatabaseAccess databaseAccess;

	// dependency injection
	@Deprecated
	public PointConverter() {
		this(new DatabaseAccess(System.getenv("JDBC_DATABASE_URL")));
	}

	public PointConverter(DatabaseAccess db) {
		databaseAccess = db;
	}

	// TODO better return Set instead of List because order is not important
	public List<RecommendedPointOfInterest> getPOIInRadius(double latitude, double longitude, int radius) {
		// Flip coordinates for PostGis, see:
		// http://postgis.net/2013/08/18/tip_lon_lat/
		String x = String.valueOf(longitude);
		String y = String.valueOf(latitude);

		String query = getSelectQuery("ways", x, y) + " FROM ways inner join nodes on ways.nodes[1]=nodes.id "
				+ getConditionQueryForGeneralPOISearch("ways", x, y, radius) + "UNION ALL "
				+ getSelectQuery("nodes", x, y) + " FROM nodes "
				+ getConditionQueryForGeneralPOISearch("nodes", x, y, radius) + ";";

		return getPOIForQuery(query);
	}

	private String getSelectQuery(String table, String x, String y) {
		String query = "SELECT " + table + ".id, " + table + ".tags-> 'name' as _name, " + table
				+ ".tags-> 'tourism' as tourism, " + table + ".tags-> 'amenity' as amenity, " + table
				+ ".tags-> 'leisure' as leisure, " + table + ".tags-> 'cuisine' as cuisine, " + table
				+ ".tags-> 'historic' as historic, " + table + ".tags-> 'shop' as shop," + table
				+ ".tags-> 'beach' as beach, " + table
				+ ".tags-> 'addr:street' as street, " + table
				+ ".tags->'addr:housenumber' as housenumber, ST_Distance(geography(geom), ST_SetSRID(geography(ST_Point("
				+ x + ", " + y + ")), 4326)) as distance, " + table + ".tags-> 'opening_hours' as openingHours";
		return query;
	}

	private String getConditionQueryForGeneralPOISearch(String table, String x, String y, int radius) {
		String query = "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
				+ ")), 4326), " + radius + ") and " + table + ".tags ? 'name' and " + table 
				+ ".tags ?| ARRAY['tourism','amenity','leisure','cuisine','beach','historic','shop']";
		return query;
	}

	public List<RecommendedPointOfInterest> getPOIForId(long itemId, double latitude, double longitude, int radius) {
		// Flip coordinates for PostGis, see:
		// http://postgis.net/2013/08/18/tip_lon_lat/
		String x = String.valueOf(longitude);
		String y = String.valueOf(latitude);

		String query = getSelectQuery("ways", x, y) + " FROM ways inner join nodes on ways.nodes[1]=nodes.id "
				+ "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
				+ ")), 4326), " + radius + ") and " + "ways.id=" + itemId + " UNION ALL "
				+ getSelectQuery("nodes", x, y) + " FROM nodes "
				+ "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
				+ ")), 4326), " + radius + ") and nodes.id=" + itemId + ";";

		List<RecommendedPointOfInterest> pois = getPOIForQuery(query);
		assert pois.size() <= 1 : "Postcondition failed: Multiple Items with same id found.";
		return pois;
	}

	private List<RecommendedPointOfInterest> getPOIForQuery(String query) {
		ResultSet resultSet = databaseAccess.executeQuery(query);
		List<RecommendedPointOfInterest> pois = new ArrayList<>();

		if (resultSet == null) {
			return pois;
		}

		try {
			while (resultSet.next()) {

				POIProfile profile = mapTagsToCategories(resultSet);
				if (profile.isPOI()) {
					long id = resultSet.getLong("id");
					String name = resultSet.getString("_name");
					name = name == null ? "" : name;
					String street = resultSet.getString("street");
					street = street == null ? "" : street;
					String houseNumber = resultSet.getString("housenumber");
					houseNumber = houseNumber == null ? "" : houseNumber;
					int distance = (int) Math.round(resultSet.getDouble("distance"));
					String openingHours = resultSet.getString("openingHours");
					openingHours = openingHours == null ? "" : openingHours;
					pois.add(new RecommendedPointOfInterest(id, name, street, houseNumber, distance, openingHours,
							profile));
				}
			}
			databaseAccess.close();
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
		Preference nature = Preference.FALSE;
		Preference shopping = Preference.FALSE;

		try {

			String tourismTag = set.getString("tourism");
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

			String beachTag = set.getString("beach");
			if (beachTag != null) {
				nature = Preference.TRUE;
			}

			String historicTag = set.getString("historic");
			if (historicTag != null) {
				sightseeing = Preference.TRUE;
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
				case "marketplace":
					shopping = Preference.TRUE;
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

			String shopTag = set.getString("shop");
			if (shopTag != null) {
				switch (shopTag) {
				case "mall":
					shopping = Preference.TRUE;
					break;
				case "marketplace":
					shopping= Preference.TRUE;
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
				}
			}
			String cuisineTag = set.getString("cuisine");
			if (cuisineTag != null) {
				food = Preference.TRUE;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new POIProfile(sightseeing, culture, food, nightlife, nature, shopping);
	}
}
