package dataAccess;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dataAccess.DatabaseAccess;
import dataAccess.PointConverter;
import dataAccess.UserDB;
import model.Location;
import model.POIProfile;
import model.Preference;
import model.RecommendedPointOfInterest;
import model.User;

public class UserDBTest {

	private UserDB userDB;
	private User expectedUser;
	private DatabaseAccess dbAccess;

	@Before
	public void setUp() {
		this.dbAccess = new DatabaseAccess(System.getenv("JDBC_DATABASE_URL"));
		this.userDB = new UserDB(dbAccess, new PointConverter(new DatabaseAccess(System.getenv("JDBC_DATABASE_URL"))));
		POIProfile profile = new POIProfile(Preference.TRUE,Preference.TRUE, Preference.FALSE, Preference.NOT_RATED,Preference.NOT_RATED, Preference.TRUE);
		this.expectedUser = new User(100l, "Testuser", 500, profile);
		Location location = new Location(41.4034984,2.1740598);
		RecommendedPointOfInterest sagradaFamilia = new RecommendedPointOfInterest(359086841l, "Basílica de la Sagrada Família",location, "Carrer de Mallorca","403", 0, "Mo-Su 09:00-20:00",
new POIProfile(Preference.TRUE, Preference.TRUE, Preference.FALSE, Preference.FALSE, Preference.FALSE, Preference.FALSE));
		expectedUser.addUnratedPOI(sagradaFamilia);
		expectedUser.addPositiveRecommendations(sagradaFamilia);
	}

	@After
	public void tearDown() {
		long id = expectedUser.getId();
		if (userDB.hasUser(id))
			userDB.deleteUser(id);
	}
	
	@Test
	public void testAddRecommendation(){
		// Prepare
		userDB.storeUser(expectedUser);
		Location location = new Location(41.2730278,1.880012);
		RecommendedPointOfInterest casaVella = new RecommendedPointOfInterest(2987711249l, "Casa Vella", location,"","", 0, "",
				new POIProfile(Preference.TRUE, Preference.FALSE, Preference.FALSE, Preference.FALSE, Preference.FALSE, Preference.FALSE));
		expectedUser.addPositiveRecommendations(casaVella);
		expectedUser.addUnratedPOI(casaVella);
		
		// Action
		userDB.addRecommendation(expectedUser.getId(), casaVella.getId());
		
		// Check
		User actualUser = userDB.getUser(expectedUser.getId());
		assertEquals(expectedUser,actualUser);
	}
	
	@Test
	public void testDeleteUnratedPOI(){
		// Prepare
		userDB.storeUser(expectedUser);
		expectedUser.getUnratedPOIs().remove(0);

		// Action
		userDB.deleteFirstUnratedRecommendation(expectedUser.getId());
		
		// Check
		User actualUser = userDB.getUser(expectedUser.getId());
		assertEquals(expectedUser,actualUser);
	}

	@Test
	public void testDeleteUser() {
		// Prepare
		userDB.storeUser(expectedUser);

		// Action
		boolean succesful = userDB.deleteUser(expectedUser.getId());

		// Check
		assertTrue(succesful);
		assertFalse(userDB.hasUser(expectedUser.getId()));
	}

	@Test
	public void testStoreInDB() {
		// Prepare
		long id = expectedUser.getId();
		assertFalse(userDB.hasUser(id));

		// Action
		boolean succesful = userDB.storeUser(expectedUser);

		// Check
		assertTrue(succesful);
		assertTrue(userDB.hasUser(id));
	}
	
	@Test
	public void testUpdateRadiusForUser() {
		// Prepare
		long id = expectedUser.getId();
		userDB.storeUser(expectedUser);
		int radius = 200;
		
		// Action
		boolean succesful = userDB.changeRadiusForUser(id, radius);

		// Check
		assertTrue(succesful);
		User user = userDB.getUser(id);
		assertEquals(radius, user.getPrefRecommendationRadius());
	}

	@Test
	public void testGetUser() {
		// Prepare
		userDB.storeUser(expectedUser);
		long id = expectedUser.getId();

		// Action
		User user = userDB.getUser(id);

		// Check
		assertEquals(expectedUser, user);
	}
	
	@Test
	public void testUpdateProfileForUser(){
		// Prepare
		userDB.storeUser(expectedUser);
		POIProfile profile = new POIProfile(Preference.TRUE,Preference.FALSE, Preference.TRUE, Preference.NOT_RATED,Preference.NOT_RATED, Preference.TRUE);
		
		// Action
		boolean succesful = userDB.changeProfileForUser(expectedUser.getId(), profile);
		
		// Check
		assertTrue(succesful);
		User user = userDB.getUser(expectedUser.getId());
		assertEquals(profile, user.getProfile());
	}
}
