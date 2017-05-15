package chatbot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import data_access.PointConverter;
import data_access.UserDB;
import model.Location;
import model.POIProfile;
import model.Preference;
import model.User;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import recommender.Recommender;

public class TouristChatbot {

	private final AgentHandler agentHandler;
	private final Map<Long, User> activeUsers;
	private UserDB userDB;
//	 private Recommender recommender;

	public TouristChatbot(AgentHandler agentHandler, UserDB userDB) {
		this.agentHandler = agentHandler;
		this.activeUsers = ExpiringMap.builder().maxSize(100).expirationPolicy(ExpirationPolicy.ACCESSED)
				.expiration(1, TimeUnit.DAYS).build();
		this.userDB = userDB;
//		 this.recommender = new Recommender(new PointConverter(db));
	}

	public ChatbotResponse processInput(long userId, String userInput) {
		User user = getUserFromId(userId);
		System.out.println(userInput + ", " + user);
		AgentResponse agentResponse = agentHandler.sendUserInput(userInput, user.getId());
		String answer = "";
		ChatbotResponse chatbotResponse;
		switch (agentResponse.getAction()) {
		case ABOUT:
			answer = getAboutText(user.getPrefRecommendationRadius());
			chatbotResponse = new ChatbotResponse(answer);
			break;
		case SAVE_INTEREST:
			saveInterests(user, agentResponse);
			chatbotResponse = new ChatbotResponse(agentResponse.getReply());
			break;
		case SHOW_INFORMATION:
			answer = getPersonalInformation(user);
			chatbotResponse = new ChatbotResponse(answer);
			break;
		case RECOMMEND_LOCATION:
			chatbotResponse = new ChatbotResponse(agentResponse.getReply(),"Send Location");
			break;
		case SAVE_RADIUS:
			// TODO refactor
			if (trySaveRadius(user, agentResponse)) { 
				answer = agentResponse.getReply();
			} else {
				answer = "Please enter a valid input.";
			}
			chatbotResponse = new ChatbotResponse(answer);
			break;
		case NONE:
			chatbotResponse = new ChatbotResponse(agentResponse.getReply());
			break;
		default:
			chatbotResponse = new ChatbotResponse(agentResponse.getReply());
		}
		
		return chatbotResponse;
	}

	public ChatbotResponse processStartMessage(long userId, String userName) {
		if (userDB.hasUser(userId)) {
			userDB.deleteUser(userId);
		}

		User user = new User(userId, userName);
		userDB.storeUser(user);
		getActiveUsers().put(userId, user);
		AgentResponse agentResponse = agentHandler.sendEvent("WELCOME", user.getId(), true);
		System.out.println(agentResponse.getSessionId());
		return new ChatbotResponse(agentResponse.getReply());
	}

	public Map<Long, User> getActiveUsers() {
		return activeUsers;
	}
	
	public void recommend(long userId, Location location){
		User user = getUserFromId(userId);
		user.setCurrentLocation(location);
//		recommender.recommend(user);
	}

	// categories to enum
	private String getPersonalInformation(User user) {
		String answer = "So, here's what I know about you: Your current recommendation radius is "
				+ user.getPrefRecommendationRadius() + " m.";
//		String interests = "";
		List<String> interests = user.getProfile().getInterestsFromProfile();

		if (!interests.isEmpty()) {
			String interestString = "";
			for(String interest: interests){
				interestString += interest+", ";
			}
			interestString = interestString.substring(0, interestString.length() - 2);
			answer += "\n\nYou are interested in: " + interestString + ".";
		}

		return answer;
	}

	// TODO check if telegram bot responds when /start was not triggered
	private User getUserFromId(long userId) {
		if (getActiveUsers().containsKey(userId)) {
			return getActiveUsers().get(userId);
		}
		User user;
		if (userDB.hasUser(userId)) {
			user = userDB.getUser(userId);
		} else {
			user = new User(userId, "");
		}
		getActiveUsers().put(user.getId(), user);
		return user;
	}

	
	// TODO refactor
	private boolean trySaveRadius(User user, AgentResponse response) {
		boolean succesful = false;
		Object object = response.getParameters().get("distance");
		if (object instanceof Integer) {
			int radius = (int) object;
			if (radius > 0) {
				user.setPrefRecommendationRadius(radius);
				userDB.changeRadiusForUser(user.getId(), radius);
				succesful = true;
			}
		}
		return succesful;
	}

	// TODO store interests permanently
	private void saveInterests(User user, AgentResponse response) {
		for(Context context: response.getContexts()){
			if(context.getName().equals("interview")){
				List<String > interests = (List<String>) context.getParameters().get("interest");
				POIProfile profile = POIProfile.getProfileForInterests(interests);
				user.setProfile(profile);
			}
		}
	}

	private String getAboutText(int recommendationRadius) {
		String aboutText = "Hey there, I am your friendly tourist chatbot! I will try my best to recommend you cool places on your trip. "
				+ "As I get to know you better, my recommendations are going to be more adjusted to your interests."
				+ "\n\n" + "Currently, I am looking for recommendations in a distance of " + recommendationRadius
				+ " m from you. " + "You can change that distance anytime." + "\n\n"
				+ "If you already asked for recommendations, I can show them to you if you like."
				+ " While doing so, you can also tell me how you liked them in order to improve my recommendations.";
		return aboutText;
	}
}
