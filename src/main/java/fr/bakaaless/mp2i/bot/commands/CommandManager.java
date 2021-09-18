package fr.bakaaless.mp2i.bot.commands;

import fr.bakaaless.mp2i.starter.Config;
import fr.bakaaless.mp2i.starter.Starter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CommandManager {

    private static final Map<String, CommandExecutor> EXECUTORS = new HashMap<>();

    public static boolean messageReceived(final @NotNull GuildMessageReceivedEvent event) {

        if (!event.getMessage().getContentDisplay().startsWith(Config.get().getPrefix()))
            return false;
        if (event.getAuthor().isBot())
            return true;
        if (event.getMessage().getContentDisplay().isBlank() || event.getMessage().getContentDisplay().isEmpty())
            return true;
        final String[] message = event.getMessage().getContentDisplay().split(" ");
        final String command = message[0].substring(1);
        Starter.getLogger().log(Level.INFO,
                event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ") performed command in channel " + event.getChannel().getName() + "(" + event.getChannel().getId() + ")" + " : " + event.getMessage().getContentDisplay()
        );
        final List<String> arguments = new ArrayList<>();
        if (message.length > 1) {
            for (final String part : message) {
                if ((Config.get().getPrefix() + command).equals(part))
                    continue;
                arguments.add(part);
            }
        }
        for (final Map.Entry<String, CommandExecutor> entry : EXECUTORS.entrySet()) {
            if (command.equals(entry.getKey())) {
                entry.getValue().run(event, command, arguments);
                return true;
            }
        }
        event.getChannel().sendMessage(":warning: Cette commande n'existe pas.").queue(deleteMessage -> {
            try {
                deleteMessage.delete().queueAfter(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
        });
        return true;
    }

    public static void register(final String command, final CommandExecutor executor) {
        EXECUTORS.putIfAbsent(command, executor);
    }

}
