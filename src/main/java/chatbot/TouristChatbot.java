package chatbot;

import model.User;

// TODO (integration) tests
public class TouristChatbot {

	private final AgentHandler agentHandler;

	public TouristChatbot(AgentHandler agentHandler) {
		this.agentHandler = agentHandler;
	}

	public String processInput(User user, String userInput) {
		AgentResponse response = agentHandler.sendQuery(userInput);
		String answer = "";

		switch (response.getAction()) {
		case ABOUT:
			answer = getAboutText(user.getPrefRecommendationRadius());
			break;
		case SAVE_INTEREST:
			answer = response.getReply();
//			saveInterests(user, response);
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
				+ "from you. " + "You can change that distance anytime."  
				+"\n\n"
				+ "If you already asked for recommendations, I can show them to you if you like."
				+ " While doing so, you can also tell me how you liked them in order to improve my recommendations.";
		return aboutText;
	}

	// TODO send welcome event to API.ai
	public String processStartMessage(User from) {
		// TODO Auto-generated method stub
		return null;
	}

}
