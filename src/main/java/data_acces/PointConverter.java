package data_acces;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.POIProfile;
import model.PointOfInterest;

public class PointConverter {

	private DatabaseAccess databaseAccess;
	private static final int DEFAULT_RADIUS = 1000;

	// TODO: dependency injection
	@Deprecated
	public PointConverter() {
		this(new DatabaseAccess("touristDB1", "touristuser", "test"));
	}

	public PointConverter(DatabaseAccess db) {
		databaseAccess = db;
	}

	// TODO: implement for table nodes too
	private String getPOIInRadiusQuery(String lat, String lon, int radius) {
		return "SELECT ways.id, ways.tags-> 'name' as _name, ways.tags-> 'tourism' as tourism, ways.tags-> 'amenity' as amenity, ways.tags-> 'leisure' as leisure,ways.tags-> 'cuisine' as cuisine, "
				+ "ways.tags-> 'addr:street' as street, ways.tags->'addr:housenumber' as housenumber, ST_Distance(geography(geom), ST_SetSRID(geography(ST_Point("+lat+", "+lon+")), 4326)) as distance "
				+ "FROM ways inner join nodes on ways.nodes[1]=nodes.id "
				+ "WHERE ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + lat + ", " + lon
				+ ")), 4326), " + radius + ") and "
				+ "ways.tags ? 'name' and (ways.tags ? 'tourism' or ways.tags ? 'amenity' or ways.tags ? 'leisure' or ways.tags ? 'cuisine')";
	}

	/**
	 * Gets all points of interest in the default radius (1000 m)
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public List<PointOfInterest> getPOIInRadius(String lat, String lon) {
		return getPOIInRadius(lat, lon, DEFAULT_RADIUS);
	}

	public List<PointOfInterest> getPOIInRadius(String lat, String lon, int radius) {
		String query = getPOIInRadiusQuery(lat, lon, radius);
		ResultSet resultSet = databaseAccess.sendQuery(query);
		List<PointOfInterest> pois = new ArrayList<>();

		try {
			while (resultSet.next()) {

				POIProfile profile = mapTagsToCategories(resultSet);
				if (profile.isPOI()) {
					int id = resultSet.getInt("id");
					String name = resultSet.getString("_name");
					String street = resultSet.getString("street");
					String houseNumber = resultSet.getString("housenumber");
					int distance = (int) Math.round(resultSet.getDouble("distance"));
					pois.add(new PointOfInterest(id, name, street, houseNumber, distance, profile));
				}
			}
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pois;
	}

	private POIProfile mapTagsToCategories(ResultSet set) {
		boolean culture = false;
		boolean sightseeing = false;
		boolean food = false;
		boolean nightlife = false;
		boolean sports = false;
		boolean nature = false;

		try {

			String tourismTag;

			tourismTag = set.getString("tourism");
			if (tourismTag != null) {
				switch (tourismTag) {
				case "museum":
					culture = true;
					break;
				case "artwork":
					culture = true;
					break;
				case "attraction":
					sightseeing = true;
					break;
				case "viewpoint":
					sightseeing = true;
					break;
				case "picnic_site":
					nature = true;
					break;
				}
			}

			String amenityTag = set.getString("amenity");
			if (amenityTag != null) {
				switch (amenityTag) {
				case "place_of_worship":
					culture = true;
					break;
				case "theatre":
					culture = true;
					break;
				case "restaurant":
					food = true;
					break;
				case "cafe":
					food = true;
					break;
				case "pub":
					nightlife = true;
					break;
				case "nightclub":
					nightlife = true;
					break;
				case "casino":
					nightlife = true;
					break;
				}
			}

			String leisureTag = set.getString("leisure");
			if (leisureTag != null) {
				switch (leisureTag) {
				case "park":
					nature = true;
					break;
				case "nature_reserve":
					nature = true;
					break;
				case "sports_centre":
					sports = true;
					break;
				case "stadium":
					sports = true;
					break;
				}
			}
			String cuisineTag = set.getString("cuisine");
			if (cuisineTag != null) {
				food = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new POIProfile(culture, sightseeing, food, nightlife, nature, sports);
	}

	private void close() throws IOException {
		databaseAccess.close();
	}
}
