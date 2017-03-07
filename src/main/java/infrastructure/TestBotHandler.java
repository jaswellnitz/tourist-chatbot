package infrastructure;


import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import spark.Request;
import spark.Response;
import spark.Route;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.request.SendMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class TestBotHandler implements Route {

    private final String token;
    private final TelegramBot bot;

    public TestBotHandler(){
    	 Properties properties = new Properties();
         try {
			properties.load(new FileInputStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        token = properties.getProperty("token");
        System.out.println("TOKEN " + token);
        bot = TelegramBotAdapter.buildDebug(token);
    }
    
    @Override
    public Object handle(Request request, Response response) {
        Update update = BotUtils.parseUpdate(request.body());
        Message message = update.message();
        
        if (isStartMessage(message)) {
        	String firstName = update.message().from().firstName();
        	sendMessage(update.message().chat().id(), "Hello " + firstName + ". This is a test.");
        } else {
        	System.out.println(update.message());
        	String text = "Test Mode: You just said: " + update.message().caption();
        	sendMessage(update.message().chat().id(), text);
//            onWebhookUpdate(update);
        }
        return "ok";
    }

    private void sendMessage(Long chatId, String message){
    	bot.execute(new SendMessage(chatId, message));
    }

    private void onWebhookUpdate(Update update) {
    	System.out.println("onWebhookUpdate called");
    	System.out.println("user: " + update.message().from().username());
        bot.execute(new SendMessage(update.message().chat().id(), "TestBotHandler: method onWebhookUpdate."));
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