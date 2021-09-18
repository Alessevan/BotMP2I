package fr.bakaaless.mp2i.bot.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public interface CommandExecutor {

    void run(final GuildMessageReceivedEvent event, final String command, final List<String> arguments);
}
