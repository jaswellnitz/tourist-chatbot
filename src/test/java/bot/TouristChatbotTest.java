package bot;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import chatbot.AgentHandler;
import chatbot.ChatbotResponse;
import chatbot.TouristChatbot;
import data_access.DatabaseAccess;
import data_access.PointConverter;
import data_access.UserDB;
import data_access.UserRatingHandler;
import model.POIProfile;
import model.Preference;
import model.Rating;
import model.RecommendedPointOfInterest;
import model.User;
import recommender.Recommender;

public class TouristChatbotTest {

	private TouristChatbot touristChatbot;
	private UserDB userDB;
	private User user;
	private AgentHandler agentHandler;
	private File ratingTestFile;
	private UserRatingHandler userRatingHandler;
	private static final String TEST_PATH = "src/test/resources/test.csv";
	private static final String DEFAULT_RATING_PATH = "src/main/resources/ratings.csv";

	@Before
	public void setUp() throws IOException {
		ratingTestFile = new File(TEST_PATH);
		this.userDB = new UserDB(new DatabaseAccess(System.getenv("JDBC_DATABASE_URL")));
		agentHandler = new AgentHandler(System.getenv("API_AI_ACCESS_TOKEN"));
		Recommender recommender = new Recommender(new PointConverter());
		this.userRatingHandler = new UserRatingHandler(TEST_PATH);
		this.touristChatbot = new TouristChatbot(agentHandler, recommender, userDB, userRatingHandler);
		user = new User(1234567890, "Testuser");
		userDB.storeUser(user);
		agentHandler.resetContext(user.getId());
		Files.copy(new File(DEFAULT_RATING_PATH), ratingTestFile);
	}

	@After
	public void tearDown() {
		if (userDB.hasUser(user.getId())) {
			userDB.deleteUser(user.getId());
		}
		agentHandler.resetContext(user.getId());
		ratingTestFile.delete();
	}

	@Test
	public void testWelcome() {
		// Prepare
		userDB.deleteUser(user.getId());
		assertFalse(userDB.hasUser(user.getId()));
		assertFalse(touristChatbot.getActiveUsers().containsKey(user.getId()));

		// Action
		ChatbotResponse response = touristChatbot.processStartMessage(user.getId(), user.getName());

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

		String input = "I want to change the recommendation radius to " + radius + " m";

		// Action
		List<ChatbotResponse> responses = touristChatbot.processInput(user.getId(), input);

		// Check
		assertFalse(responses.isEmpty());
		String answer = responses.get(0).getReply();
		assertFalse(answer.isEmpty());
		assertTrue(touristChatbot.getActiveUsers().containsKey(user.getId()));
		assertTrue(userDB.hasUser(user.getId()));
		User storedUser = userDB.getUser(user.getId());
		User cachedUser = touristChatbot.getActiveUsers().get(user.getId());
		assertEquals(storedUser, cachedUser);
		assertEquals(radius, storedUser.getPrefRecommendationRadius());
		assertEquals(radius, cachedUser.getPrefRecommendationRadius());
	}

