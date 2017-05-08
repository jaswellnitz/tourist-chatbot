package chatbot;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import spark.Request;
import spark.Response;
import spark.Route;
import util.PropertyLoader;

import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.request.SendMessage;

import model.User;

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
		
//		if(message.location() != null){
//			user.setCurrentLocation(message.location().latitude(),message.location().longitude());
//		}
		
		if (isStartMessage(message)) {
			answer = touristChatbot.processStartMessage(message.from().id(), message.from().firstName());
		} else {
			answer = touristChatbot.processInput(message.from().id(), message.text());
		}
	
		sendMessage(message.chat().id(), answer);

		return "ok";
	}

	protected void sendMessage(Long chatId, String message) {
		telegramBot.execute(new SendMessage(chatId, message));
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