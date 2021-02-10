package com.github.renanrfranca.telegramlistbot;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class ListBot extends TelegramLongPollingBot {
    public static final String BOT_HANDLE =  "@rrflistbot";

    public static final String START =  "/start";
    public static final String HELP =  "/help";
    public static final String NEW_LIST =  "/new_list";
    public static final String NEW_ITEM =  "/new_item";
    public static final String ROLL =  "/roll";
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
            Message receivedMessage = update.getMessage();
            logger.info(receivedMessage.toString());

            String command = receivedMessage.getText().replace(BOT_HANDLE, "");

            switch (command) {
                case START:
                case HELP:
                    handleHelp(receivedMessage);
                    break;
                case NEW_LIST:
                    handleNewList(receivedMessage);
                    break;
                case NEW_ITEM:
                    handleNewItem(receivedMessage);
                    break;
                case ROLL:
                    handleRoll(receivedMessage);
                    break;
            }
        }
    }

    private void handleHelp (Message message) {
            SendMessage response = new SendMessage();
            response.setChatId(message.getChatId().toString());
            response.setText(
                    "Bem vindo ao listbot!" +
                            "\nPara criar uma nova lista, digite " + NEW_LIST +
                            "\nPara adicionar um item a uma lista, digite " + NEW_ITEM
            );
            try {
                execute(response);
                logger.info("Help reply sent");
            } catch (TelegramApiException e) {
                logger.error("Failed to send message due to error: {}", e.getMessage());
            }
    }

    private void handleNewList(Message message) {

    }

    private void handleNewItem(Message message) {

    }

    private void handleRoll(Message message) {
        int random = (int) (Math.random() * MAX_ROLL) + 1;
        String roll = StringUtils.leftPad(String.valueOf(random), 6);

        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId().toString());
        response.setReplyToMessageId(message.getMessageId());
        response.setText(message.getFrom().getFirstName() + " rollou " + roll);
        try {
            execute(response);
            logger.info("Help reply sent");
        } catch (TelegramApiException e) {
            logger.error("Failed to send message due to error: {}", e.getMessage());
        }
    }
}
