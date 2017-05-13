package bot;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import chatbot.AgentHandler;
import chatbot.TouristChatbot;
import data_access.DatabaseAccess;
import data_access.UserDB;
import model.User;

public class TouristChatbotTest {

	private TouristChatbot touristChatbot;
	private UserDB userDB;
	private User user;

	@Before
	public void setUp() {
		this.userDB = new UserDB(new DatabaseAccess(System.getenv("JDBC_DATABASE_URL")));
		this.touristChatbot = new TouristChatbot(new AgentHandler(System.getenv("API_AI_ACCESS_TOKEN")), userDB);
		user = new User(1234567890, "Testuser");
		userDB.storeUser(user);
	}

	@After
	public void tearDown() {
		if (userDB.hasUser(user.getId())) {
			userDB.deleteUser(user.getId());
		}
	}

	@Test
	public void testWelcome() {
		// Prepare
		userDB.deleteUser(user.getId());
		assertFalse(userDB.hasUser(user.getId()));
		assertFalse(touristChatbot.getActiveUsers().containsKey(user.getId()));

		// Action
		String answer = touristChatbot.processStartMessage(user.getId(), user.getName());

		// Check
		assertFalse(answer.isEmpty());
		assertTrue(userDB.hasUser(user.getId()));
		assertEquals(user, userDB.getUser(user.getId()));
		assertTrue(touristChatbot.getActiveUsers().containsKey(user.getId()));
	}

	@Test
	public void testChangeDistance() {
		// Prepare
		int radius = 500;
	
		String input = "I want to change the recommendation radius to " + radius +" m";
		
		// Action
		String answer = touristChatbot.processInput(user.getId(), input);

		assertFalse(answer.isEmpty());
		assertTrue(touristChatbot.getActiveUsers().containsKey(user.getId()));
		assertTrue(userDB.hasUser(user.getId()));
		User storedUser = userDB.getUser(user.getId());
		User cachedUser = touristChatbot.getActiveUsers().get(user.getId());
		assertEquals(storedUser,cachedUser);
		assertEquals(radius,storedUser.getPrefRecommendationRadius());
		assertEquals(radius, cachedUser.getPrefRecommendationRadius());
	}
}
