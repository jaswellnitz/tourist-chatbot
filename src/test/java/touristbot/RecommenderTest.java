package touristbot;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import data_access.PointConverter;
import model.Location;
import model.PointOfInterest;
import model.Preference;
import model.User;
import recommender.Recommender;

// TODO write content based tests

public class RecommenderTest {
	private Recommender recommender;
	private static final String testRatingPath = "src/test/resources/ratings.csv";

	@Before
	public void setUp() throws Exception {
		recommender = new Recommender(new PointConverter(), testRatingPath);
	}

	@Test
	public void testRecommendCollaborative() {
		// Prepare
		long userId = 999; // test user who liked Casa Battló
		User user = new User(userId); 
		Location location = new Location(41.4034984,2.1740598);
		user.setCurrentLocation(location);
		
		long recommendedId =  359086841; // Sagrada familia
		// Action
		List<PointOfInterest> recommendations = recommender.recommendCollaborative(user);
		
		// Check
		assertFalse(recommendations.isEmpty());
		List<Long> ids = new ArrayList<>();
		for(PointOfInterest poi: recommendations){
			ids.add(poi.getId());
		}
		assertTrue(ids.contains(recommendedId));
	}
	
	@Test
	public void testRecommendCollaborativePOISameCategory() {
		// Prepare
		long userId = 998; // test user - one restaurant rating
		User user = new User(userId); 
		Location location = new Location(41.4034984,2.1740598);
		user.setCurrentLocation(location);
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendCollaborative(user);
		
		// Check
		assertFalse(recommendations.isEmpty());
		PointOfInterest pointOfInterest = recommendations.get(0);
		assertEquals(Preference.TRUE,pointOfInterest.getProfile().hasFood());
	}
	
	// TODO check recommendation value
	@Test
	public void testRecommendCollaborativePOIDropExistingRating(){
		// dropped rating from original data 1011,66713401,4
		long userId = 1011;
		User user = new User(userId);
		Location location = new Location(41.4034984,2.1740598);
		user.setCurrentLocation(location);
		long droppedId = 66713401;

		// Action
		List<PointOfInterest> recommendations = recommender.recommendCollaborative(user);
		
		// Check
		assertFalse(recommendations.isEmpty());
		
		List<Long> ids = new ArrayList<>();
		for(PointOfInterest poi: recommendations){
			ids.add(poi.getId());
		}
		assertTrue(ids.contains(droppedId));
	}
	
	@Test
	public void testRecommendContentBased(){
		
	}
	
	
}
