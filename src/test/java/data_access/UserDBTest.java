package data_access;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import model.POIProfile;
import model.Preference;
import model.User;

public class UserDBTest {

	private UserDB userDB;
	private User expectedUser;
	private DatabaseAccess dbAccess;

	@Before
	public void setUp() {
		this.dbAccess = new DatabaseAccess(System.getenv("JDBC_DATABASE_URL"));
		this.userDB = new UserDB(dbAccess);
		POIProfile profile = new POIProfile(Preference.TRUE,Preference.TRUE, Preference.FALSE, Preference.NOT_RATED,Preference.NOT_RATED, Preference.TRUE);
		this.expectedUser = new User(100l, "Testuser", 500, profile);
	}

	@After
	public void tearDown() {
		long id = expectedUser.getId();
		if (userDB.hasUser(id))
			userDB.deleteUser(id);
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
		long id = expectedUser.getId();
		userDB.storeUser(expectedUser);

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
