package chatbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import util.JsonUtil;

// according to https://docs.api.ai/docs/query
public class AgentResult {
	private final String source;
	private final String resolvedQuery;
	private final String action;
	private final String reply;
	private final double score;
	private final Map<String, String> parameters;
	private final List<Context> contexts;
	// private boolean actionIncomplete;
	// private Fulfillment fulfillment;
	// private AgentMetaData metaData;

	private AgentResult(String source, String resolvedQuery, String action, Map<String, String> parameters,
			List<Context> contexts, String reply, double score) {
		this.source = source;
		this.resolvedQuery = resolvedQuery;
		this.action = action;
		this.parameters = parameters;
		this.contexts = contexts;
		this.reply = reply;
		this.score = score;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getReply() {
		return reply;
	}

	public double getScore() {
		return score;
	}

	public String getSource() {
		return source;
	}

	public String getResolvedQuery() {
		return resolvedQuery;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((contexts == null) ? 0 : contexts.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((reply == null) ? 0 : reply.hashCode());
		result = prime * result + ((resolvedQuery == null) ? 0 : resolvedQuery.hashCode());
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentResult other = (AgentResult) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (contexts == null) {
			if (other.contexts != null)
				return false;
		} else if (!contexts.equals(other.contexts))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (reply == null) {
			if (other.reply != null)
				return false;
		} else if (!reply.equals(other.reply))
			return false;
		if (resolvedQuery == null) {
			if (other.resolvedQuery != null)
				return false;
		} else if (!resolvedQuery.equals(other.resolvedQuery))
			return false;
		if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	public static AgentResult fromJson(String jsonResponse) {
		JsonObject resultObject = JsonUtil.parseToJson(jsonResponse).get("result").getAsJsonObject();

		String source = resultObject.get("source").getAsString();
		String resolvedQuery = resultObject.get("resolvedQuery").getAsString();
		String action = resultObject.get("action").getAsString();
		String reply = resultObject.get("fulfillment").getAsJsonObject().get("speech").getAsString();
		double score = resultObject.get("score").getAsDouble();
		Map<String, String> parameters = new HashMap<>();
		if (resultObject.has("parameters")) {
			for (Entry<String, JsonElement> entry : resultObject.get("parameters").getAsJsonObject().entrySet()) {
				parameters.put(entry.getKey(), entry.getValue().getAsString());
			}
		}

		List<Context> contexts = new ArrayList<Context>();
		if (resultObject.has("contexts")) {
			JsonArray jsonArray = resultObject.get("contexts").getAsJsonArray();

			for (int i = 0; i < jsonArray.size(); i++) {
				JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
				String name = jsonObject.get("name").getAsString();
				Map<String, String> contextParameters = new HashMap<>();
				for (Entry<String, JsonElement> entry : jsonObject.get("parameters").getAsJsonObject().entrySet()) {
					contextParameters.put(entry.getKey(), entry.getValue().getAsString());
				}
				int lifespan = jsonObject.get("lifespan").getAsInt();
				contexts.add(new Context(name, contextParameters, lifespan));
			}
		}
		return new AgentResult(source, resolvedQuery, action, parameters, contexts, reply, score);
	}

	public List<Context> getContext() {
		return contexts;
	}

	public String getAction() {
		return action;
	}
	
	@Override
	public String toString(){
		return "source: "+source+", resolvedQuery: "+resolvedQuery +", action: " + action +", reply: " + reply;
	}
}
