package touristbot;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import data_access.PointConverter;
import data_access.UserDataHandler;
import model.PointOfInterest;
import recommender.Recommender;


// TODO 
// Tests schreiben
// POIProfile an neue TAGS anpassen
// Radius in User abspeichern

public class RecommenderTest {
	private Recommender recommender;

	@Before
	public void setUp() throws Exception {
		recommender = new Recommender(new PointConverter(), new UserDataHandler());
	}

	@Test
	public void testRecommendCollaborative() {
		// Prepare
		long userId = 999; // test user - sightseer
		String lat = "41.4034984";
		String lon = "2.1740598";
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendCollaborative(userId, lat, lon);
		
		// Check
		for (PointOfInterest recommendedPOI : recommendations) {
			System.out.println("Recommended item for user " + userId + ": " + recommendedPOI);
		}
	}
	
	@Test
	public void testRecommendCollaborative2() {
		// Prepare
		long userId = 998; // test user - foodie
		String lat = "41.4034984";
		String lon = "2.1740598";
		
		// Action
		List<PointOfInterest> recommendations = recommender.recommendCollaborative(userId, lat, lon);
		
		// Check
		for (PointOfInterest recommendedPOI : recommendations) {
			System.out.println("Recommended item for user " + userId + ": " + recommendedPOI);
		}
	}
}
