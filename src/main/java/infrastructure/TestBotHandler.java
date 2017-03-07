package infrastructure;

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

public class TestBotHandler implements Route {

	private final String token;
	private final TelegramBot bot;
	private final AgentConnector agentConnector;

	public TestBotHandler() {
		token = PropertyLoader.getProperty("telegramToken");
		bot = TelegramBotAdapter.buildDebug(token);
		agentConnector =  new AgentConnector();
	}

	@Override
	public Object handle(Request request, Response response) {
		Update update = BotUtils.parseUpdate(request.body());
		Message message = update.message();

		if (isStartMessage(message)) {
			String firstName = update.message().from().firstName();
			sendMessage(update.message().chat().id(), "Testbot: Hello " + firstName + ". Your id: " + message.from().id());
		} else {
			System.out.println(message);
			String text = "";
			if(message.text() != null){
				String agentAnswer = agentConnector.sendQuery(message.text());
				text += "Agent answers: " + agentAnswer + " "; 
			}
			Location location = message.location();
			if (location != null) {
				float lat = location.latitude();
				float lon = location.longitude();
				text += "TestBot: Your location - latitude " + lat + ", longitude " + lon;
			}
			sendMessage(message.chat().id(), text);
		}
		return "ok";
	}

	private void sendMessage(Long chatId, String message) {
		bot.execute(new SendMessage(chatId, message));
	}

	public String getToken() {
		return token;
	}

	public TelegramBot getBot() {
		return bot;
	}

	private boolean isStartMessage(Message message) {
		return message != null && message.text() != null && message.text().startsWith("/start");
	}
}