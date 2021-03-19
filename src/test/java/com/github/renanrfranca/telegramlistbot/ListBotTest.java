package com.github.renanrfranca.telegramlistbot;

import com.github.renanrfranca.telegramlistbot.jpa.Item;
import com.github.renanrfranca.telegramlistbot.jpa.ItemList;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.IOException;
import java.util.List;

@DataJpaTest
@SpringBootTest
class ListBotTest {
    public static final int USER_ID = 1337;
    public static final Long CHAT_ID = 1337L;

    private ListBot bot;
    private User user;
    private final DBContext dbContext = MapDBContext.offlineInstance("test");

    @BeforeEach
    public void setUp() {
        bot = new ListBot(dbContext);
        bot.onRegister();
        MessageSender sender = mock(MessageSender.class);
        bot.setSender(sender);
        SilentSender silentSender = mock(SilentSender.class);
        bot.setSilentSender(silentSender);
        user = new User(USER_ID, "João", false, "Doe", "@joaodoe", null, null, null, null);
    }

    @Test
    void showHelp() {
        Update update = new Update();
        MessageContext context = MessageContext.newContext(update, user, CHAT_ID, bot);
        bot.showHelp().action().accept(context);
        verify(bot.silent(), times(1)).send(ListBot.HELP_TEXT, CHAT_ID);
    }

    @Test
    void showStart() {
        Update update = new Update();
        MessageContext context = MessageContext.newContext(update, user, CHAT_ID, bot);
        bot.showStart().action().accept(context);
        verify(bot.silent(), times(1)).send(ListBot.HELP_TEXT, CHAT_ID);
    }

    @Test
    void getFormattedList() {
        ItemList mockList = mock(ItemList.class);

        when(mockList.getTitle()).thenReturn("Lista 1");

        when(mockList.getItems()).thenReturn(List.of(
            new Item("Item 1", mockList),
            new Item("Item 2", mockList),
            new Item("Item 3", mockList)
        ));

        assertThat(bot.getFormattedList(mockList)).isEqualTo("<b>Lista 1</b>\n- Item 1\n- Item 2\n- Item 3");
    }

    @Test
    void addList() {
        Update update = mock(Update.class, RETURNS_DEEP_STUBS);

        when(update.getMessage().hasText()).thenReturn(true);
        when(update.getMessage().getText()).thenReturn("Lista 1");
        when(update.getMessage().getChatId()).thenReturn(CHAT_ID);
        when(update.getMessage().getChat().isUserChat()).thenReturn(false);
        when(update.getMessage().getReplyToMessage().getFrom().getUserName()).thenReturn(bot.getBotUsername());
        when(update.getMessage().getReplyToMessage().hasText()).thenReturn(true);
        when(update.getMessage().getReplyToMessage().getText()).thenReturn(ListBot.ASK_FOR_LIST_TITLE);



        ItemList lista1 = bot.addList("Lista 1", CHAT_ID);
        ItemList lista2 = bot.addList("Lista 2", CHAT_ID);

        assertThat(lista1).as("lista 1 retornada pelo addList").isNotNull();
        assertThat(lista2).as("Lista 2 retornada pelo addList").isNotNull();

        assertThat(bot.listRepository.findById(lista1.getId()).get())
            .as("Lista 1 presente no banco de dados")
            .isEqualTo(lista1);
        assertThat(bot.listRepository.findById(lista2.getId()).get())
            .as("Lista 2 presente no banco de dados")
            .isEqualTo(lista2);
    }

    @AfterEach
    void tearDown() throws IOException {
        dbContext.clear();
        dbContext.close();
    }
}