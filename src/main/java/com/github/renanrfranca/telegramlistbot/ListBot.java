package com.github.renanrfranca.telegramlistbot;

import com.github.renanrfranca.telegramlistbot.commands.Command;
import com.github.renanrfranca.telegramlistbot.commands.CommandFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ListBot extends TelegramLongPollingBot {
    public static final String BOT_HANDLE = "@rrflistbot";

    public static final Pattern COMMAND_PATTERN = Pattern.compile("/(\\w+)");
    public static final int MAX_ROLL =  999999;

    private static final Logger logger = LoggerFactory.getLogger(ListBot.class);

    private final String token = System.getenv("TELEGRAM_BOT_TOKEN");
    private final String username = System.getenv("TELEGRAM_BOT_USERNAME");

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String text = message.getText();

            if (hasCommand(text)) {
                Command command;
                try {
                    command = CommandFactory.create(getCommandFromText(text));
                    logger.info("Executando comando:" + text);
                    command.handle(message);
                } catch (IllegalAccessException | InstantiationException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    private boolean hasCommand(String text) {
        Matcher m = COMMAND_PATTERN.matcher(text);
        return m.find();
    }

    private String getCommandFromText(String text) {
        Matcher m = COMMAND_PATTERN.matcher(text);
        return m.group(1);
    }
}
