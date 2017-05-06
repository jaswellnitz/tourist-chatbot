package chatbot;

import model.User;

public class TouristChatbot {

	private final AgentHandler agentHandler;

	public TouristChatbot(AgentHandler agentHandler) {
		this.agentHandler = agentHandler;
		// add recommender
	}
	
	public String processInput(User user, String userInput) {
		return agentHandler.sendQuery(userInput).getReply();
	}

	public String processStartMessage(User from) {
		// TODO Auto-generated method stub
		return null;
	}

}
