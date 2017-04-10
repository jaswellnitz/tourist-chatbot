package data_acces;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.POIProfile;

//MOCKUP: Approach 1 - get all points of interests out of database and store as csv file

@Deprecated
public class OSMToCSVConverter {

	private BufferedWriter bw;
	private DatabaseAccess databaseAccess;

	public OSMToCSVConverter(DatabaseAccess db, String fileName) {
		try {
			bw = new BufferedWriter(new FileWriter("src/main/resources/" + fileName + ".csv"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		databaseAccess = db;
	}

	// TODO: implement for table nodes too
	private String getAllPOIQuery() {
		return "SELECT ways.id, ways.tags-> 'name' as _name, ways.tags-> 'tourism' as tourism, ways.tags-> 'amenity' as amenity, ways.tags-> 'leisure' as leisure,ways.tags-> 'cuisine' as cuisine "
				+ "FROM ways inner join nodes on ways.nodes[1]=nodes.id "
				+ "WHERE ways.tags ? 'name' and (ways.tags ? 'tourism' or ways.tags ? 'amenity' or ways.tags ? 'leisure' or ways.tags ? 'cuisine')";
	}

	private String getRadius(String lat, String lon, String radius) {
		// "ST_DWithin(geography(nodes.geom),
		// ST_SetSRID(geography(ST_Point(9.991636, 53.550090)), 4326), 2000)"
		return "ST_DWithin(geography(nodes.geom), ST_SetSRID(geography(ST_Point(" + lat + ", " + lon + ")), 4326), "
				+ radius + ")";
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

	private void convertOSMToCSV() {

		String query = getAllPOIQuery();
		ResultSet resultSet = databaseAccess.sendQuery(query);
		try {
			while (resultSet.next()) {
				POIProfile profile = mapTagsToCategories(resultSet);
				if (profile.isPOI()) {
//					System.out.println(resultSet.getString("_name"));
//					System.out.println(profile);
					bw.write(profile.toString()+"\n");
				}
			}
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void close() throws IOException {
		databaseAccess.close();
		bw.close();
	}

	public static void main(String... args) {
		DatabaseAccess db = new DatabaseAccess("touristDB1", "touristuser", "test");
		OSMToCSVConverter osmToCSVConverter = new OSMToCSVConverter(db, "itemProfile");
		osmToCSVConverter.convertOSMToCSV();

	}

}
