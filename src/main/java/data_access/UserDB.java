package data_access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.User;

public class UserDB {

	private DatabaseAccess dbAccess;

	public UserDB(DatabaseAccess dbAccess) {
		this.dbAccess = dbAccess;
	}

	public boolean hasUser(long userId){
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
	
	public void storeUser(User user) {
		String query = "INSERT into users (id, name, radius) values (" + user.getId() + ",'" + user.getName() + "',"
				+ user.getPrefRecommendationRadius() + ")";
		dbAccess.executeUpdate(query);
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
		assert users.size() == 1 : "Postcondition failed: multiple users with same id found.";

		return users.get(0);
	}
}
