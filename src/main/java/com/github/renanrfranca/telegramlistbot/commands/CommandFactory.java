package com.github.renanrfranca.telegramlistbot.commands;

import java.util.HashMap;

public class CommandFactory {
    private static final HashMap<String, Class<? extends Command>> commands;

    static
    {
        commands = new HashMap<>();

        commands.put("start", StartCommand.class);
        commands.put("help", StartCommand.class);
        commands.put("roll", RollCommand.class);
    }

    public static Command create(String commandText) throws IllegalAccessException, InstantiationException {
        return commands.get(commandText).newInstance();
    }
}
