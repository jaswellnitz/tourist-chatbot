package poiRecommendation;

import static org.junit.Assert.*;

import org.junit.Test;

import model.RecommendedPointOfInterest;

public class PointOfInterestTest {

	@Test
	public void testPOIEmptyConstructor() {
		
		// Action
		RecommendedPointOfInterest poi = new RecommendedPointOfInterest(100, null,null, null, null, 0, null,null);
		
		// Check
		assertEquals("", poi.getAddress());
		assertEquals("", poi.getName());		
	}
	
	

}
