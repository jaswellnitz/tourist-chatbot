package service.agent;

import java.util.List;
import java.util.Map;

import chatbot.Action;

/**
 * Contains the information of the API.AI agent response
 * @author Jasmin Wellnitz
 *
 */
// according to https://docs.api.ai/docs/query
public class AgentResponse {
	private final String resolvedQuery;
	private final Action action;
	private final String reply;
	private final Map<String, Object> parameters;
	private final List<Context> contexts;

	/**
	 * Agent response
	 * @param resolvedQuery
	 * @param action
	 * @param parameters
	 * @param contexts
	 * @param reply
	 */
	public AgentResponse(String resolvedQuery, Action action, Map<String, Object> parameters,
			List<Context> contexts, String reply) {
		this.resolvedQuery = resolvedQuery;
		this.action = action;
		this.parameters = parameters;
		this.contexts = contexts;
		this.reply = reply;
	}

	/**
	 * Gets the parameters filtered from the agent
	 * @return parameter map
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * Gets the agent's reply.
	 * @return agent reply
	 */
	public String getReply() {
		return reply;
	}

	/**
	 * Gets the original user query.
	 * @return resolved query
	 */
	public String getResolvedQuery() {
		return resolvedQuery;
	}

	/**
	 * Gets the active contexts
	 * @return list of contexts
	 */
	public List<Context> getContexts() {
		return contexts;
	}

	/**
	 * Gets the specified action that defines the conversation flow in the application.
	 * @return action
	 */
	public Action getAction() {
		return action;
	}
}
