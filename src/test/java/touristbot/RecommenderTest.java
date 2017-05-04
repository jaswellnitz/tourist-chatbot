package touristbot;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import data_access.PointConverter;
import model.Location;
import model.POIProfile;
import model.PointOfInterest;
import model.Preference;
import model.User;
import recommender.Recommender;

// TODO write content based tests

public class RecommenderTest {
	private Recommender recommender;
	private static final String testRatingPath = "src/test/resources/ratings.csv";
	private Location defaultUserLocation;

	@Before
	public void setUp() throws Exception {
		recommender = new Recommender(new PointConverter(), testRatingPath);
		this.defaultUserLocation = new Location(41.4034984,2.1740598);
	}

	@Test
	public void testRecommendCollaborative() {
		// Prepare
		long userId = 999; // test user who liked Casa Battló
		User user = new User(userId, defaultUserLocation); 
		
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
		long userId = 998; // test user who has one restaurant rating
		User user = new User(userId, defaultUserLocation); 
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendCollaborative(user);
		
		// Check
		assertFalse(recommendations.isEmpty());
		PointOfInterest pointOfInterest = recommendations.get(0);
		assertEquals(Preference.TRUE,pointOfInterest.getProfile().hasFood());
	}
	
	// TODO check recommendation value
	// dropped rating from original data 1011,66713401,4
	@Test
	public void testRecommendCollaborativePOIDropExistingRating(){
		// Prepare
		long userId = 1011;
		User user = new User(userId, defaultUserLocation);
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
	public void testRecommendContentBasedCompleteProfile(){
		// Prepare
		long userId = 1;
		POIProfile sightseeingProfile = new POIProfile(Preference.TRUE, Preference.TRUE, Preference.FALSE, Preference.FALSE, Preference.FALSE, Preference.FALSE);
		User user = new User(userId,sightseeingProfile, defaultUserLocation);
		int numRecommendations = 2;
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendContentBased(user, numRecommendations);
		
		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(numRecommendations, recommendations.size());
		PointOfInterest poi1 = recommendations.get(0);
		PointOfInterest poi2 = recommendations.get(1);
		System.out.println(poi1);
		System.out.println(poi2);
		assertEquals(Preference.TRUE,poi1.getProfile().hasSightseeing());
		assertEquals(Preference.TRUE,poi1.getProfile().hasCulture());
		assertEquals(Preference.TRUE,poi2.getProfile().hasSightseeing());
		assertEquals(Preference.TRUE,poi2.getProfile().hasCulture());
	}

	@Test
	public void testRecommendContentBasedSightseeing(){
		// Prepare
		long userId = 1;
		POIProfile sightseeingProfile = new POIProfile(Preference.TRUE, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED);
		User user = new User(userId,sightseeingProfile, defaultUserLocation);
		int numRecommendations = 2;
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendContentBased(user, numRecommendations);
		
		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(numRecommendations, recommendations.size());
		PointOfInterest poi1 = recommendations.get(0);
		PointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE,poi1.getProfile().hasSightseeing());
		assertEquals(Preference.TRUE,poi2.getProfile().hasSightseeing());
	}
	
	@Test
	public void testRecommendContentBasedCulture(){
		// Prepare
		long userId = 1;
		POIProfile sightseeingProfile = new POIProfile(Preference.NOT_RATED, Preference.TRUE, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED);
		assertEquals(Preference.TRUE,sightseeingProfile.hasCulture());
		User user = new User(userId,sightseeingProfile, defaultUserLocation);
		int numRecommendations = 2;
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendContentBased(user, numRecommendations);
		
		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(numRecommendations, recommendations.size());
		PointOfInterest poi1 = recommendations.get(0);
		PointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE,poi1.getProfile().hasCulture());
		assertEquals(Preference.TRUE,poi2.getProfile().hasCulture());
	}
	
	@Test
	public void testRecommendContentBasedFood(){
		// Prepare
		long userId = 1;
		POIProfile sightseeingProfile = new POIProfile(Preference.NOT_RATED, Preference.NOT_RATED, Preference.TRUE, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED);
		assertEquals(Preference.TRUE,sightseeingProfile.hasFood());
		User user = new User(userId,sightseeingProfile, defaultUserLocation);
		int numRecommendations = 2;
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendContentBased(user, numRecommendations);
		
		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(numRecommendations, recommendations.size());
		PointOfInterest poi1 = recommendations.get(0);
		PointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE,poi1.getProfile().hasFood());
		assertEquals(Preference.TRUE,poi2.getProfile().hasFood());
	}
	
	@Test
	public void testRecommendContentBasedNature(){
		// Prepare
		long userId = 1;
		POIProfile sightseeingProfile = new POIProfile(Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.TRUE, Preference.NOT_RATED);
		assertEquals(Preference.TRUE,sightseeingProfile.hasNature());
		User user = new User(userId,sightseeingProfile, defaultUserLocation);
		int numRecommendations = 2;
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendContentBased(user, numRecommendations);
		
		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(numRecommendations, recommendations.size());
		PointOfInterest poi1 = recommendations.get(0);
		PointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE,poi1.getProfile().hasNature());
		assertEquals(Preference.TRUE,poi2.getProfile().hasNature());
	}
	
	@Test
	public void testRecommendContentBasedNightlife(){
		// Prepare
		long userId = 1;
		POIProfile sightseeingProfile = new POIProfile(Preference.FALSE, Preference.FALSE, Preference.FALSE, Preference.TRUE, Preference.FALSE, Preference.FALSE);
		assertEquals(Preference.TRUE,sightseeingProfile.hasNightlife());
		User user = new User(userId,sightseeingProfile, defaultUserLocation);
		int numRecommendations = 2;
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendContentBased(user, numRecommendations);
		
		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(numRecommendations, recommendations.size());
		PointOfInterest poi1 = recommendations.get(0);
		PointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE,poi1.getProfile().hasNightlife());
		assertEquals(Preference.TRUE,poi2.getProfile().hasNightlife());
	}
	
	@Test
	public void testRecommendContentBasedNightlifeNotRated(){
		// Prepare
		long userId = 1;
		POIProfile sightseeingProfile = new POIProfile(Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.TRUE, Preference.NOT_RATED, Preference.NOT_RATED);
		assertEquals(Preference.TRUE,sightseeingProfile.hasNightlife());
		User user = new User(userId,sightseeingProfile, defaultUserLocation);
		int numRecommendations = 2;
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendContentBased(user, numRecommendations);
		
		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(numRecommendations, recommendations.size());
		PointOfInterest poi1 = recommendations.get(0);
		PointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE,poi1.getProfile().hasNightlife());
		assertEquals(Preference.TRUE,poi2.getProfile().hasNightlife());
	}
	
	
	@Test
	public void testRecommendContentBasedShopping(){
		// Prepare
		long userId = 1000;
		POIProfile nightlifeProfile = new POIProfile(Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED, Preference.TRUE);
		assertEquals(Preference.TRUE,nightlifeProfile.hasShopping());
		User user = new User(userId,nightlifeProfile, defaultUserLocation);
		int numRecommendations = 2;
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendContentBased(user, numRecommendations);
		
		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(numRecommendations, recommendations.size());
		PointOfInterest poi1 = recommendations.get(0);
		PointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE,poi1.getProfile().hasShopping());
		assertEquals(Preference.TRUE,poi2.getProfile().hasShopping());
	}
}
