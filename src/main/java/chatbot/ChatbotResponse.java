package chatbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatbotResponse {

	private final String reply;
	private final List<String> keyboardButtons;

	public ChatbotResponse(String reply, String... keyboardButtons) {
		this.reply = reply;
		if (keyboardButtons != null) {
			this.keyboardButtons = Arrays.asList(keyboardButtons);
		} else {
			this.keyboardButtons = new ArrayList<>();
		}
	}

	public String getReply() {
		return reply;
	}

	public boolean hasChangedKeyboard() {
		return !keyboardButtons.isEmpty();
	}

	public List<String> getKeyboardButtons() {
		return keyboardButtons;
	}
	
	public boolean hasPhoto(){
		return reply.startsWith("https://") && reply.endsWith(".jpg");
	}
}
