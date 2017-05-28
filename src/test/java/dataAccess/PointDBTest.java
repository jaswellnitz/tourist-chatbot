package dataAccess;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

import dataAccess.PointDB;
import model.Location;
import model.POIProfile;
import model.RecommendedPointOfInterest;
import model.Preference;

public class PointDBTest {
	private PointDB pointConverter;
	private RecommendedPointOfInterest sagradaFamilia;
	
	@Before
	public void setUp(){
		String url = System.getenv("DATABASE_URL");
		pointConverter = new PointDB(url);
		Location location = new Location(41.4034984,2.1740598);
		POIProfile profile = new POIProfile(Preference.TRUE, Preference.TRUE, Preference.FALSE, Preference.FALSE, Preference.FALSE, Preference.FALSE);
		this.sagradaFamilia = new RecommendedPointOfInterest(359086841l, "Basílica de la Sagrada Família", location,"Carrer de Mallorca","403", 0, "Mo-Su 09:00-20:00",profile);
		
	}
	
	@Test
	public void testGetPOIForIdInRadius(){
		// Prepare
		int radius = 50;
		long itemId = 359086841;
		double lat = 41.4034984;
		double lon = 2.1740598;
	
		
		// Action
		List<RecommendedPointOfInterest> poi = pointConverter.getPOIForId(itemId, lat, lon, radius);
		
		// Check
		assertEquals(poi.size(),1);
		assertEquals(poi.get(0),sagradaFamilia);
	}
	
	@Test
	public void testGetPOIForIdNotInRadius(){
		// Prepare
		int radius = 30;
		long itemId = 359086841;
		double lat = 41.4031157;
		double lon = 2.1742518;
		
		// Action
		List<RecommendedPointOfInterest> poi = pointConverter.getPOIForId(itemId,lat, lon, radius);
		
		// Check
		assertEquals(poi.size(),0);
	}

	@Test
	public void getPOIInRadius(){
		// Prepare
		int radius = 50;
		double lat = 41.4034984;
		double lon = 2.1740598;
		
		// Action
		List<RecommendedPointOfInterest> poiInRadius = pointConverter.getPOIInRadius(lat, lon, radius);
		
		// Check
		assertEquals(poiInRadius.size(),4);
		boolean containsSagrada = poiInRadius.contains(sagradaFamilia);
		assertTrue(containsSagrada);
	}
}
