package data_access;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import model.User;

public class UserDBTest {

	private UserDB userDB;
	private User expectedUser;
	private DatabaseAccess dbAccess;

	@Before
	public void setUp() {
		this.dbAccess = new DatabaseAccess(System.getenv("JDBC_DATABASE_URL"));
		this.userDB = new UserDB(dbAccess);
		this.expectedUser = new User(100l, "Testuser");
	}

	@After
	public void tearDown() {
		String query = "Delete from users where id = " + expectedUser.getId();
		dbAccess.executeUpdate(query);
	}

	@Test
	public void testStoreInDB() {
		// Prepare
		long id = expectedUser.getId();
		assertFalse(userDB.hasUser(id));

		// Action
		userDB.storeUser(expectedUser);

		// Check
		assertTrue(userDB.hasUser(id));
	}

	@Test
	public void testGetUser() {
		// Prepare
		long id = expectedUser.getId();
		userDB.storeUser(expectedUser);
		
		// Action
		User user = userDB.getUser(id);
		
		// Check
		assertEquals(expectedUser,user);
	}
}
