package dataAccess;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import domain.Rating;
import domain.User;
import recommender.POIProfile;
import recommender.Preference;

public class RatingDBTest {
	private RatingDB ratingDB;
	private User user;
	private int pointId;

	@Before
	public void setUp() {
		String url = System.getenv("DATABASE_URL");
		this.ratingDB = new RatingDB(url);
		POIProfile profile = new POIProfile(Preference.TRUE, Preference.FALSE, Preference.NOT_RATED,
				Preference.NOT_RATED, Preference.FALSE, Preference.NOT_RATED);
		
		long userId = 666;
		user = new User(userId, "Testuser");
		user.setProfile(profile);
		pointId = 999;
		ratingDB.saveRating(user.getId(), pointId, Rating._5);
	}

	@After
	public void tearDown() {
		if(ratingDB.hasRatingForUser(user.getId())){
			ratingDB.deleteAllUserRatings(user.getId());
		}
	}
	
	@Test
	public void testHasRating(){
		// Prepare
		long existingId = user.getId();
		long nonExistingId = 1;
		
		// Action
		boolean hasRatingForUser1 = ratingDB.hasRatingForUser(existingId);
		boolean hasRatingForUser2 = ratingDB.hasRatingForUser(nonExistingId);
		
		// Check
		assertTrue(hasRatingForUser1);
		assertFalse(hasRatingForUser2);
	}

	@Test
	public void testSaveRating() throws IOException {
		// Prepare
		long userId = user.getId();
		long itemId = 5555;
		Rating rating = Rating._2;
		assertFalse(ratingDB.hasRatingForUser(userId,itemId));

		// Action
		ratingDB.saveRating(userId, itemId, rating);
		
		// Check
		assertTrue(ratingDB.hasRatingForUser(userId));
		assertTrue(ratingDB.hasRatingForUser(userId, itemId));
	}
	
	@Test
	public void testDeleteAllUserEntries(){
		// Prepare
		long userId = user.getId();
		assertTrue(ratingDB.hasRatingForUser(userId));
		
		// Action
		ratingDB.deleteAllUserRatings(userId);
		
		// Check
		assertFalse(ratingDB.hasRatingForUser(userId));
	}
	
	
	@Test
	public void testUpdateRating(){
		// Prepare
		Rating newRating = Rating._3;
		
		// Action
		ratingDB.updateRating(user.getId(), pointId, newRating);
		
		// Check
		Rating rating = ratingDB.getRating(user.getId(), pointId);
		assertEquals(newRating, rating);
	}

}
