package infrastructure;

// https://docs.api.ai/docs/webhook

public class AgentResponse {
	private String speech;
	private String displayText;
	private String source;

	public AgentResponse(String speech, String displayText, String source) {
		super();
		this.speech = speech;
		this.source = source;
		this.displayText = displayText;
	}
	
	public String getSpeech() {
		return speech;
	}

	public void setSpeech(String speech) {
		this.speech = speech;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}
}