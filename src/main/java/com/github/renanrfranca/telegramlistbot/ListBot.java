package com.github.renanrfranca.telegramlistbot;

import com.github.renanrfranca.telegramlistbot.jpa.Chat;
import com.github.renanrfranca.telegramlistbot.jpa.ChatRepository;
import com.github.renanrfranca.telegramlistbot.jpa.ItemList;
import com.github.renanrfranca.telegramlistbot.jpa.ListRepository;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.function.Predicate;

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
                .info("Add a new list to this chat")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> {
                    SendMessage sendMessage = new SendMessage(ctx.chatId().toString(), "Informe o título da lista");
                    sendMessage.setReplyToMessageId(ctx.update().getMessage().getMessageId());
                    sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, true));
                    silent.execute(sendMessage);
                })
                .reply(
                    upd -> {
                        addList(upd.getMessage().getText(), upd.getMessage().getChatId());
                        silent.send("Lista criada: " + upd.getMessage().getText(), upd.getMessage().getChatId());
                    },
                    Flag.MESSAGE,
                    Flag.REPLY,
                    isReplyToBot(),
                    isReplyToMessage("Informe o título da lista")
                )
                .build();
    }

    public void addList(String title, Long chatId) {
        Chat chat;
        Optional<Chat> optionalChat = chatRepository.findById(chatId);
        chat = optionalChat.orElse(chatRepository.save(new Chat(chatId)));

        ItemList itemList = new ItemList(chat, title);
        listRepository.save(itemList);
    }

    public Ability showLists() {
        return Ability.builder()
                .name("showlists")
                .info("Shows all lists in the current chat.")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> {
                    List<ItemList> itemLists = listRepository.findAllByChatId(ctx.chatId());
                    if (! itemLists.isEmpty()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("*Listas do chat:*\n");
                        itemLists.forEach((itemList) -> {
                            stringBuilder.append(itemList.getTitle()).append("\n");
                        });
                        silent.send(stringBuilder.toString(), ctx.chatId());
                    } else {
                        silent.send("Ainda não há listas nesse chat. Crie uma agora com o comando /addlist!", ctx.chatId());
                    }
                })
                .build();
    }

    public Ability addItem() {
        return Ability.builder()
                .name("addlist")
                .info("Add a new list to this chat")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> {
                    List<ItemList> itemLists = listRepository.findAllByChatId(ctx.chatId());
                    ReplyKeyboard listKeyboard = this.buildListKeyboard(itemLists);

                    SendMessage sendMessage = new SendMessage(
                            ctx.chatId().toString(),
                            "Selecione a lista a qual deseja adcionar itens."
                    );
                    sendMessage.setReplyMarkup(listKeyboard);

                    sendMessage.setReplyToMessageId(ctx.update().getMessage().getMessageId());
                    sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, true));
                    silent.execute(sendMessage);
                })
                .reply(
                        upd -> {
                            addList(upd.getMessage().getText(), upd.getMessage().getChatId());
                            silent.send("Lista criada: " + upd.getMessage().getText(), upd.getMessage().getChatId());
                        },
                        Flag.MESSAGE,
                        Flag.REPLY,
                        isReplyToBot(),
                        isReplyToMessage("Informe o título da lista")
                )
                .build();
    }

    private ReplyKeyboard buildListKeyboard(List<ItemList> itemlists) {
        var markupBuilder = InlineKeyboardMarkup.builder();

        itemlists.forEach(itemList -> {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(itemList.getTitle())
                    .callbackData(itemList.getId().toString())
                    .build();
            markupBuilder.keyboardRow(Collections.singletonList(button));
        });

        return markupBuilder.build();
    }

    public void addItem(String title, String description, Long chatId) {
        Chat chat;
        Optional<Chat> optionalChat = chatRepository.findById(chatId);
        chat = optionalChat.orElse(chatRepository.save(new Chat(chatId)));

        ItemList itemList = new ItemList(chat, title);
        listRepository.save(itemList);
    }

    private Predicate<Update> isReplyToMessage(String message) {
        return upd -> {
            Message reply = upd.getMessage().getReplyToMessage();
            return reply.hasText() && reply.getText().equalsIgnoreCase(message);
        };
    }

    private Predicate<Update> isReplyToBot() {
        return upd -> upd.getMessage().getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(getBotUsername());
    }

    @VisibleForTesting
    void setSender(MessageSender sender) {
        this.sender = sender;
    }
}
