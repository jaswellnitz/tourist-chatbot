package chatbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A container that bundles the information to be transmitted back to the messenger
 * @author Jasmin Wellnitz
 *
 */
public class ChatbotResponse {

	private final String reply;
	private final List<String> keyboardButtons;

	/**
	 * Creates a chatbot response, containing the answer and, if available, keyboard buttons to be introduced
	 * @param reply
	 * @param keyboardButtons define whether and how many keyboard buttons are set in the messenger
	 */
	public ChatbotResponse(String reply, String... keyboardButtons) {
		this.reply = reply;
		if (keyboardButtons != null) {
			this.keyboardButtons = Arrays.asList(keyboardButtons);
		} else {
			this.keyboardButtons = new ArrayList<>();
		}
	}

	/**
	 * Returns the chatbot's reply.
	 * @return reply
	 */
	public String getReply() {
		return reply;
	}

	/**
	 * Indicates whether the keyboard is changed in this answer.
	 * @return boolean
	 */
	public boolean hasChangedKeyboard() {
		return !keyboardButtons.isEmpty();
	}

	/**
	 * Returns the keyboard buttons
	 * @return keyboard buttons
	 */
	public List<String> getKeyboardButtons() {
		return keyboardButtons;
	}
	
	/**
	 * Shows if the chatbot answer contains a image url
	 * @return boolean
	 */
	public boolean hasPhoto(){
		return reply.startsWith("https://") && reply.endsWith(".jpg");
	}
}
