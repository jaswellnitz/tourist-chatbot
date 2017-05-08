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
		long sessionId = 1234567890;

		// Action
		AgentResponse agentResult = agentHandler.sendUserInput(userInput, sessionId);
		
		// Check
		assertNotNull(agentResult);
		assertEquals(userInput, agentResult.getResolvedQuery());
		assertEquals(String.valueOf(sessionId), agentResult.getSessionId());
		assertFalse(agentResult.getReply().isEmpty());
	}
	
	
	@Test
	public void testSendWelcomeEvent(){
		// Prepare
		String welcomeEvent = "WELCOME";
		long sessionId = 1234567890;

		// Action
		AgentResponse agentResult = agentHandler.sendEvent(welcomeEvent, sessionId);
		
		// Check
		assertNotNull(agentResult);
		assertFalse(agentResult.getReply().isEmpty());
		assertEquals(1,agentResult.getContexts().size());
		assertEquals(String.valueOf(sessionId),agentResult.getSessionId());
		
		assertEquals("welcome-followup",agentResult.getContexts().get(0).getName());
		System.out.println(agentResult);
	}
	
	@Test
	public void testDifferentSessions(){
		// Prepare
		String welcomeEvent = "WELCOME";
		long sessionIdUser1= 1234567890;
		
		String followUpInput = "I want the short version";
		long sessionIdUser2 = 1000000000;
		
		// Action
		AgentResponse agentResult = agentHandler.sendEvent(welcomeEvent, sessionIdUser1);
		
		// Check
		assertNotNull(agentResult);
		assertEquals(String.valueOf(sessionIdUser1),agentResult.getSessionId());
		assertFalse(agentResult.getReply().isEmpty());
		assertEquals(1,agentResult.getContexts().size());
		assertEquals("welcome-followup",agentResult.getContexts().get(0).getName());
		
		// Action
		AgentResponse agentResult2 = agentHandler.sendUserInput(followUpInput, sessionIdUser2);
		
		// Check
		assertNotNull(agentResult2);
		assertEquals(String.valueOf(sessionIdUser2),agentResult2.getSessionId());
		assertTrue(agentResult2.getContexts().isEmpty());
		assertFalse(agentResult2.getReply().isEmpty());
		
		// Action
		AgentResponse agentResult3 = agentHandler.sendUserInput(followUpInput, sessionIdUser1);
		
		// Check
		assertNotNull(agentResult3);
		assertEquals(String.valueOf(sessionIdUser1),agentResult3.getSessionId());
		assertFalse(agentResult3.getReply().isEmpty());
		assertFalse(agentResult3.getContexts().isEmpty());
		assertEquals("interview-short",agentResult3.getContexts().get(0).getName());
	}

}
