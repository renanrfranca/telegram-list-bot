package com.github.renanrfranca.telegramlistbot;

import com.github.renanrfranca.telegramlistbot.jpa.Item;
import com.github.renanrfranca.telegramlistbot.jpa.ItemList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.IOException;
import java.util.List;

class ListBotTest {
    public static final int USER_ID = 1337;
    public static final Long CHAT_ID = 1337L;

    private ListBot bot;
    private MessageSender sender;
    private SilentSender silentSender;
    private User user;
    private DBContext dbContext = MapDBContext.offlineInstance("test");

    @BeforeEach
    public void setUp() {
        bot = new ListBot(dbContext);
        bot.onRegister();
        sender = Mockito.mock(MessageSender.class);
        bot.setSender(sender);
        silentSender = Mockito.mock(SilentSender.class);
        bot.setSilentSender(silentSender);
        user = new User(USER_ID, "João", false, "Doe", "@joaodoe", null, null, null, null);
    }

    @Test
    void showHelp() {
        Update update = new Update();
        MessageContext context = MessageContext.newContext(update, user, CHAT_ID, bot);
        bot.showHelp().action().accept(context);
        Mockito.verify(bot.silent(), Mockito.times(1)).send(ListBot.HELP_TEXT, CHAT_ID);
    }

    @Test
    void showStart() {
        Update update = new Update();
        MessageContext context = MessageContext.newContext(update, user, CHAT_ID, bot);
        bot.showStart().action().accept(context);
        Mockito.verify(bot.silent(), Mockito.times(1)).send(ListBot.HELP_TEXT, CHAT_ID);
    }

    @Test
    void getFormattedList() {
        ItemList mockList = Mockito.mock(ItemList.class);

        Mockito.when(mockList.getTitle()).thenReturn("Lista 1");

        Mockito.when(mockList.getItems()).thenReturn(List.of(
            new Item("Item 1", mockList),
            new Item("Item 2", mockList),
            new Item("Item 3", mockList)
        ));

        Assertions.assertEquals(
            "<b>Lista 1</b>\n- Item 1\n- Item 2\n- Item 3",
            bot.getFormattedList(mockList)
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        dbContext.clear();
        dbContext.close();
    }
}