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
	public void testSendUserInput(){
		// Prepare
		String userInput = "Hello";

		// Action
		AgentResponse agentResult = agentHandler.sendUserInput(userInput);
		
		// Check
		assertNotNull(agentResult);
		assertEquals(userInput, agentResult.getResolvedQuery());
		assertFalse(agentResult.getReply().isEmpty());
	}
	
	
	@Test
	public void testSendWelcomeEvent(){
		// Prepare
		String welcomeEvent = "WELCOME";

		// Action
		AgentResponse agentResult = agentHandler.sendEvent(welcomeEvent);
		
		// Check
		assertNotNull(agentResult);
		assertFalse(agentResult.getReply().isEmpty());
		assertEquals(1,agentResult.getContext().size());
		assertEquals("welcome-followup",agentResult.getContext().get(0).getName());
		System.out.println(agentResult);
	}

}
