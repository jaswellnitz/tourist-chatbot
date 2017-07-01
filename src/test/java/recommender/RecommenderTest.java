package recommender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dataAccess.PointDB;
import dataAccess.RatingDB;
import domain.Location;
import domain.Rating;
import domain.RecommendedPointOfInterest;
import domain.User;
import recommender.POIProfile;
import recommender.Preference;
import recommender.Recommender;

public class RecommenderTest {
	private Recommender recommender;
	private Location defaultUserLocation;
	private RatingDB ratingDB;

	@Before
	public void setUp() {
		String url = System.getenv("DATABASE_URL");
		this.ratingDB = new RatingDB(url);
		recommender = new Recommender(new PointDB(url), ratingDB);
		this.defaultUserLocation = new Location(41.4034984, 2.1740598);
		ratingDB.saveRating(998, 2200465185l, Rating._4);
		ratingDB.saveRating(999, 249401770, Rating._4);
	}
	
	@After
	public void tearDown(){
		ratingDB.deleteAllUserRatings(998);
		ratingDB.deleteAllUserRatings(999);
	}

	@Test
	public void testRecommend() {
		// Prepare
		long userId = 999; // test user who liked Casa Battló
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);
		
		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommend(user);

		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(Recommender.NUM_RECOMMENDATIONS, recommendations.size());

