package chatbot;

import java.util.HashSet;
import java.util.Set;

import model.User;

// TODO (integration) tests
public class TouristChatbot {

	private final AgentHandler agentHandler;
	private Set<User> activeUsers;
//	private UserDB userDB;

	public TouristChatbot(AgentHandler agentHandler) {
		this.agentHandler = agentHandler;
		this.activeUsers = new HashSet<>();
//		this.userDB = userDB;
	}

	public String processInput(long userId, String userInput) {
		User user = getUserFromId(userId);
		AgentResponse response = agentHandler.sendUserInput(userInput, user.getId());
		String answer = "";

		switch (response.getAction()) {
		case ABOUT:
			answer = getAboutText(user.getPrefRecommendationRadius());
			break;
		case SAVE_INTEREST:
			answer = response.getReply();
			// saveInterests(user, response);
			break;
		case SAVE_RADIUS:
			answer = response.getReply();
			if (trySaveRadius(user, response)) {
				answer = response.getReply();
			} else {
				// error handling
			}
			break;
		case NONE:
			answer = response.getReply();
			break;
		default:
			answer = response.getReply();
		}
		return answer;
	}

	// check if telegram bot responds when /start was not triggered
	private User getUserFromId(long userId) {
		for (User activeUser : activeUsers) {
			if (activeUser.getId() == userId) {
				return activeUser;
			}
		}
//		return userDB.getUser(userId);
		return new User(userId, "");
	}

	private boolean trySaveRadius(User user, AgentResponse response) {
		boolean succesful = false;
		Object object = response.getParameters().get("distance");
		if (object instanceof Integer) {
			int radius = (int) object;
			if (radius > 0) {
				user.setPrefRecommendationRadius(radius);
				succesful = true;
			}
		}
		return succesful;
	}

	private void saveInterests(User user, AgentResponse response) {
		// TODO implement
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

	// TODO store user in db
	public String processStartMessage(long userId, String userName) {
		User user = new User(userId, userName);
//		userDB.storeUser(user);
		activeUsers.add(user);
		AgentResponse response = agentHandler.sendEvent("WELCOME", user.getId());
		System.out.println(response.getSessionId());
		return response.getReply();
	}
}
