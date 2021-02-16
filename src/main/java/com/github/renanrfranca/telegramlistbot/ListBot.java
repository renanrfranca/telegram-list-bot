package com.github.renanrfranca.telegramlistbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import java.util.regex.Pattern;

@Component
public class ListBot extends AbilityBot {
    public static final Logger logger = LoggerFactory.getLogger(ListBot.class);

    public static final String BOT_HANDLE = "@rrflistbot";
    public static final int MAX_ROLL =  999999;
    private static final String BOT_TONKEN = System.getenv("TELEGRAM_BOT_TOKEN");
    private static final String BOT_USERNAME = System.getenv("TELEGRAM_BOT_USERNAME");

    protected ListBot() {
        super(BOT_TONKEN, BOT_USERNAME);
    }

    @Override
    public String getBotToken() {
        return BOT_TONKEN;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public int creatorId() {
        return 1392938098;
    }

}