		// Recommendations are unique
		Set<RecommendedPointOfInterest> s = new HashSet<>(recommendations);
		assertEquals(s.size(), recommendations.size());
	}

	@Test
	public void testRecommendForCategory() {
		// Prepare
		long userId = 999; // test user who liked Casa Battló
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);
		int categoryIndex = 3;

		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendForCategory(user, categoryIndex);

		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(Recommender.NUM_RECOMMENDATIONS, recommendations.size());
		// Recommendations are unique
		Set<RecommendedPointOfInterest> s = new HashSet<>(recommendations);
		assertEquals(s.size(), recommendations.size());
		
		for (RecommendedPointOfInterest recommendation : recommendations) {
			POIProfile profile = recommendation.getProfile();
			assertEquals(Preference.TRUE, profile.getAllCategories().get(categoryIndex));
		}
	}

	@Test
	public void testRecommendCollaborative() {
		// Prepare
		long userId = 999; // test user who liked Casa Battló
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);

		long recommendedId = 359086841; // Sagrada familia
		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendCollaborative(user);

		// Check
		assertFalse(recommendations.isEmpty());
		List<Long> ids = new ArrayList<>();
		for (RecommendedPointOfInterest poi : recommendations) {
			ids.add(poi.getId());
		}
		assertTrue(ids.contains(recommendedId));
	}

	@Test
	public void testRecommendCollaborativePOISameCategory() {
		// Prepare
		long userId = 998; // test user who has one restaurant rating
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);

		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendCollaborative(user);

		// Check
		assertFalse(recommendations.isEmpty());
		RecommendedPointOfInterest pointOfInterest = recommendations.get(0);
		assertEquals(Preference.TRUE, pointOfInterest.getProfile().hasFood());
	}

	@Test
	public void testRecommendContentBasedCompleteProfile() {
		// Prepare
		long userId = 1;
		POIProfile profile = new POIProfile(Preference.TRUE, Preference.TRUE, Preference.FALSE,
				Preference.FALSE, Preference.FALSE, Preference.FALSE);
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);
		user.setProfile(profile);

		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendContentBased(user);

		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(Recommender.NUM_RECOMMENDATIONS, recommendations.size());
		RecommendedPointOfInterest poi1 = recommendations.get(0);
		RecommendedPointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE, poi1.getProfile().hasSightseeing());
		assertEquals(Preference.TRUE, poi1.getProfile().hasCulture());
		assertEquals(Preference.TRUE, poi2.getProfile().hasSightseeing());
		assertEquals(Preference.TRUE, poi2.getProfile().hasCulture());
	}

	@Test
	public void testRecommendContentBasedSightseeing() {
		// Prepare
		long userId = 1;
		POIProfile profile = new POIProfile(Preference.TRUE, Preference.NOT_RATED, Preference.NOT_RATED,
				Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED);
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);
		user.setProfile(profile);

		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendContentBased(user);

		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(Recommender.NUM_RECOMMENDATIONS, recommendations.size());
		RecommendedPointOfInterest poi1 = recommendations.get(0);
		RecommendedPointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE, poi1.getProfile().hasSightseeing());
		assertEquals(Preference.TRUE, poi2.getProfile().hasSightseeing());
	}

	@Test
	public void testRecommendContentBasedCulture() {
		// Prepare
		long userId = 1;
		POIProfile profile = new POIProfile(Preference.NOT_RATED, Preference.TRUE, Preference.NOT_RATED,
				Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED);
		assertEquals(Preference.TRUE, profile.hasCulture());
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);
		user.setProfile(profile);

		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendContentBased(user);

		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(Recommender.NUM_RECOMMENDATIONS, recommendations.size());
		RecommendedPointOfInterest poi1 = recommendations.get(0);
		RecommendedPointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE, poi1.getProfile().hasCulture());
		assertEquals(Preference.TRUE, poi2.getProfile().hasCulture());
	}

	@Test
	public void testRecommendContentBasedFood() {
		// Prepare
		long userId = 1;
		POIProfile profile = new POIProfile(Preference.NOT_RATED, Preference.NOT_RATED, Preference.TRUE,
				Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED);
		assertEquals(Preference.TRUE, profile.hasFood());
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);
		user.setProfile(profile);;

		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendContentBased(user);

		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(Recommender.NUM_RECOMMENDATIONS, recommendations.size());
		RecommendedPointOfInterest poi1 = recommendations.get(0);
		RecommendedPointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE, poi1.getProfile().hasFood());
		assertEquals(Preference.TRUE, poi2.getProfile().hasFood());
	}

	@Test
	public void testRecommendContentBasedNature() {
		// Prepare
		long userId = 1;
		POIProfile profile = new POIProfile(Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED,
				Preference.NOT_RATED, Preference.TRUE, Preference.NOT_RATED);
		assertEquals(Preference.TRUE, profile.hasNature());
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);
		user.setProfile(profile);

		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendContentBased(user);

		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(Recommender.NUM_RECOMMENDATIONS, recommendations.size());
		RecommendedPointOfInterest poi1 = recommendations.get(0);
		RecommendedPointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE, poi1.getProfile().hasNature());
		assertEquals(Preference.TRUE, poi2.getProfile().hasNature());
	}

	@Test
	public void testRecommendContentBasedNightlife() {
		// Prepare
		long userId = 1;
		POIProfile profile = new POIProfile(Preference.FALSE, Preference.FALSE, Preference.FALSE,
				Preference.TRUE, Preference.FALSE, Preference.FALSE);
		assertEquals(Preference.TRUE, profile.hasNightlife());
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);
		user.setProfile(profile);

		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendContentBased(user);

		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(Recommender.NUM_RECOMMENDATIONS, recommendations.size());
		RecommendedPointOfInterest poi1 = recommendations.get(0);
		RecommendedPointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE, poi1.getProfile().hasNightlife());
		assertEquals(Preference.TRUE, poi2.getProfile().hasNightlife());
	}

	@Test
	public void testRecommendContentBasedNightlifeNotRated() {
		// Prepare
		long userId = 1;
		POIProfile sightseeingProfile = new POIProfile(Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED,
				Preference.TRUE, Preference.NOT_RATED, Preference.NOT_RATED);
		assertEquals(Preference.TRUE, sightseeingProfile.hasNightlife());
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);
		user.setProfile(sightseeingProfile);

		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendContentBased(user);

		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(Recommender.NUM_RECOMMENDATIONS, recommendations.size());
		RecommendedPointOfInterest poi1 = recommendations.get(0);
		RecommendedPointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE, poi1.getProfile().hasNightlife());
		assertEquals(Preference.TRUE, poi2.getProfile().hasNightlife());
	}

	@Test
	public void testRecommendContentBasedShopping() {
		// Prepare
		long userId = 1000;
		POIProfile profile = new POIProfile(Preference.NOT_RATED, Preference.NOT_RATED, Preference.NOT_RATED,
				Preference.NOT_RATED, Preference.NOT_RATED, Preference.TRUE);
		assertEquals(Preference.TRUE, profile.hasShopping());
		User user = new User(userId, "");
		user.setCurrentLocation(defaultUserLocation);
		user.setProfile(profile);

		// Action
		List<RecommendedPointOfInterest> recommendations = recommender.recommendContentBased(user);

		// Check
		assertFalse(recommendations.isEmpty());
		assertEquals(Recommender.NUM_RECOMMENDATIONS, recommendations.size());
		RecommendedPointOfInterest poi1 = recommendations.get(0);
		RecommendedPointOfInterest poi2 = recommendations.get(1);
		assertEquals(Preference.TRUE, poi1.getProfile().hasShopping());
		assertEquals(Preference.TRUE, poi2.getProfile().hasShopping());
	}
}
