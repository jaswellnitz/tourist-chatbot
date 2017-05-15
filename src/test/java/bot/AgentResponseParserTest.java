package bot;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import chatbot.Action;
import chatbot.AgentResponse;
import chatbot.AgentResponseParser;
import chatbot.Context;

public class AgentResponseParserTest {

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

		Map<String, Object> expectedParameters = new HashMap<>();
		expectedParameters.put("bla", "5");
		expectedParameters.put("city", "hamburg");
		List<Context> expectedContext = new ArrayList<>();
		Map<String, Object> expectedContextParameters = new HashMap<>(expectedParameters);
		expectedContextParameters.put("bla.original", "");
		expectedContextParameters.put("city.original", "");
		expectedContext.add(new Context("testcontext", expectedContextParameters, 5));

		// Action
		AgentResponse agentResult = AgentResponseParser.fromJson(json);

		// Check
		assertEquals("agent", agentResult.getSource());
		assertEquals("who are you", agentResult.getResolvedQuery());
		assertEquals(Action.ABOUT, agentResult.getAction());
		assertEquals("9196295d-f6f3-4d41-9750-b19d61d5ea6a", agentResult.getSessionId());
		assertFalse(agentResult.isActionIncomplete());
		assertEquals("Hey there, I am your friendly tourist chatbot!", agentResult.getReply());
		assertEquals(expectedParameters, agentResult.getParameters());
		assertEquals(expectedContext, agentResult.getContexts());
	}

	@Test
	public void testEmptyInput() {
		// Prepare
		String json = "{ \"id\": \"e1465d93-8a6f-4c8a-8838-e6fef839add8\", \"timestamp\": \"2017-05-06T14:21:50.159Z\", "
				+ "\"lang\": \"en\", \"result\": { \"source\": \"agent\", \"resolvedQuery\": \"\", "
				+ "\"action\": \"input.unknown\", \"actionIncomplete\": false, "
				+ "\"parameters\": {}, \"contexts\": [], \"metadata\": "
				+ "{ \"intentId\": \"d67a9f5f-d927-4a83-8c41-baa66d67c880\", \"webhookUsed\": \"false\", "
				+ "\"webhookForSlotFillingUsed\": \"false\", \"intentName\": \"Default Fallback Intent\" }, "
				+ "\"fulfillment\": { \"speech\": \"I missed what you said. Say it again?\", \"messages\": "
				+ "[ { \"type\": 0, \"speech\": \"Sorry, can you say that again?\" } ] }, \"score\": 1 }, \"status\": "
				+ "{ \"code\": 200, \"errorType\": \"success\" }, \"sessionId\": \"9196295d-f6f3-4d41-9750-b19d61d5ea6a\" }";

		Map<String, Object> expectedParameters = new HashMap<>();
		List<Context> expectedContext = new ArrayList<>();

		// Action
		AgentResponse agentResult = AgentResponseParser.fromJson(json);

		// Check
		assertEquals("agent", agentResult.getSource());
		assertEquals("", agentResult.getResolvedQuery());
		assertFalse(agentResult.isActionIncomplete());
		assertEquals(Action.NONE, agentResult.getAction());
		assertEquals("9196295d-f6f3-4d41-9750-b19d61d5ea6a", agentResult.getSessionId());
		assertEquals("I missed what you said. Say it again?", agentResult.getReply());
		assertEquals(expectedParameters, agentResult.getParameters());
		assertEquals(expectedContext, agentResult.getContexts());
	}

	@Test
	public void testDomainInput() {
		String json = "{ \"id\": \"786853d7-05d6-4921-89ed-a296bdc8ca9c\", \"timestamp\": \"2017-05-06T14:25:15.083Z\", "
				+ "\"result\": { \"source\": \"domains\", \"resolvedQuery\": \"Hello\", \"action\": \"smalltalk.greetings\", "
				+ "\"parameters\": { \"simplified\": \"hello\" }, \"metadata\": {}, \"fulfillment\": { \"speech\": \"Hey!\" }, "
				+ "\"score\": 1 }, \"status\": { \"code\": 200, \"errorType\": \"success\" }, "
				+ "\"sessionId\": \"9196295d-f6f3-4d41-9750-b19d61d5ea6a\" }";
		Map<String, Object> expectedParameters = new HashMap<>();
		expectedParameters.put("simplified", "hello");
		List<Context> expectedContext = new ArrayList<>();

		// Action
		AgentResponse agentResult = AgentResponseParser.fromJson(json);

		// Check
		assertEquals("domains", agentResult.getSource());
		assertEquals("Hello", agentResult.getResolvedQuery());
		assertEquals(Action.NONE, agentResult.getAction());
		assertFalse(agentResult.isActionIncomplete());
		assertEquals("Hey!", agentResult.getReply());
		assertEquals(expectedParameters, agentResult.getParameters());
		assertEquals(expectedContext, agentResult.getContexts());
	}

	@Test
	public void testComplexParameters() {
		String json = "{ \"id\": \"38155fd8-880a-4f49-9c28-d66abaa42f39\", \"timestamp\": \"2017-05-07T14:55:47.952Z\", "
				+ "\"lang\": \"en\", \"result\": { \"source\": \"agent\", \"resolvedQuery\": \"500 m\", \"action\": \"save_radius\","
				+ " \"actionIncomplete\": false, \"parameters\": { \"distance\": { \"amount\": 500, \"unit\": \"m\" } }, "
				+ "\"contexts\": [], \"metadata\": { \"intentId\": \"2051a6dd-efd3-423b-9157-6148acf6b91f\", \"webhookUsed\": \"false\", "
				+ "\"webhookForSlotFillingUsed\": \"false\", \"intentName\": \"change_distance\" }, \"fulfillment\": "
				+ "{ \"speech\": \"Fine, I will only recommend you places in less than 500 m\", \"messages\": "
				+ "[ { \"type\": 0, \"speech\": \"Fine, I will only recommend you places in less than 500 m\" } ] }, \"score\": 1 }, "
				+ "\"status\": { \"code\": 200, \"errorType\": \"success\" }, \"sessionId\": \"9196295d-f6f3-4d41-9750-b19d61d5ea6a\" }";
		Map<String, Object> expectedParameters = new HashMap<>();
		expectedParameters.put("distance", 500);

		// Action
		AgentResponse agentResult = AgentResponseParser.fromJson(json);

		// Check
		assertEquals(Action.SAVE_RADIUS, agentResult.getAction());
		assertEquals("9196295d-f6f3-4d41-9750-b19d61d5ea6a", agentResult.getSessionId());
		assertEquals(expectedParameters, agentResult.getParameters());
	}

	@Test
	public void testComplexParameterInterests() {
		String json = "{\"id\": \"e0921896-6d13-42e7-9b17-353b25c148ce\", \"timestamp\": \"2017-05-13T16:11:15.719Z\", \"lang\": \"en\", \"result\": "
				+ "{ \"source\": \"agent\", \"resolvedQuery\": \"yes\", \"action\": \"interview\", \"actionIncomplete\": false, \"parameters\": {}, "
				+ "\"contexts\": [ { \"name\": \"interview\", \"parameters\": { \"interest.original\": [ \"museums\", \"sightseeing\" ], \"interest\": "
				+ "[ \"culture\", \"sightseeing\" ] }, \"lifespan\": 3 } ], \"metadata\": { \"intentId\": \"b0ad9d5f-77f9-4c7f-895e-a8857415d596\", "
				+ "\"webhookUsed\": \"false\", \"webhookForSlotFillingUsed\": \"false\", \"intentName\": \"interview - yes\" }, \"fulfillment\": "
				+ "{ \"speech\": \"Ok great, I will keep that in mind! You can go back and change these information anytime you like.\", \"messages\": "
				+ "[ { \"type\": 0, \"speech\": \"Ok great, I will keep that in mind! You can go back and change these information anytime you like.\" } ] }, "
				+ "\"score\": 1 }, \"status\": { \"code\": 200, \"errorType\": \"success\" }, \"sessionId\": \"530ba681-42a8-4b1e-aa31-a1b48d9170f4\" }";
		Map<String, Object> expectedParameters = new HashMap<>();
		List<String> list = new ArrayList<>();
		list.add("culture");
		list.add("sightseeing");
		expectedParameters.put("interest", list);
		list = new ArrayList<>();
		list.add("museums");
		list.add("sightseeing");
		expectedParameters.put("interest.original", list);
		List<Context> contexts = new ArrayList<>();
		Context context = new Context("interview", expectedParameters, 3);
		contexts.add(context);
		
		// Action
		AgentResponse agentResult = AgentResponseParser.fromJson(json);

		// Check
		assertEquals(contexts, agentResult.getContexts());
	}
}