package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;

import model.Rating;

public class RatingDB extends DatabaseManager {

	public RatingDB(String dbUrl) {
		super(dbUrl);
	}

	public boolean hasRatingForUser(long userId) {
		String query = "select * from ratings where userId = " + userId + ";";

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

	public boolean hasRatingForUser(long userId, long pointId) {
		String query = "select * from ratings where userId = " + userId + " and pointId =" + pointId + ";";
		boolean hasNext = false;

		try (ResultSet resultSet = executeQuery(query)) {
			hasNext = resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();

		return hasNext;
	}

	public boolean deleteAllUserRatings(long userId) {
		String query = "delete from ratings where userId = " + userId + ";";
		int rowCount = executeUpdate(query);
		return rowCount == 1;
	}

	public boolean updateRating(long userId, long pointId, Rating rating) {
		String query = "update ratings set ratings = " + rating.getValue() + " where userId = " + userId
				+ " and pointId = " + pointId + ";";

		int rowCount = executeUpdate(query);
		return rowCount == 1;
	}

	public boolean deleteRating(long userId, long pointId) {
		String query = "DELETE from ratings where userId = " + userId + " and pointId = " + pointId + ";";
		int rowCount = executeUpdate(query);
		return rowCount == 1;
	}

	public boolean saveRating(long userId, long pointId, Rating rating) {
		String query = "INSERT into ratings values (" + userId + "," + pointId + "," + rating.getValue() + ");";
		int rowCount = executeUpdate(query);
		return rowCount == 1;
	}

	public Rating getRating(long userId, long pointId) {
		String query = "Select ratings from ratings where userId=" + userId + " and pointId=" + pointId + ";";
		Rating rating = Rating.INVALID;
		try (ResultSet resultSet = executeQuery(query)) {
			boolean next = resultSet.next();
			if (next) {
				int rat = resultSet.getInt("ratings");
				rating = Rating.valueOf(rat);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		return rating;
	}
}
