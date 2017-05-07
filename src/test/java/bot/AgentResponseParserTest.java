package bot;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonObject;

import chatbot.AgentResult;
import chatbot.Context;
import util.JsonUtil;

public class AgentResultTest {

	@Test
	public void testFromJson() {
		// Prepare
		String json = "{ \"id\": \"9a76bd3a-3f41-4fc2-bbdc-c29242ce2b1b\", \"timestamp\": \"2017-05-06T10:18:19.271Z\", \"lang\": \"en\", "
				+ "\"result\": { \"source\": \"agent\", \"resolvedQuery\": \"who are you\", \"action\": \"about\", \"actionIncomplete\": false, "
				+ "\"parameters\": { \"bla\": \"5\", \"city\": \"hamburg\" }, \"contexts\": [ { \"name\": \"testcontext\", "
				+ "\"parameters\": { \"city\": \"hamburg\", \"bla\": \"5\", \"bla.original\": \"\", \"city.original\": \"\" },"
				+ " \"lifespan\": 5 } ], \"metadata\": { \"intentId\": \"404c0183-350e-460a-9b99-9452f51fe6a1\", \"webhookUsed\": \"false\", "
				+ "\"webhookForSlotFillingUsed\": \"false\", \"intentName\": \"About\" }, \"fulfillment\":"
				+ " { \"speech\": \"Hey there, I am your friendly tourist chatbot!\", \"messages\": [ { \"type\": 0, "
				+ "\"speech\": \"Hey there, I am your friendly tourist chatbot!\" } ] }, \"score\": 1 }, "
				+ "\"status\": { \"code\": 200, \"errorType\": \"success\" }, \"sessionId\": \"9196295d-f6f3-4d41-9750-b19d61d5ea6a\" }";

		Map<String,String> expectedParameters = new HashMap<>();
		expectedParameters.put("bla","5");
		expectedParameters.put("city", "hamburg");
		List<Context> expectedContext = new ArrayList<>();
		Map<String,String> expectedContextParameters = new HashMap<>(expectedParameters);
		expectedContextParameters.put("bla.original","");
		expectedContextParameters.put("city.original","");
		expectedContext.add(new Context("testcontext", expectedContextParameters, 5));
		
		// Action
		AgentResult agentResult = AgentResult.fromJson(json);
		
		// Check
		assertEquals("agent", agentResult.getSource());
		assertEquals("who are you", agentResult.getResolvedQuery());
		assertEquals("about", agentResult.getAction());
		assertEquals("Hey there, I am your friendly tourist chatbot!", agentResult.getReply());
		assertTrue(agentResult.getScore() == 1.0);
		assertEquals(expectedParameters, agentResult.getParameters());
		assertEquals(expectedContext,agentResult.getContext());
	}
	
	@Test
	public void testEmptyInput(){
		// Prepare
		String json ="{ \"id\": \"e1465d93-8a6f-4c8a-8838-e6fef839add8\", \"timestamp\": \"2017-05-06T14:21:50.159Z\", "
				+ "\"lang\": \"en\", \"result\": { \"source\": \"agent\", \"resolvedQuery\": \"\", "
				+ "\"action\": \"input.unknown\", \"actionIncomplete\": false, "
				+ "\"parameters\": {}, \"contexts\": [], \"metadata\": "
				+ "{ \"intentId\": \"d67a9f5f-d927-4a83-8c41-baa66d67c880\", \"webhookUsed\": \"false\", "
				+ "\"webhookForSlotFillingUsed\": \"false\", \"intentName\": \"Default Fallback Intent\" }, "
				+ "\"fulfillment\": { \"speech\": \"I missed what you said. Say it again?\", \"messages\": "
				+ "[ { \"type\": 0, \"speech\": \"Sorry, can you say that again?\" } ] }, \"score\": 1 }, \"status\": "
				+ "{ \"code\": 200, \"errorType\": \"success\" }, \"sessionId\": \"9196295d-f6f3-4d41-9750-b19d61d5ea6a\" }";
		
		Map<String,String> expectedParameters = new HashMap<>();
		List<Context> expectedContext = new ArrayList<>();
		
		// Action
		AgentResult agentResult = AgentResult.fromJson(json);
		
		// Check
		assertEquals("agent", agentResult.getSource());
		assertEquals("", agentResult.getResolvedQuery());
		assertEquals("input.unknown", agentResult.getAction());
		assertEquals("I missed what you said. Say it again?", agentResult.getReply());
		assertTrue(agentResult.getScore() == 1.0);
		assertEquals(expectedParameters, agentResult.getParameters());
		assertEquals(expectedContext,agentResult.getContext());
	}
	
	@Test
	public void testDomainInput(){
		String json = "{ \"id\": \"786853d7-05d6-4921-89ed-a296bdc8ca9c\", \"timestamp\": \"2017-05-06T14:25:15.083Z\", "
				+ "\"result\": { \"source\": \"domains\", \"resolvedQuery\": \"Hello\", \"action\": \"smalltalk.greetings\", "
				+ "\"parameters\": { \"simplified\": \"hello\" }, \"metadata\": {}, \"fulfillment\": { \"speech\": \"Hey!\" }, "
				+ "\"score\": 1 }, \"status\": { \"code\": 200, \"errorType\": \"success\" }, "
				+ "\"sessionId\": \"9196295d-f6f3-4d41-9750-b19d61d5ea6a\" }";
		Map<String,String> expectedParameters = new HashMap<>();
		expectedParameters.put("simplified","hello");
		List<Context> expectedContext = new ArrayList<>();
		
		// Action
		AgentResult agentResult = AgentResult.fromJson(json);
		
		// Check
		assertEquals("domains", agentResult.getSource());
		assertEquals("Hello", agentResult.getResolvedQuery());
		assertEquals("smalltalk.greetings", agentResult.getAction());
		assertEquals("Hey!", agentResult.getReply());
		assertTrue(agentResult.getScore() == 1.0);
		assertEquals(expectedParameters, agentResult.getParameters());
		assertEquals(expectedContext,agentResult.getContext());
	}
}