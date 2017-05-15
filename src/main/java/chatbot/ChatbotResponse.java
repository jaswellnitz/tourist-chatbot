package chatbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatbotResponse {

	private final String reply;
	private final List<String> keyboardButtons;
	private boolean triggerRecommendation;

	public ChatbotResponse(String reply, String... keyboardButtons) {
		this.reply = reply;
		if (keyboardButtons != null) {
			this.keyboardButtons = Arrays.asList(keyboardButtons);
		} else {
			this.keyboardButtons = new ArrayList<>();
		}
		triggerRecommendation = false;
	}

	public String getReply() {
		return reply;
	}

	public boolean changeKeyboard() {
		return !keyboardButtons.isEmpty();
	}

	public List<String> getKeyboardButtons() {
		return keyboardButtons;
	}

	public boolean triggerRecommendation() {
		return triggerRecommendation;
	}

	public void setTriggerRecommendation(boolean trigger) {
		triggerRecommendation = trigger;
	}
}
