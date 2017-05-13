package data_access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multiset.Entry;

import model.User;

public class UserDB {

	private DatabaseAccess dbAccess;

	public UserDB(DatabaseAccess dbAccess) {
		this.dbAccess = dbAccess;
	}

	public boolean hasUser(long userId) {
		String query = "select * from users where id = " + userId;

		ResultSet resultSet = dbAccess.executeQuery(query);
		boolean hasNext = false;
		try {
			hasNext = resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return hasNext;
	}

	public boolean changeRadiusForUser(long userId, int radius){
		String radiusParam = "radius";
		Map<String,String> param = new HashMap<String,String>();
		param.put(radiusParam, String.valueOf(radius));
		return updateUser(userId, param);
	}
	
	private boolean updateUser(long userId, Map<String, String> parameters) {
		String query = "update users set";
		
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			query+= " " + entry.getKey() +"= '" + entry.getValue() +"',";
		}
		query = query.substring(0,query.length()-1) +  " where id = " + userId + ";";
		
		int rowCount = dbAccess.executeUpdate(query);
		return rowCount == 1;
	}

	public boolean deleteUser(long userId) {
		String query = "DELETE from users where id = " + userId + ";";
		int rowCount = dbAccess.executeUpdate(query);
		return rowCount == 1;
	}

	public boolean storeUser(User user) {
		String query = "INSERT into users (id, name, radius) values (" + user.getId() + ",'" + user.getName() + "',"
				+ user.getPrefRecommendationRadius() + ")";
		int rowCount = dbAccess.executeUpdate(query);
		return rowCount == 1;
	}

	public User getUser(long userId) {
		String query = "select * from users where id = " + userId;

		ResultSet resultSet = dbAccess.executeQuery(query);
		assert resultSet != null : "Postcondition failed: no user found for id " + userId;
		List<User> users = new ArrayList<>();
		try {
			while (resultSet.next()) {
				long id = resultSet.getLong("id");
				String name = resultSet.getString("name");
				int radius = resultSet.getInt("radius");
				users.add(new User(id, name, radius));
			}
			dbAccess.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assert users.size() == 1 : "Postcondition failed: getUser from UserDB for id " + userId;

		return users.get(0);
	}
}
