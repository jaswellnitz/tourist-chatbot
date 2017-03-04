package infrastructure;

// https://docs.api.ai/docs/webhook

public class Fulfillment {
	private String speech;
	private String source;
	private String displayText;
	private Object data;
	private Object contextOut[];

	public Fulfillment(String speech, String displayText, String source) {
		super();
		this.speech = speech;
		this.source = source;
		this.displayText = displayText;
		this.data = "";
		this.contextOut = new Object[1];
		
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