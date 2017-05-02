package touristbot;

import static org.junit.Assert.*;

import org.junit.Test;

import model.PointOfInterest;

public class PointOfInterestTest {

	@Test
	public void testPOIEmptyConstructor() {
		
		// Action
		PointOfInterest poi = new PointOfInterest(100, null, null, null, 0, null,null);
		
		// Check
		assertEquals(poi.getAddress(), "");
		assertEquals(poi.getName(), "");		
	}
	
	

}
