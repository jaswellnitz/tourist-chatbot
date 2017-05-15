package bot;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import chatbot.AgentHandler;
import chatbot.ChatbotResponse;
import chatbot.TouristChatbot;
import data_access.DatabaseAccess;
import data_access.PointConverter;
import data_access.UserDB;
import data_access.UserRatingHandler;
import model.POIProfile;
import model.Preference;
import model.User;
import recommender.Recommender;

public class TouristChatbotTest {

	private TouristChatbot touristChatbot;
	private UserDB userDB;
	private User user;
	private AgentHandler agentHandler;

	@Before
	public void setUp() {
		this.userDB = new UserDB(new DatabaseAccess(System.getenv("JDBC_DATABASE_URL")));
		agentHandler = new AgentHandler(System.getenv("API_AI_ACCESS_TOKEN"));
		Recommender recommender = new Recommender(new PointConverter());
		this.touristChatbot = new TouristChatbot(agentHandler, recommender, userDB, new UserRatingHandler());
		user = new User(1234567890, "Testuser");
		userDB.storeUser(user);
	}

	@After
	public void tearDown() {
		if (userDB.hasUser(user.getId())) {
			userDB.deleteUser(user.getId());
		}
		agentHandler.resetContext(user.getId());
	}

	@Test
	public void testWelcome() {
		// Prepare
		userDB.deleteUser(user.getId());
		assertFalse(userDB.hasUser(user.getId()));
		assertFalse(touristChatbot.getActiveUsers().containsKey(user.getId()));

		// Action
		ChatbotResponse response  = touristChatbot.processStartMessage(user.getId(), user.getName());

		// Check
		String answer = response.getReply();
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
		ChatbotResponse response = touristChatbot.processInput(user.getId(), input);

		// Check
		String answer = response.getReply();
		assertFalse(answer.isEmpty());
		assertTrue(touristChatbot.getActiveUsers().containsKey(user.getId()));
		assertTrue(userDB.hasUser(user.getId()));
		User storedUser = userDB.getUser(user.getId());
		User cachedUser = touristChatbot.getActiveUsers().get(user.getId());
		assertEquals(storedUser,cachedUser);
		assertEquals(radius,storedUser.getPrefRecommendationRadius());
		assertEquals(radius, cachedUser.getPrefRecommendationRadius());
	}
	
	
	@Test
	public void testSaveInterest() {
		// Prepare
		touristChatbot.getActiveUsers().put(user.getId(), user);
		agentHandler.resetContext(user.getId());
		touristChatbot.processInput(user.getId(), "Let's talk.");
		POIProfile emptyProfile = new POIProfile(); // empty profile
		POIProfile expectedProfile = new POIProfile(Preference.TRUE,Preference.TRUE,Preference.NOT_RATED,Preference.NOT_RATED,Preference.NOT_RATED, Preference.TRUE);
		assertEquals(emptyProfile,user.getProfile());
		
		// Action
		touristChatbot.processInput(user.getId(), "I am interested in museums, sightseeing and shopping.");
		touristChatbot.processInput(user.getId(), "Yes");
		
		
		// Check
		User cachedUser = touristChatbot.getActiveUsers().get(user.getId());
		assertEquals(expectedProfile, cachedUser.getProfile());
		User storedUser = userDB.getUser(user.getId());
		assertEquals(expectedProfile, storedUser.getProfile());
	}
	
	
	
	@Test
	public void testGetPersonalInformation(){
		// Prepare
		String input = "What do you know about me?";
		user.setProfile(new POIProfile(Preference.TRUE, Preference.NOT_RATED, Preference.TRUE, Preference.FALSE, Preference.TRUE, Preference.TRUE));
		user.setPrefRecommendationRadius(300);
		touristChatbot.getActiveUsers().put(user.getId(), user);
		String expectedAnswer = "So, here's what I know about you: Your current recommendation radius is 300 m.\n\n"
				+ "You are interested in: sightseeing, food, nature, shopping.";
		
		// Action
		ChatbotResponse response = touristChatbot.processInput(user.getId(), input);
		
		// Check
		String answer = response.getReply();
		assertFalse(answer.isEmpty());
		assertEquals(expectedAnswer, answer);
	}
}
