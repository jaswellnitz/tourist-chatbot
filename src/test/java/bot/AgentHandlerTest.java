package bot;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import service.agent.AgentHandler;
import service.agent.AgentResponse;
import service.agent.Parameter;

public class AgentHandlerTest {
	
	private AgentHandler agentHandler;

	@Before
	public void setUp(){
		this.agentHandler = new AgentHandler(System.getenv("API_AI_ACCESS_TOKEN"));
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
		assertFalse(agentResult.getReply().isEmpty());
	}
	
	
	@Test
	public void testSendWelcomeEvent(){
		// Prepare
		String welcomeEvent = "WELCOME";
		long sessionId = 1234567890l;

		// Action
		AgentResponse agentResult = agentHandler.sendEvent(welcomeEvent, sessionId, true);
		
		// Check
		assertNotNull(agentResult);
		assertFalse(agentResult.getReply().isEmpty());
		assertEquals(1,agentResult.getContexts().size());
		
		assertEquals("welcome-followup",agentResult.getContexts().get(0).getName());
	}
	@Test
	public void testResetContext(){
		// Prepare
		long sessionId = 1234567890;
		AgentResponse previousResponse = agentHandler.sendEvent("WELCOME", sessionId, true);
		assertFalse(previousResponse.getContexts().isEmpty());
		
		// Action
		AgentResponse agentResponse = agentHandler.resetContext(sessionId);
		
		// Check
		assertTrue(agentResponse.getContexts().isEmpty());
		assertTrue(agentResponse.getParameters().isEmpty());
	}
	
	@Test
	public void testSetContext(){
		// Prepare
		long sessionId = 1234567890;
		String context = "rate";
		String expectedRating = "5";
		
		// Action
		AgentResponse setContext = agentHandler.setContext(context, sessionId);
		AgentResponse agentResponse = agentHandler.sendUserInput(expectedRating, sessionId);
		
		// Check
		assertEquals(context,setContext.getContexts().get(0).getName());
		String rating = (String)agentResponse.getParameters().get(Parameter.RATING.name());
		assertEquals(expectedRating,rating);
	}
	
	
	@Test
	public void testDifferentSessions(){
		// Prepare
		String welcomeEvent = "WELCOME";
		long sessionIdUser1= 2345678901l;
		
		String followUpInput = "Yes";
		long sessionIdUser2 = 3456789012l;
		
		// Action
		AgentResponse agentResult = agentHandler.sendEvent(welcomeEvent, sessionIdUser1, true);
		
		// Check
		assertNotNull(agentResult);
		assertFalse(agentResult.getReply().isEmpty());
		System.out.println(agentResult.getContexts());
		assertEquals(1,agentResult.getContexts().size());
		assertEquals("welcome-followup",agentResult.getContexts().get(0).getName());
		
		// Action
		AgentResponse agentResult2 = agentHandler.sendUserInput(followUpInput, sessionIdUser2);
		
		// Check
		assertNotNull(agentResult2);
		assertTrue(agentResult2.getContexts().isEmpty());
		assertFalse(agentResult2.getReply().isEmpty());
		
		// Action
		AgentResponse agentResult3 = agentHandler.sendUserInput(followUpInput, sessionIdUser1);
		
		// Check
		assertNotNull(agentResult3);
		assertFalse(agentResult3.getReply().isEmpty());
		assertFalse(agentResult3.getContexts().isEmpty());
		assertEquals("interview",agentResult3.getContexts().get(0).getName());
	}
}
