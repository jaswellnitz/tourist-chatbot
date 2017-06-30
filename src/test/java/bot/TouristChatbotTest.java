package bot;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import chatbot.ChatbotResponse;
import chatbot.TouristChatbot;
import dataAccess.PointDB;
import dataAccess.RatingDB;
import dataAccess.UserDB;
import domain.Location;
import domain.Rating;
import domain.RecommendedPointOfInterest;
import domain.User;
import recommender.POIProfile;
import recommender.Preference;
import recommender.Recommender;
import service.ImageRequester;
import service.agent.AgentHandler;

public class TouristChatbotTest {

	private TouristChatbot touristChatbot;
	private UserDB userDB;
	private User user;
	private AgentHandler agentHandler;
	private RecommendedPointOfInterest recPOI;
	private RatingDB ratingDB;

	
	@Before
	public void setUp() throws IOException {
		String url = System.getenv("DATABASE_URL");
		PointDB pointConverter = new PointDB(url);
		this.userDB = new UserDB(url, pointConverter);
		ratingDB = new RatingDB(url);
		agentHandler = new AgentHandler(System.getenv("API_AI_ACCESS_TOKEN"));
		Recommender recommender = new Recommender(pointConverter, ratingDB);
		ImageRequester imageRequester = new ImageRequester(System.getenv("F_CLIENT_ID"), System.getenv("F_CLIENT_SECRET"));
		this.touristChatbot = new TouristChatbot(agentHandler, imageRequester, recommender, userDB, ratingDB);
		user = new User(1234567890, "Testuser");
		userDB.storeUser(user);
		agentHandler.resetContext(user.getId());
		Location location = new Location(41.4034984,2.1740598);
		this.recPOI = new RecommendedPointOfInterest(359086841l, "Basílica de la Sagrada Família",location, "Carrer de Mallorca","403", 0, "Mo-Su 09:00-20:00",
				new POIProfile(Preference.TRUE, Preference.TRUE, Preference.FALSE, Preference.FALSE, Preference.FALSE, Preference.FALSE));
	}

	@After
	public void tearDown() {
		if (userDB.hasUser(user.getId())) {
			userDB.deleteUser(user.getId());
		}
		if(ratingDB.hasRatingForUser(user.getId())){
			ratingDB.deleteAllUserRatings(user.getId());
		}
	}

	@Test
	public void deleteme(){
		String input = "I need a recommendation";
		String coordinates = "41.403706,2.173504";

		touristChatbot.processInput(user.getId(), input);
		agentHandler.checkContexts(user.getId());
		touristChatbot.processInput(user.getId(), coordinates);
		agentHandler.checkContexts(user.getId());
		touristChatbot.processInput(user.getId(),"Sounds good");
		agentHandler.checkContexts(user.getId());
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
		user.addPositiveRecommendations(recPOI);
		touristChatbot.getActiveUsers().put(user.getId(), user);

		// Action
		List<ChatbotResponse> responses = touristChatbot.processInput(user.getId(), "Show me my past recommendations");

		// Check
		assertEquals(1, responses.size());
		assertTrue(responses.get(0).getReply().contains(recPOI.getName()));
	}

	@Test
	public void testShowRecommendationsWithRating() throws SQLException {
		// Prepare
		ratingDB.saveRating(user.getId(), recPOI.getId(), Rating._4);
		user.addPositiveRecommendations(recPOI);
		user.addUnratedPOI(recPOI);
		userDB.addRecommendation(user.getId(), recPOI.getId());
		touristChatbot.getActiveUsers().put(user.getId(), user);

		// Action
		List<ChatbotResponse> responses = touristChatbot.processInput(user.getId(), "Show me my past recommendations");

		// Check
		assertEquals(2, responses.size());
		assertTrue(responses.get(0).getReply().contains(recPOI.getName()));
		
		// Action
		touristChatbot.processInput(user.getId(), "3");
		
		// Check
		User activeUser = touristChatbot.getActiveUsers().get(user.getId());
		assertEquals(Rating._3,ratingDB.getRating(user.getId(), recPOI.getId()));
		assertTrue(activeUser.getUnratedPOIs().isEmpty());
		User storedUser = userDB.getUser(user.getId());
		assertEquals(activeUser.getPositiveRecommendations(),storedUser.getPositiveRecommendations());
		assertEquals(activeUser.getUnratedPOIs(),storedUser.getUnratedPOIs());
	}

