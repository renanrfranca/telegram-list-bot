package com.github.renanrfranca.telegramlistbot;

import com.github.renanrfranca.telegramlistbot.jpa.Chat;
import com.github.renanrfranca.telegramlistbot.jpa.ChatRepository;
import com.github.renanrfranca.telegramlistbot.jpa.List;
import com.github.renanrfranca.telegramlistbot.jpa.ListRepository;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;
import java.util.Random;

@Component
public class ListBot extends AbilityBot {
    public static final Logger logger = LoggerFactory.getLogger(ListBot.class);

    public static final String BOT_HANDLE = "@rrflistbot";
    private final Random random = new Random();
    public static final int MAX_ROLL = 999999;
    private static final String BOT_TOKEN = System.getenv("TELEGRAM_BOT_TOKEN");
    private static final String BOT_USERNAME = System.getenv("TELEGRAM_BOT_USERNAME");

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private ChatRepository chatRepository;

    protected ListBot() {
        super(BOT_TOKEN, BOT_USERNAME);
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
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
                    int random = this.random.nextInt(MAX_ROLL);
                    String roll = String.format("%06d", random);
                    String text = ctx.update().getMessage().getFrom().getFirstName() + " rollou " + roll;
                    SendMessage sendMessage = new SendMessage(ctx.chatId().toString(), text);
                    sendMessage.setReplyToMessageId(ctx.update().getMessage().getMessageId());
                    silent.execute(sendMessage);
                })
                .build();
    }

    public Ability addList() {
        return Ability.builder()
                .name("addlist")
                .info("add a new list to this chat: /addlist \"listname\"")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(2)
                .action(ctx -> addList(
                        ctx.firstArg(),
                        ctx.secondArg(),
                        ctx.chatId()
                ))
                .build();
    }

    public void addList(String title, String description, Long chatId) {
        Chat chat;
        Optional<Chat> optionalChat = chatRepository.findById(chatId);
        chat = optionalChat.orElse(chatRepository.save(new Chat(chatId)));

        List list = new List(chat, title, description);
        listRepository.save(list);
    }

    @VisibleForTesting
    void setSender(MessageSender sender) {
        this.sender = sender;
    }
}
