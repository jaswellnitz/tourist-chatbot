package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.RecommendedPointOfInterest;
import domain.User;
import recommender.POIProfile;
import recommender.Preference;

public class UserDB extends DatabaseManager {

	private PointDB pointConverter;

	public UserDB(String dbUrl, PointDB pointConverter) {
		super(dbUrl);
		this.pointConverter = pointConverter;
	}

	public boolean hasUser(long userId) {
		String query = "select * from users where id = " + userId;

		ResultSet resultSet = executeQuery(query);
		boolean hasNext = false;
		try {
			hasNext = resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();

		return hasNext;
	}

	public boolean addRecommendation(long userId, long recommendationId) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("recommendations", "recommendations || " + recommendationId + "::bigint");
		param.put("unrated", "unrated || " + recommendationId + "::bigint");
		return updateUser(userId, param);
	}

	public boolean deleteFirstUnratedRecommendation(long userId) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("unrated", "unrated[2:array_length(unrated,1)]");
		return updateUser(userId, param);
	}

	public boolean changeRadiusForUser(long userId, int radius) {
		String radiusParam = "radius";
		Map<String, String> param = new HashMap<String, String>();
		param.put(radiusParam, String.valueOf(radius));
		return updateUser(userId, param);
	}

	public boolean changeProfileForUser(long userId, POIProfile profile) {
		String profileParam = "profile";
		Map<String, String> param = new HashMap<String, String>();
		param.put(profileParam, "'" + profile.toString() + "'");
		return updateUser(userId, param);
	}

	private boolean updateUser(long userId, Map<String, String> parameters) {
		String query = "update users set";

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			query += " " + entry.getKey() + "=" + entry.getValue() + ",";
		}
		query = query.substring(0, query.length() - 1) + " where id = " + userId + ";";

		int rowCount = executeUpdate(query);
		return rowCount == 1;
	}

	public boolean deleteUser(long userId) {
		String query = "DELETE from users where id = " + userId + ";";
		int rowCount = executeUpdate(query);
		return rowCount == 1;
	}

	public boolean storeUser(User user) {
		String posRec = preparePSQLArray(user.getPositiveRecommendations());
		String unrated = preparePSQLArray(user.getUnratedPOIs());
		String query = "INSERT into users (id, name, radius, profile, recommendations,unrated) values (" + user.getId()
				+ ",'" + user.getName() + "'," + user.getPrefRecommendationRadius() + ",'"
				+ user.getProfile().toString() + "'" + "," + posRec + "," + unrated + ")";
		int rowCount = executeUpdate(query);
		return rowCount == 1;
	}

	private String preparePSQLArray(List<RecommendedPointOfInterest> pois) {
		String ret = "";
		if (pois.isEmpty()) {
			ret = "ARRAY[]::bigint[]";
		} else {
			ret = "ARRAY[";
			for (RecommendedPointOfInterest rec : pois) {
				ret += rec.getId() + ",";
			}
			ret = ret.substring(0, ret.length() - 1) + "]";
		}
		return ret;
	}

	public User getUser(long userId) {
		String query = "select * from users where id = " + userId;
		List<User> users = new ArrayList<>();

		try(ResultSet resultSet = executeQuery(query)){
		assert resultSet != null : "Postcondition failed: no user found for id " + userId;
			while (resultSet.next()) {
				long id = resultSet.getLong("id");
				String name = resultSet.getString("name");
				int radius = resultSet.getInt("radius");
				String profile = resultSet.getString("profile");
				POIProfile poiProfile = convertToProfile(profile.split(","));
				resultSet.getArray("recommendations").getArray().getClass();
				Long[] positiveRec = (Long[])resultSet.getArray("recommendations").getArray();
				Long[] unrated = (Long[])resultSet.getArray("unrated").getArray();
				User user = new User(id, name, radius, poiProfile);
				for(long poiId: positiveRec){
					user.addPositiveRecommendations(pointConverter.getPOIForId(poiId));
				}
				for(long poiId: unrated){
					user.addUnratedPOI(pointConverter.getPOIForId(poiId));
				}
				users.add(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
		assert users.size() == 1 : "Postcondition failed: getUser from UserDB for id " + userId;

		return users.get(0);
	}

	private POIProfile convertToProfile(String[] profileFields) {
		Preference[] values = new Preference[profileFields.length];
		for (int i = 0; i < profileFields.length; i++) {
			values[i] = Preference.valueByFieldName(profileFields[i]);
		}
		return new POIProfile(values[0], values[1], values[2], values[3], values[4], values[5]);
	}
}