	@Test
	public void testSaveInterest() {
		// Prepare
		touristChatbot.processInput(user.getId(), "Let's talk.");
		POIProfile emptyProfile = new POIProfile(); // empty profile
		POIProfile expectedProfile = new POIProfile(Preference.TRUE, Preference.TRUE, Preference.NOT_RATED,
				Preference.NOT_RATED, Preference.NOT_RATED, Preference.TRUE);
		assertEquals(emptyProfile, user.getProfile());

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
	public void testGetPersonalInformation() {
		// Prepare
		String input = "What do you know about me?";
		user.setProfile(new POIProfile(Preference.TRUE, Preference.NOT_RATED, Preference.TRUE, Preference.FALSE,
				Preference.TRUE, Preference.TRUE));
		user.setPrefRecommendationRadius(300);
		touristChatbot.getActiveUsers().put(user.getId(), user);
		String expectedAnswer = "So, here's what I know about you: Your current recommendation radius is 300 m.\n\n"
				+ "You are interested in: sightseeing, food, nature, shopping.";

		// Action
		List<ChatbotResponse> responses = touristChatbot.processInput(user.getId(), input);

		// Check
		assertFalse(responses.isEmpty());
		String answer = responses.get(0).getReply();
		assertFalse(answer.isEmpty());
		assertEquals(expectedAnswer, answer);
	}

	@Test
	public void testShowRecommendations() {
		// Prepare
		RecommendedPointOfInterest recPOI = new RecommendedPointOfInterest(1, "test", "", "", 20, "", new POIProfile());
		user.addPositiveRecommendations(recPOI);
		touristChatbot.getActiveUsers().put(user.getId(), user);
		
		// Action
		List<ChatbotResponse> responses = touristChatbot.processInput(user.getId(), "Show me my past recommendations");
		
		// Check
		assertTrue
	}

	@Test
	public void testShowRecommendationsWithRating() {
		// TODO implement
	}

	@Test
	public void testRate() {
		// TODO implement
	}

	@Test
	// TODO check persistency
	public void testRecommendPositive() {
		// Prepare
		String input = "I need a recommendation";
		String coordinates = "41.403706,2.173504";

		touristChatbot.processInput(user.getId(), input);
		touristChatbot.processInput(user.getId(), coordinates);

		User activeUser = touristChatbot.getActiveUsers().get(user.getId());
		List<RecommendedPointOfInterest> pendingRecommendations = new ArrayList<>(
				activeUser.getPendingRecommendations());
		assertFalse(pendingRecommendations.isEmpty());
		int size = pendingRecommendations.size();
		RecommendedPointOfInterest recPOI = activeUser.getPendingRecommendations().get(0);

		// Action
		touristChatbot.processInput(user.getId(), "Sounds good!");

		// Check
		activeUser = touristChatbot.getActiveUsers().get(user.getId());
		assertEquals(size - 1, activeUser.getPendingRecommendations().size());
		List<RecommendedPointOfInterest> posiveRecommendations = activeUser.getPositiveRecommendations();
		assertFalse(activeUser.getPendingRecommendations().contains(recPOI));
		assertEquals(1, posiveRecommendations.size());
		assertEquals(pendingRecommendations.get(0), posiveRecommendations.get(0));
		assertEquals(pendingRecommendations.get(0), activeUser.getUnratedPOIs().get(0));
		assertEquals(Rating._4, userRatingHandler.getUserRatingForItem(user.getId(), recPOI.getId()));
	}

	@Test
	public void testRecommendNegative() {
		// Prepare
		String input = "I need a recommendation";
		String coordinates = "41.403706,2.173504";

		touristChatbot.processInput(user.getId(), input);
		touristChatbot.processInput(user.getId(), coordinates);

		User activeUser = touristChatbot.getActiveUsers().get(user.getId());
		assertFalse(activeUser.getPendingRecommendations().isEmpty());
		int size = activeUser.getPendingRecommendations().size();
		RecommendedPointOfInterest recPOI = activeUser.getPendingRecommendations().get(0);

		// Action
		touristChatbot.processInput(user.getId(), "not interested");

		// Check
		activeUser = touristChatbot.getActiveUsers().get(user.getId());
		assertEquals(size - 1, activeUser.getPendingRecommendations().size());
		assertFalse(activeUser.getPendingRecommendations().contains(recPOI));
		activeUser = touristChatbot.getActiveUsers().get(user.getId());
		assertTrue(activeUser.getPositiveRecommendations().isEmpty());
		assertTrue(activeUser.getUnratedPOIs().isEmpty());
		assertEquals(Rating._1, userRatingHandler.getUserRatingForItem(activeUser.getId(), recPOI.getId()));
	}

	@Test
	public void testRecommendShowDifferentOptions() {
		// Prepare
		String input = "I need a recommendation";
		String coordinates = "41.403706,2.173504";

		touristChatbot.processInput(user.getId(), input);
		touristChatbot.processInput(user.getId(), coordinates);
		List<ChatbotResponse> responses = touristChatbot.processInput(user.getId(), "not interested");

		User activeUser = touristChatbot.getActiveUsers().get(user.getId());

		int size = activeUser.getPendingRecommendations().size();
		RecommendedPointOfInterest poi2 = activeUser.getPendingRecommendations().get(1);

		String option2 = responses.get(0).getKeyboardButtons().get(1);

		// Action
		touristChatbot.processInput(user.getId(), option2);
		touristChatbot.processInput(user.getId(), "Sounds good");

		// Check
		activeUser = touristChatbot.getActiveUsers().get(user.getId());
		assertEquals(size - 1, activeUser.getPendingRecommendations().size());
		assertFalse(activeUser.getPendingRecommendations().contains(poi2));
		assertEquals(1, activeUser.getPositiveRecommendations().size());
		assertEquals(poi2, activeUser.getPositiveRecommendations().get(0));
		assertEquals(poi2, activeUser.getUnratedPOIs().get(0));
		assertEquals(Rating._4, userRatingHandler.getUserRatingForItem(user.getId(), poi2.getId()));
	}

	@Test
	public void testRecommendShowAllOptions() {
		// Prepare
		String input = "I need a recommendation";
		String coordinates = "41.403706,2.173504";
		String positiveImpression = "Sounds good";
		String firstOption = "1";

		touristChatbot.processInput(user.getId(), input);
		touristChatbot.processInput(user.getId(), coordinates);
		User activeUser = touristChatbot.getActiveUsers().get(user.getId());
		List<RecommendedPointOfInterest> allRecommendations = new ArrayList<>(activeUser.getPendingRecommendations());

		touristChatbot.processInput(user.getId(), positiveImpression);

		// Action
		for (int i = 0; i < allRecommendations.size() - 1; i++) {
			touristChatbot.processInput(user.getId(), firstOption);
			touristChatbot.processInput(user.getId(), positiveImpression);
		}

		// Check
		activeUser = touristChatbot.getActiveUsers().get(user.getId());
		assertEquals(0, activeUser.getPendingRecommendations().size());
		assertEquals(allRecommendations, activeUser.getPositiveRecommendations());
		assertEquals(allRecommendations, activeUser.getUnratedPOIs());

		for (RecommendedPointOfInterest recPOI : allRecommendations) {
			assertEquals(Rating._4, userRatingHandler.getUserRatingForItem(user.getId(), recPOI.getId()));
		}
	}
}
