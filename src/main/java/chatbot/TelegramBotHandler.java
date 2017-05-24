package chatbot;

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
import com.pengrad.telegrambot.response.SendResponse;

// Receives Updates from Telegram and passes text messages to api.ai
public class TelegramBotHandler implements Route {

	private final String token;
	private final TelegramBot telegramBot;
	private TouristChatbot touristChatbot;

	public TelegramBotHandler(String token, TouristChatbot touristChatbot) {
		this.telegramBot = TelegramBotAdapter.buildDebug(token);
		this.token = token;
		this.touristChatbot = touristChatbot;
	}

	@Override
	public Object handle(Request request, Response response) {
		Update update = BotUtils.parseUpdate(request.body());
		Message message = update.message();

		List<ChatbotResponse> chatbotResponses = new ArrayList<>();
		if (isStartMessage(message)) {
			chatbotResponses.add(touristChatbot.processStartMessage(message.from().id(), message.from().firstName()));
		} else {
			Object input = null;
			;
			if (message.location() != null) {
				input = new model.Location(message.location().latitude(), message.location().longitude());
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

	private Keyboard getKeyboard(List<String> keyboardText) {
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

	private boolean sendMessage(long chatId, List<ChatbotResponse> chatbotResponses) {
		boolean isOk = false;
		for (ChatbotResponse chatbotResponse : chatbotResponses) {
			SendMessage sendMessage = new SendMessage(chatId, chatbotResponse.getReply());
			Keyboard keyboard;
			if (chatbotResponse.changeKeyboard()) {
				keyboard = getKeyboard(chatbotResponse.getKeyboardButtons());
			} else {
				keyboard = new ReplyKeyboardRemove();
			}
			sendMessage.replyMarkup(keyboard);
			SendResponse execute = telegramBot.execute(sendMessage);
			isOk = execute.isOk();
		}

		return isOk;
	}

	public String getToken() {
		return token;
	}

	public TelegramBot getTelegramBot() {
		return telegramBot;
	}

	protected boolean isStartMessage(Message message) {
		return message != null && message.text() != null && message.text().startsWith("/start");
	}
}