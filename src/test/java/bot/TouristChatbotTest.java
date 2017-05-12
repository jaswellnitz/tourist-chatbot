package bot;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import chatbot.AgentHandler;
import chatbot.TouristChatbot;
import model.User;

public class TouristChatbotTest {
	
	private TouristChatbot touristChatbot;
	
	@Before
	public void setUp(){
		this.touristChatbot = new TouristChatbot(new AgentHandler(System.getenv("API_AI_ACCESS_TOKEN")));
	}
}
