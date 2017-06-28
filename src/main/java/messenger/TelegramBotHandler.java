package messenger;

import java.util.ArrayList;
import java.util.List;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;

import spark.Request;
import spark.Response;
import spark.Route;

import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;

import chatbot.ChatbotResponse;
import chatbot.TouristChatbot;

/**
 * Interface to the Telegram messenger. Receives updates from the messenger and handles the replies.
 * @author Jasmin Wellnitz
 *
 */
public class TelegramBotHandler implements Route {

	private final TelegramBot telegramBot;
	private TouristChatbot touristChatbot;

	/**
	 * Creates the TelegramBotHandler
	 * @param token the Telegram access token
	 * @param touristChatbot 
	 */
	public TelegramBotHandler(String token, TouristChatbot touristChatbot) {
		this.telegramBot = TelegramBotAdapter.buildDebug(token);
		this.touristChatbot = touristChatbot;
	}

	/**
	 * Receives messages from Telegram and forwards them to the TouristChatbot.
	 */
	@Override
	public Object handle(Request request, Response response) {
		Update update = BotUtils.parseUpdate(request.body());
		Message message = update.message();

		List<ChatbotResponse> chatbotResponses = new ArrayList<>();
		if (isStartMessage(message)) {
			chatbotResponses.add(touristChatbot.processStartMessage(message.from().id(), message.from().firstName()));
		} else {
			Object input = null;
			if (message.location() != null) {
				input = new domain.Location(message.location().latitude(), message.location().longitude());
				SendChatAction sendChatAction = new SendChatAction(message.chat().id(),
						ChatAction.find_location.name());
				telegramBot.execute(sendChatAction);
			} else if (message.text() != null) {
				input = message.text();
			}
			chatbotResponses.addAll(touristChatbot.processInput(message.from().id(), input));
		}

		boolean state = sendMessage(message.chat().id(), chatbotResponses);
		// TODO error handling

		return state;
	}

	/**
	 * Creates the Telegram keyboard
	 * @param keyboardText  The keyboard text for the buttons
	 * @return a Telegram keyboard
	 */
	private Keyboard createKeyboard(List<String> keyboardText) {
		KeyboardButton[] keyboardButtons = new KeyboardButton[keyboardText.size()];
		for (int i = 0; i < keyboardText.size(); i++) {
			KeyboardButton keyboardButton = new KeyboardButton(keyboardText.get(i));
			if (keyboardText.get(i).contains("Location")) {
				keyboardButton.requestLocation(true);
			}
			keyboardButtons[i] = keyboardButton;
		}
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardButtons).oneTimeKeyboard(true)
				.resizeKeyboard(true);
		return replyKeyboardMarkup;
	}

	/**
	 * Sends messages to the Telegram. Based on the element count in chatbotResponses, multiple messages are sent.
	 * @param chatId
	 * @param chatbotResponses the message content
	 * @return a boolean indicating whether the action was successful.
	 */
	private boolean sendMessage(long chatId, List<ChatbotResponse> chatbotResponses) {
		boolean isOk = false;
		for (ChatbotResponse chatbotResponse : chatbotResponses) {

			if (chatbotResponse.hasPhoto()) {
				SendPhoto sendPhoto = new SendPhoto(chatId, chatbotResponse.getReply());
				SendResponse execute = telegramBot.execute(sendPhoto);
				isOk = execute.isOk();
			} else {
				SendMessage sendMessage = new SendMessage(chatId, chatbotResponse.getReply());
				Keyboard keyboard;
				if (chatbotResponse.hasChangedKeyboard()) {
					keyboard = createKeyboard(chatbotResponse.getKeyboardButtons());
				} else {
					keyboard = new ReplyKeyboardRemove();
				}
				sendMessage.replyMarkup(keyboard);
				SendResponse execute = telegramBot.execute(sendMessage);
				isOk = execute.isOk();
			}
		}

		return isOk;
	}

	/**
	 * Returns the TelegramBot
	 * @return telegramBot
	 */
	public TelegramBot getTelegramBot() {
		return telegramBot;
	}

	/**
	 * Indicates whether the user input is a start message.
	 * @param message a Telegram message
	 * @return boolean
	 */
	protected boolean isStartMessage(Message message) {
		return message != null && message.text() != null && message.text().startsWith("/start");
	}
}