package com.github.renanrfranca.telegramlistbot.commands;

import com.github.renanrfranca.telegramlistbot.ListBot;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class RollCommand implements Command {
    private static final int MAX_ROLL = 999999;

    @Autowired
    private ListBot listBot;

    @Override
    public void handle(Message message) {
        int random = (int) (Math.random() * MAX_ROLL) + 1;
        String roll = StringUtils.leftPad(String.valueOf(random), 6);

        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId().toString());
        response.setReplyToMessageId(message.getMessageId());
        response.setText(message.getFrom().getFirstName() + " rollou " + roll);
        try {
            ListBot.logger.info("Sending: {}", response.getText());
            listBot.execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