	@Test
	public void testRating() throws SQLException{
		// Prepare
		ratingDB.saveRating(user.getId(), recPOI.getId(), Rating._4);
		user.addUnratedPOI(recPOI);
		user.addPositiveRecommendations(recPOI);
		userDB.addRecommendation(user.getId(), recPOI.getId());
		touristChatbot.getActiveUsers().put(user.getId(), user);
		
		// Action
		List<ChatbotResponse> responses = touristChatbot.processInput(user.getId(), "Hey");
		
		// Check
		assertEquals(2,responses.size());
		// Rating count is 5
		assertEquals(5, responses.get(1).getKeyboardButtons().size());
		
		// Action
		touristChatbot.processInput(user.getId(), "5");
		
		// Check
		User activeUser = touristChatbot.getActiveUsers().get(user.getId());
		assertEquals(Rating._5,ratingDB.getRating(user.getId(), recPOI.getId()));
		assertTrue(activeUser.getUnratedPOIs().isEmpty());
		User storedUser = userDB.getUser(user.getId());
		assertEquals(activeUser.getPositiveRecommendations(),storedUser.getPositiveRecommendations());
		assertEquals(activeUser.getUnratedPOIs(),storedUser.getUnratedPOIs());
	}
	
	@Test
	public void testRecommendPositive() throws SQLException {
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
		assertEquals(Rating._4, ratingDB.getRating(user.getId(), recPOI.getId()));
		User storedUser = userDB.getUser(user.getId());
		assertEquals(activeUser.getPositiveRecommendations(),storedUser.getPositiveRecommendations());
		assertEquals(activeUser.getUnratedPOIs(),storedUser.getUnratedPOIs());
	}

	@Test
	public void testRecommendNegative() throws SQLException {
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
		assertEquals(Rating._1, ratingDB.getRating(activeUser.getId(), recPOI.getId()));
		User storedUser = userDB.getUser(user.getId());
		assertEquals(activeUser.getPositiveRecommendations(),storedUser.getPositiveRecommendations());
		assertEquals(activeUser.getUnratedPOIs(),storedUser.getUnratedPOIs());
	}

	@Test
	public void testRecommendShowDifferentOptions() throws SQLException {
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
		assertEquals(Rating._4, ratingDB.getRating(user.getId(), poi2.getId()));
		User storedUser = userDB.getUser(user.getId());
		assertEquals(activeUser.getPositiveRecommendations(),storedUser.getPositiveRecommendations());
		assertEquals(activeUser.getUnratedPOIs(),storedUser.getUnratedPOIs());
	}

	@Test
	public void testRecommendShowAllOptions() throws SQLException {
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
			assertEquals(Rating._4, ratingDB.getRating(user.getId(), recPOI.getId()));
		}
		User storedUser = userDB.getUser(user.getId());
		assertEquals(activeUser.getPositiveRecommendations(),storedUser.getPositiveRecommendations());
		assertEquals(activeUser.getUnratedPOIs(),storedUser.getUnratedPOIs());
	}
	
	@Test
	public void testRecommendCategory() {
		// Prepare
		String input = "I want to do some shopping";
		String coordinates = "41.403706,2.173504";

		// Action
		touristChatbot.processInput(user.getId(), input);
		touristChatbot.processInput(user.getId(), coordinates);
		User activeUser = touristChatbot.getActiveUsers().get(user.getId());


		// Check
		activeUser = touristChatbot.getActiveUsers().get(user.getId());
		List<RecommendedPointOfInterest> pendingRecommendations = activeUser.getPendingRecommendations();
		for(RecommendedPointOfInterest recPOI: pendingRecommendations){
			assertTrue(recPOI.getProfile().hasShopping().toBoolean());
		}
	}
}
