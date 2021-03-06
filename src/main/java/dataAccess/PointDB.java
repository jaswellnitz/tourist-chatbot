package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import domain.Location;
import domain.RecommendedPointOfInterest;
import recommender.POIProfile;
import recommender.Preference;

/**
 * Retrieves geographic data from the database and converts them into Points of Interest
 * @author Jasmin Wellnitz
 *
 */
public class PointDB extends DatabaseManager {

	/**
	 * 
	 * @param databaseUrl The database access URL
	 */
	public PointDB(String databaseUrl) {
		super(databaseUrl);
	}

	/**
	 * Gets all points of interest in a specified radius from a given position (in coordinates) from the database
	 * @param latitude latitude of the position
	 * @param longitude longitude of the position
	 * @param radius 
	 * @return a list of points of interest in the given radius
	 */
	public List<RecommendedPointOfInterest> getPOIInRadius(double latitude, double longitude, int radius) {
		// Flip coordinates for PostGis, see:
		// http://postgis.net/2013/08/18/tip_lon_lat/
		String x = String.valueOf(longitude);
		String y = String.valueOf(latitude);

		String query = getSelectQuery("ways", x, y) + " FROM ways inner join nodes on ways.nodes[1]=nodes.id "
				+ getConditionQueryForGeneralPOISearch("ways", x, y, radius) + "UNION ALL "
				+ getSelectQuery("nodes", x, y) + " FROM nodes "
				+ getConditionQueryForGeneralPOISearch("nodes", x, y, radius) + ";";

		return getPOIForQuery(query, true);
	}

	/**
	 * Gets the point of interest that matches the given id
	 * @param osmId
	 * @return the point of interest
	 */
	public RecommendedPointOfInterest getPOIForId(long osmId) {
		String query = getSelectQuery("ways") + " FROM ways inner join nodes on ways.nodes[1]=nodes.id "
				+ "WHERE ways.id=" + osmId + " UNION ALL " + getSelectQuery("nodes") + " FROM nodes "
				+ "WHERE nodes.id=" + osmId + ";";

		List<RecommendedPointOfInterest> pois = getPOIForQuery(query, false);
		assert pois.size() == 1 : "Postcondition failed: Multiple Items with same id found.";
		return pois.get(0);
	}

	/**
	 * Gets the point of interest that matches the given id if it is in the specified radius from a given position.
	 * @param osmId the OpenStreetMap id
	 * @param latitude latitude of the specified initial position
	 * @param longitude longitude of the specified initial position
	 * @param radius
	 * @return point of interest
	 */
	public List<RecommendedPointOfInterest> getPOIForId(long osmId, double latitude, double longitude, int radius) {
		// Flip coordinates for PostGis, see:
		// http://postgis.net/2013/08/18/tip_lon_lat/
		String x = String.valueOf(longitude);
		String y = String.valueOf(latitude);

		String query = getSelectQuery("ways", x, y) + " FROM ways inner join nodes on ways.nodes[1]=nodes.id "
				+ "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
				+ ")), 4326), " + radius + ") and " + "ways.id=" + osmId + " UNION ALL "
				+ getSelectQuery("nodes", x, y) + " FROM nodes "
				+ "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
				+ ")), 4326), " + radius + ") and nodes.id=" + osmId + ";";

		List<RecommendedPointOfInterest> pois = getPOIForQuery(query, true);
		assert pois.size() <= 1 : "Postcondition failed: Multiple Items with same id found.";
		return pois;
	}


