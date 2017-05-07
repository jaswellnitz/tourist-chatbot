package bot;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import chatbot.AgentHandler;
import chatbot.AgentResponse;
import util.PropertyLoader;

public class AgentHandlerTest {
	
	private AgentHandler agentHandler;

	@Before
	public void setUp(){
		this.agentHandler = new AgentHandler(PropertyLoader.getProperty("clientAccessToken"));
	}
	
	@Test
	public void testSendQuery(){
		// Prepare
		String userInput = "Hello";

		// Action
		AgentResponse agentResult = agentHandler.sendQuery(userInput);
		
		// Check
		assertNotNull(agentResult);
		assertEquals(userInput, agentResult.getResolvedQuery());
		assertFalse(agentResult.getReply().isEmpty());
		System.out.println(agentResult);
	}

}
