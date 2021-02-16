package com.github.renanrfranca.telegramlistbot;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.MessageSender;

@Component
public class ListBot extends AbilityBot {
    public static final Logger logger = LoggerFactory.getLogger(ListBot.class);

    public static final String BOT_HANDLE = "@rrflistbot";
    public static final int MAX_ROLL = 999999;
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

    public Ability showHelp() {
        return Ability.builder()
                .name("help")
                .info("Display help text")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(0)
                .action(ctx -> silent.send("Help text", ctx.chatId()))
                .build();
    }

    public Ability showStart() {
        return Ability.builder()
                .name("start")
                .info("Display start text")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(0)
                .action(ctx -> silent.send("Help text", ctx.chatId()))
                .build();
    }

    public Ability roll() {
        return Ability.builder()
                .name("roll")
                .info("roll 6 digit dice")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(0)
                .action(ctx -> {
                    int random = (int) (Math.random() * MAX_ROLL) + 1;
                    String roll = String.format("%05d", random);
                    silent.send(
                            ctx.update().getMessage().getFrom().getFirstName() + " rollou " + roll,
                            ctx.chatId()
                    );
                })
                .build();
    }

    @VisibleForTesting
    void setSender(MessageSender sender) {
        this.sender = sender;
    }
}
