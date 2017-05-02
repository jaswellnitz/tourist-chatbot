package touristbot;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import data_access.PointConverter;
import model.POIProfile;
import model.PointOfInterest;
import model.Preference;

public class PointConverterTest {
	private PointConverter pointConverter;
	
	@Before
	public void setUp(){
		pointConverter = new PointConverter();
	}
	
	@Test
	public void testGetPOIForIdInRadius(){
		// Prepare
		int radius = 50;
		long itemId = 359086841;
		String lat = "41.4034984";
		String lon = "2.1740598";
		POIProfile profile = new POIProfile(Preference.TRUE, Preference.TRUE, Preference.FALSE, Preference.FALSE, Preference.FALSE, Preference.FALSE);
		PointOfInterest sagradaFamilia = new PointOfInterest(359086841l, "Basílica de la Sagrada Família", "Carrer de Mallorca","403", 0, "Mo-Su 09:00-20:00",profile);
		
		// Action
		List<PointOfInterest> poi = pointConverter.getPOIForId(lat, lon, itemId, radius);
		
		// Check
		assertEquals(poi.size(),1);
		assertEquals(poi.get(0),sagradaFamilia);
	}
	
	@Test
	public void testGetPOIForIdNotInRadius(){
		// Prepare
		int radius = 30;
		long itemId = 359086841;
		String lat = "41.4031157";
		String lon = "2.1742518";
		
		// Action
		List<PointOfInterest> poi = pointConverter.getPOIForId(lat, lon, itemId, radius);
		
		// Check
		assertEquals(poi.size(),0);
	}

	@Test
	public void getPOIInRadius(){
		// Prepare
		int radius = 50;
		String lat = "41.4034984";
		String lon = "2.1740598";
		POIProfile profile = new POIProfile(Preference.TRUE, Preference.TRUE, Preference.FALSE, Preference.FALSE, Preference.FALSE, Preference.FALSE);
		PointOfInterest sagradaFamilia = new PointOfInterest(359086841l, "Basílica de la Sagrada Família", "Carrer de Mallorca","403", 0, "Mo-Su 09:00-20:00",profile);
		
		// Action
		List<PointOfInterest> poiInRadius = pointConverter.getPOIInRadius(lat, lon, radius);
		
		// Check
		assertEquals(poiInRadius.size(),3);
		boolean containsSagrada = poiInRadius.contains(sagradaFamilia);
		assertTrue(containsSagrada);
	}
}
