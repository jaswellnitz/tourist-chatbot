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

import java.io.IOException;


public class TestBotHandler implements Route {

    private final String token;
    private final TelegramBot bot;

    public TestBotHandler(){
        token = "300250866:AAG7hoV0LLtuNPwQAu-7vJPYU_XJ1Znitqk";
        bot = TelegramBotAdapter.buildDebug(token);
    }
    
    @Override
    public Object handle(Request request, Response response) {
        Update update = BotUtils.parseUpdate(request.body());
        Message message = update.message();

        if (isStartMessage(message) && onStart(message)) {
            return "ok";
        } else {
            onWebhookUpdate(update);
        }
        return "ok";
    }


    private void onWebhookUpdate(Update update) {
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

    protected boolean onStart(Message message) {
        return false;
    }
}