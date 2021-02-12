package com.github.renanrfranca.telegramlistbot.commands;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface Command {
    public void handle(Message message);
}
