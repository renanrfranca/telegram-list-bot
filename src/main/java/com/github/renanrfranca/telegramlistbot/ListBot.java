package com.github.renanrfranca.telegramlistbot;

import com.github.renanrfranca.telegramlistbot.jpa.*;
import com.github.renanrfranca.telegramlistbot.lib.CustomReplyKeyboardMarkup;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import javax.persistence.EntityManager;
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

    protected static final String HELP_TEXT = "Help text";
    protected static final String ASK_FOR_LIST_TITLE = "Informe o título da lista:";
    protected static final String NO_LISTS_WARNING = "Ainda não há listas nesta conversa. Você pode criar uma utilizando o comando /addlist!";
    protected static final String SELECT_LIST_FOR_NEW_ITEM = "Escolha a lista em qual deseja adcionar um novo item:";
    protected static final String ASK_FOR_ITEM_TEXT = "Informe novo item pra lista #";
    protected static final String SELECT_LIST_TO_SHOW = "Selecione a lista a ser exibida";


    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private ItemRepository itemRepository;

    public ListBot() {
        super(BOT_TOKEN, BOT_USERNAME);
    }

    public ListBot(DBContext dbContext) {
        super(BOT_TOKEN, BOT_USERNAME, dbContext);
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
                .action(ctx -> silent.send(HELP_TEXT, ctx.chatId()))
                .build();
    }

    public Ability showStart() {
        return Ability.builder()
                .name("start")
                .info("Display start text")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(0)
                .action(ctx -> silent.send(HELP_TEXT, ctx.chatId()))
                .build();
    }

    public Ability roll() {
        return Ability.builder()
                .name("roll")
                .info("Roll 6 digit dice")
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
                    SendMessage sendMessage = new SendMessage(ctx.chatId().toString(), ASK_FOR_LIST_TITLE);
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
                    isReplyToMessage(ASK_FOR_LIST_TITLE)
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

    public Ability addItem() {
        return Ability.builder()
            .name("additem")
            .info("Add a new list to this chat")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action(ctx -> {
                List<ItemList> itemLists = listRepository.findAllByChatId(ctx.chatId());

                if (itemLists.isEmpty()) {
                    silent.send(NO_LISTS_WARNING, ctx.chatId());
                    return;
                }

                ReplyKeyboard listKeyboard = this.buildListKeyboard(itemLists);

                SendMessage sendMessage = new SendMessage(
                        ctx.chatId().toString(),
                        SELECT_LIST_FOR_NEW_ITEM
                );
                sendMessage.setReplyMarkup(listKeyboard);

                sendMessage.setReplyToMessageId(ctx.update().getMessage().getMessageId());
                silent.execute(sendMessage);
            })
            .reply(
                upd -> {
                    String[] split = upd.getMessage().getText().split(" #");
                    if (split.length < 2 || ! StringUtils.isNumeric(split[split.length - 1])) {
                        return;
                    }
                    SendMessage sendMessage = new SendMessage(upd.getMessage().getChatId().toString(), ASK_FOR_ITEM_TEXT + split[split.length - 1]);
                    sendMessage.setReplyToMessageId(upd.getMessage().getMessageId());
                    sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, true));
                    silent.execute(sendMessage);
                },
                isReplyToBot(),
                isReplyToMessage(SELECT_LIST_FOR_NEW_ITEM)
            )
            .reply(
                upd -> {
                    String itemText = upd.getMessage().getText();
                    String[] split = upd.getMessage().getReplyToMessage().getText().split(" #");
                    Long listId = Long.valueOf(split[split.length - 1]);
                    Optional<ItemList> itemList = listRepository.findById(listId);
                    if (itemList.isPresent()) {
                        if (itemList.get().getChat().getId().equals(upd.getMessage().getChatId())) {
                            addItem(itemText, itemList.get());
                            itemList = listRepository.findById(listId);
                            SendMessage message = new SendMessage(upd.getMessage().getChatId().toString(), getFormattedList(itemList.get()));
                            message.setParseMode(ParseMode.HTML);
                            silent.execute(message);
                        }
                    }
                },
                isReplyToBot(),
                isNewItem()
            )
            .build();
    }

    public Ability showList() {
        return Ability.builder()
            .name("showlist")
            .info("Displays all itens in a chosen list")
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action(ctx ->{
                List<ItemList> itemLists = listRepository.findAllByChatId(ctx.chatId());

                if (itemLists.isEmpty()) {
                    silent.send(NO_LISTS_WARNING, ctx.chatId());
                    return;
                }

                SendMessage sendMessage = new SendMessage(
                        ctx.chatId().toString(),
                        SELECT_LIST_TO_SHOW
                );
                sendMessage.setReplyMarkup(this.buildListKeyboard(itemLists));

                sendMessage.setReplyToMessageId(ctx.update().getMessage().getMessageId());
                silent.execute(sendMessage);
            })
            .reply(
                upd -> {
                    String[] split = upd.getMessage().getText().split(" #");
                    if (split.length < 2 || ! StringUtils.isNumeric(split[split.length - 1])) {
                        return;
                    }

                    Long listId = Long.valueOf(split[split.length - 1]);
                    Optional<ItemList> optionalItemList = listRepository.findById(listId);

                    if (
                        optionalItemList.isPresent()
                        && optionalItemList.get().getChat().getId().equals(upd.getMessage().getChatId())
                    ) {
                        ItemList itemList = optionalItemList.get();
                        SendMessage message = new SendMessage(upd.getMessage().getChatId().toString(), getFormattedList(itemList));
                        message.setParseMode(ParseMode.HTML);
                        silent.execute(message);
                    }
                },
                isReplyToBot(),
                isReplyToMessage(SELECT_LIST_TO_SHOW)
            )
            .build();
    }

    protected String getFormattedList(ItemList itemList) {
        List<Item> items = itemList.getItems();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>").append(itemList.getTitle()).append("</b>\n");
        items.forEach((item) -> {
            stringBuilder.append("- ").append(item.getDescription()).append("\n");
        });
        return stringBuilder.toString();
    }

    private ReplyKeyboard buildListKeyboard(List<ItemList> itemLists) {
        var markupBuilder = CustomReplyKeyboardMarkup.getBuilder();

        KeyboardRow row = new KeyboardRow();
        for (ItemList itemList : itemLists) {
            row.add(itemList.getTitle() + " #" + itemList.getId().toString());

            if (row.size() == 2) {
                markupBuilder.keyboardRow(row);
                row = new KeyboardRow();
            }
        }
        if (! row.isEmpty()) {
            markupBuilder.keyboardRow(row);
        }

        return markupBuilder.selective(true).oneTimeKeyboard(true).forceReply(true).build();
    }

    public void addItem(String text, ItemList list) {
        Item item = new Item(text, list);
        itemRepository.save(item);
    }

    private Predicate<Update> isReplyToMessage(String message) {
        return upd -> {
            Message reply = upd.getMessage().getReplyToMessage();
            return reply.hasText() && reply.getText().equalsIgnoreCase(message);
        };
    }

    private Predicate<Update> isReplyToBot() {
        return upd -> upd.getMessage().getChat().isUserChat()
            || upd.getMessage().getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(getBotUsername());
    }

    private Predicate<Update> isNewItem() {
        return update -> update.getMessage().getReplyToMessage().getText()
                .startsWith(ASK_FOR_ITEM_TEXT);
    }

    @VisibleForTesting
    void setSender(MessageSender sender) {
        this.sender = sender;
    }

    @VisibleForTesting
    void setSilentSender(SilentSender siletSender) {
        this.silent = siletSender;
    }
}
