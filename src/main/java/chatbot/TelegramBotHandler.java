package chatbot;

import java.util.List;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

import spark.Request;
import spark.Response;
import spark.Route;

import com.pengrad.telegrambot.TelegramBotAdapter;
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
		String answer = "";

		// if(message.location() != null){
		// user.setCurrentLocation(message.location().latitude(),message.location().longitude());
		// }
		ChatbotResponse chatbotResponse;
		if (isStartMessage(message)) {
			chatbotResponse = touristChatbot.processStartMessage(message.from().id(), message.from().firstName());
		} else {
			chatbotResponse = touristChatbot.processInput(message.from().id(), message.text());
		}

		boolean isOk = sendMessage(message.chat().id(), chatbotResponse);

		return isOk;
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
		return new ReplyKeyboardMarkup(keyboardButtons);
	}

	private boolean sendMessage(long chatId, ChatbotResponse chatbotResponse) {
		SendMessage sendMessage = new SendMessage(chatId, chatbotResponse.getReply());
		if (chatbotResponse.changeKeyboard()) {
			Keyboard keyboard = getKeyboard(chatbotResponse.getKeyboardButtons());
			sendMessage.replyMarkup(keyboard);
		}
		SendResponse execute = telegramBot.execute(sendMessage);
		return execute.isOk();
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