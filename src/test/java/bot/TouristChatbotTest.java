package bot;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import chatbot.AgentHandler;
import chatbot.TouristChatbot;
import model.User;
import util.PropertyLoader;

public class TouristChatbotTest {
	
	private TouristChatbot touristChatbot;
	
	@Before
	public void setUp(){
//		this.touristChatbot = new TouristChatbot(new AgentHandler(PropertyLoader.getProperty("clientAccessToken")));
	}

	@Test
	public void testProcessStartMessage(){
//		User user = new User(1l, "Testuser");
//		String reply= touristChatbot.processStartMessage(user);
		
//		assertFalse(reply.isEmpty());
	}
}
