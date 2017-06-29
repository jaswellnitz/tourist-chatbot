package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;

import domain.Rating;

/**
 * Stores and retrieves ratings from the database
 * 
 * @author Jasmin Wellnitz
 *
 */
public class RatingDB extends DatabaseManager {

	/**
	 * 
	 * @param dbUrl
	 *            The database access URL
	 */
	public RatingDB(String dbUrl) {
		super(dbUrl);
	}

	/**
	 * Checks whether the user has rated any point of interest
	 * 
	 * @param userId
	 * @return Boolean that indicates whether there is a rating for the stated
	 *         user in database or not
	 */
	public boolean hasRatingForUser(long userId) {
		String query = "select * from ratings where userId = " + userId + ";";

		ResultSet resultSet = executeQuery(query);
		boolean hasNext = false;
		try {
			hasNext = resultSet.next();
		} catch (SQLException e) {
			logger.error(e);
		}
		close();

		return hasNext;
	}

	/**
	 * Checks whether a user has already rated a specific point of interest
	 * 
	 * @param userId
	 * @param pointId
	 * @return Boolean that indicates whether there is that specific rating or
	 *         not
	 */
	public boolean hasRating(long userId, long pointId) {
		String query = "select * from ratings where userId = " + userId + " and pointId =" + pointId + ";";
		boolean hasNext = false;

		try (ResultSet resultSet = executeQuery(query)) {
			hasNext = resultSet.next();
		} catch (SQLException e) {
			logger.error(e);
		}
		close();

		return hasNext;
	}

	/**
	 * Deletes all ratings for a user
	 * 
	 * @param userId
	 * @return boolean which indicates the action's success
	 */
	public boolean deleteAllUserRatings(long userId) {
		String query = "delete from ratings where userId = " + userId + ";";
		int rowCount = executeUpdate(query);
		return rowCount == 1;
	}

	/**
	 * Updates an already existing rating with a new rating value
	 * 
	 * @param userId  id of the user that rated the point of interest
	 * @param pointId  the point of interest that was rated
	 * @param rating
	 *            new rating value
	 * @return boolean which indicates the action's success
	 */
	public boolean updateRating(long userId, long pointId, Rating rating) {
		assert rating != null: "Precondition failed: rating != null";
		
		String query = "update ratings set ratings = " + rating.getValue() + " where userId = " + userId
				+ " and pointId = " + pointId + ";";

		int rowCount = executeUpdate(query);
		return rowCount == 1;
	}

	/**
	 * Stores a new rating in the database
	 * 
	 * @param userId id of the user that rated the point of interest
	 * @param pointId  the point of interest that was rated
	 * @param rating
	 * @return boolean which indicates the action's success
	 */

	public boolean saveRating(long userId, long pointId, Rating rating) {
		assert rating != null: "Precondition failed: rating != null";
		
		String query = "INSERT into ratings values (" + userId + "," + pointId + "," + rating.getValue() + ");";
		int rowCount = executeUpdate(query);
		return rowCount == 1;
	}

	/**
	 * Gets a rating from the database.
	 * @param userId id of the user that rated the point of interest
	 * @param pointId the point of interest that was rated
	 * @return The rating
	 */
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
			logger.error(e);
		}
		close();
		return rating;
	}
}
