package fr.bakaaless.mp2i.bot.listeners;

import fr.bakaaless.mp2i.bot.commands.CommandManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (CommandManager.messageReceived(event))
            return;
        final String commonLink = "https://(?:ptb.|canary.)?discord(?:app)?.com/channels/" + event.getGuild().getIdLong() + "/";
        final Pattern pattern = Pattern.compile(commonLink + "(\\d{18})/(\\d{18})");
        final Matcher matcher = pattern.matcher(event.getMessage().getContentDisplay());
        if (matcher.groupCount() == 0)
            return;
        final List<String> links = new ArrayList<>();
        while (matcher.find()) {
            links.add(matcher.group());
        }
        if (links.size() == 0)
            return;
        for (final String link : links) {
            final String[] ids = link.replaceAll(commonLink, "").split("/");
            final TextChannel channel = event.getGuild().getTextChannelById(ids[0]);
            if (channel == null)
                continue;
            channel.retrieveMessageById(ids[1]).queue(message -> {
                if  (message == null)
                    return;
                final EmbedBuilder embed = new EmbedBuilder().setAuthor("Message de " + (message.getMember() != null ? message.getMember().getEffectiveName() : message.getAuthor().getName()), message.getAuthor().getAvatarUrl(), message.getAuthor().getAvatarUrl());
                embed.setTimestamp(Instant.now()).setFooter("Message cité par " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getName()), event.getAuthor().getAvatarUrl());
                embed.setColor(Color.CYAN).setDescription(trimText(message.getContentRaw(), 1900) + "\n[(lien)](" + message.getJumpUrl() + ")");

                if (message.getAttachments().size() > 0) {
                    for (int index = 0; index < message.getAttachments().size(); index++) {
                        final Message.Attachment attachment = message.getAttachments().get(index);
                        embed.addField("Pièce jointe n°" + index, attachment.getProxyUrl(), false);
                    }
                }

                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            });
        }
    }

    private String trimText(final String message, final int length) {
        if (message.length() <= length)
            return message;
        final StringBuilder builder = new StringBuilder();
        for (int index = 0; index < length; index++) {
            builder.append(message.charAt(index));
        }
        return builder.toString();
    }

}
