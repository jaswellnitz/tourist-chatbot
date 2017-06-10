package model;

import java.util.List;
import java.util.Map;

import chatbot.Action;

// according to https://docs.api.ai/docs/query
public class AgentResponse {
	private final String resolvedQuery;
	private final Action action;
	private final String reply;
	private final Map<String, Object> parameters;
	private final List<Context> contexts;
	private final String sessionId;

	public AgentResponse(String resolvedQuery, Action action, Map<String, Object> parameters,
			List<Context> contexts, String reply, String sessionId) {
		this.resolvedQuery = resolvedQuery;
		this.action = action;
		this.parameters = parameters;
		this.contexts = contexts;
		this.reply = reply;
		this.sessionId = sessionId;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public String getReply() {
		return reply;
	}

	public String getResolvedQuery() {
		return resolvedQuery;
	}

	public List<Context> getContexts() {
		return contexts;
	}

	public Action getAction() {
		return action;
	}

	public String getSessionId() {
		return sessionId;
	}
}