	/**
	 * Creates a POIProfile from the database results by mapping the OpenStreetMap tags to the defined tourist categories.
	 * @param set the JDBC ResultSet
	 * @return POIProfile
	 * @throws SQLException 
	 */
	private POIProfile mapTagsToCategories(ResultSet set) throws SQLException {
		Preference culture = Preference.FALSE;
		Preference sightseeing = Preference.FALSE;
		Preference food = Preference.FALSE;
		Preference nightlife = Preference.FALSE;
		Preference nature = Preference.FALSE;
		Preference shopping = Preference.FALSE;


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
				default:
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
				default:
					break;
				}
			}

			String shopTag = set.getString("shop");
			if (shopTag != null && (shopTag.equals("mall") || shopTag.equals("marketplace"))) {
				shopping = Preference.TRUE;
			}

			String leisureTag = set.getString("leisure");
			if (leisureTag != null && (leisureTag.equals("park") || leisureTag.equals("nature_reserve"))) {
				nature = Preference.TRUE;
			}

			String cuisineTag = set.getString("cuisine");
			if (cuisineTag != null) {
				food = Preference.TRUE;
			}
		return new POIProfile(sightseeing, culture, food, nightlife, nature, shopping);
	}
	

	private List<RecommendedPointOfInterest> getPOIForQuery(String query, boolean inRadius) {
		List<RecommendedPointOfInterest> pois = new ArrayList<>();

		try (ResultSet resultSet = executeQuery(query)) {

			if (resultSet == null) {
				return pois;
			}

			while (resultSet.next()) {

				POIProfile profile = mapTagsToCategories(resultSet);
				if (profile.isPOI()) {
					RecommendedPointOfInterest pointOfInterest = createPOIFromResultSet(resultSet, profile, inRadius);
					pois.add(pointOfInterest);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
		close();

		return pois;
	}

	private RecommendedPointOfInterest createPOIFromResultSet(ResultSet resultSet, POIProfile profile, boolean inRadius)
			throws SQLException {
		long id = resultSet.getLong("id");
		double lat = resultSet.getDouble("lat");
		double lon = resultSet.getDouble("lon");
		Location location = new Location(lat, lon);
		String name = resultSet.getString("_name");
		name = name == null ? "" : name;
		String street = resultSet.getString("street");
		street = street == null ? "" : street;
		String houseNumber = resultSet.getString("housenumber");
		houseNumber = houseNumber == null ? "" : houseNumber;
		int distance = -1;
		if (inRadius) {
			distance = (int) Math.round(resultSet.getDouble("distance"));
		}
		String openingHours = resultSet.getString("openingHours");
		openingHours = openingHours == null ? "" : openingHours;
		RecommendedPointOfInterest pointOfInterest = new RecommendedPointOfInterest(id, name, location, street,
				houseNumber, distance, openingHours, profile);
		return pointOfInterest;
	}
	
	private String getSelectQuery(String table, String x, String y) {
		String query = "SELECT ST_Y(geom) as lat, ST_X(geom) as lon, " + table + ".id, " + table
				+ ".tags-> 'name' as _name, " + table + ".tags-> 'tourism' as tourism, " + table
				+ ".tags-> 'amenity' as amenity, " + table + ".tags-> 'leisure' as leisure, " + table
				+ ".tags-> 'cuisine' as cuisine, " + table + ".tags-> 'historic' as historic, " + table
				+ ".tags-> 'shop' as shop," + table + ".tags-> 'beach' as beach, " + table
				+ ".tags-> 'addr:street' as street, " + table + ".tags->'addr:housenumber' as housenumber, " + table
				+ ".tags-> 'opening_hours' as openingHours";
		if (!x.isEmpty() && !y.isEmpty()) {
			query += ", ST_Distance(geography(geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
					+ ")), 4326)) as distance";
		}
		return query;
	}

	private String getSelectQuery(String table) {
		return getSelectQuery(table, "", "");
	}

	private String getConditionQueryForGeneralPOISearch(String table, String x, String y, int radius) {
		String query = "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + x + ", " + y
				+ ")), 4326), " + radius + ") and " + table + ".tags ? 'name' and " + table
				+ ".tags ?| ARRAY['tourism','amenity','leisure','cuisine','beach','historic','shop']";
		return query;
	}
}
