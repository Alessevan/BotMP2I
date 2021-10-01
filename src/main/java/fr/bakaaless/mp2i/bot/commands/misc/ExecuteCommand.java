package fr.bakaaless.mp2i.bot.commands.misc;

import fr.bakaaless.mp2i.bot.commands.CommandExecutor;
import fr.bakaaless.mp2i.starter.Starter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ExecuteCommand implements CommandExecutor {

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        final Optional<Message> code = Optional.ofNullable(event.getMessage().getReferencedMessage());
        final StringBuilder args = new StringBuilder();
        for (final String part : arguments) {
            args.append(" ").append(part);
        }
        code.ifPresent(message -> {
            if (message.getContentRaw().toLowerCase().startsWith("```python")) {
                final File executeCode = new File("tempo", "tempopython_" + message.getAuthor().getId() + "_" + System.currentTimeMillis() + ".py");
                try {
                    executeCode.getParentFile().mkdirs();
                    executeCode.createNewFile();
                    FileUtils.writeByteArrayToFile(executeCode, message.getContentStripped().getBytes(StandardCharsets.UTF_8));
                    final Process pythonProcess = Runtime.getRuntime().exec("python3 " + executeCode.getPath() + args);
                    printErrorCommand(event, pythonProcess);
                    printResultCommand(event, pythonProcess);
                    new Thread(() -> {
                        deleteCacheFiles(executeCode, pythonProcess);
                    }).start();
                } catch (IOException exception) {
                    event.getChannel().sendMessage(exception.toString()).queue();
                    exception.printStackTrace();
                }
            } else if (message.getContentRaw().toLowerCase().startsWith("```c")) {
                final String fileName = "tempoc_" + message.getAuthor().getId() + "_" + System.currentTimeMillis();
                final File executeCode = new File("tempo", fileName + ".c");
                try {
                    executeCode.getParentFile().mkdirs();
                    executeCode.createNewFile();
                    FileUtils.writeByteArrayToFile(executeCode, message.getContentStripped().getBytes(StandardCharsets.UTF_8));
                    final Process cProcess = Runtime.getRuntime().exec("gcc " + executeCode.getPath() + " -o " + fileName + ".h");
                    printErrorCommand(event, cProcess);
                    printResultCommand(event, cProcess);
                    new Thread(() -> {
                        deleteCacheFiles(executeCode, cProcess);
                        try {
                            final Process hProcess = Runtime.getRuntime().exec("./" + fileName + ".h" + args);
                            printErrorCommand(event, hProcess);
                            printResultCommand(event, hProcess);
                            new Thread(() -> {
                                final File hFile = new File(fileName + ".h");
                                deleteCacheFiles(hFile, hProcess);

                            }).start();
                        } catch (IOException e) {
                            event.getChannel().sendMessage(e.toString()).queue();
                            e.printStackTrace();
                        }
                    }).start();
                } catch (IOException exception) {
                    event.getChannel().sendMessage(exception.toString()).queue();
                    exception.printStackTrace();
                }
            } else if (message.getContentRaw().toLowerCase().startsWith("```ocaml")) {
                final String fileName = "tempoocaml_" + message.getAuthor().getId() + "_" + System.currentTimeMillis();
                final File executeCode = new File("tempo", fileName + ".ml");
                try {
                    executeCode.getParentFile().mkdirs();
                    executeCode.createNewFile();
                    FileUtils.writeByteArrayToFile(executeCode, message.getContentStripped().getBytes(StandardCharsets.UTF_8));
                    final Process ocamlProcess = Runtime.getRuntime().exec("ocaml " + executeCode.getPath() + args);
                    printErrorCommand(event, ocamlProcess);
                    printResultCommand(event, ocamlProcess);
                    new Thread(() -> {
                        deleteCacheFiles(executeCode, ocamlProcess);
                    }).start();
                } catch (IOException exception) {
                    event.getChannel().sendMessage(exception.toString()).queue();
                    exception.printStackTrace();
                }
            }
        });
    }

    public void deleteCacheFiles(final File executeCode, final Process process) {
        boolean delete = !process.isAlive() && executeCode.delete();
        while (!delete) {
            delete = !process.isAlive() && executeCode.delete();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void printResultCommand(final GuildMessageReceivedEvent event, final Process process) {
        final InputStream stdout = process.getInputStream();
        printCommand(event, stdout, false);
    }

    public void printErrorCommand(final GuildMessageReceivedEvent event, final Process process) {
        final InputStream stdout = process.getErrorStream();
        printCommand(event, stdout, true);
    }

    private void printCommand(final GuildMessageReceivedEvent event, final InputStream stdout, final boolean error) {
        String line;
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8))) {
            try {
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    builder.append(line.replaceAll("tempo/tempo(python|c|ocaml)?_[0-9]{18}_[0-9]{13}.(py|c|ml):", "")).append(System.lineSeparator());
                    Starter.getLogger().log(Level.INFO, line);
                    if (builder.length() > 1900) {
                        if (error)
                            sendError(event, builder);
                        else
                            event.getChannel().sendMessage(builder.toString()).queue();
                        builder = new StringBuilder();
                    }
                }
                if (builder.length() > 0) {
                    if (error)
                        sendError(event, builder);
                    else
                        event.getChannel().sendMessage(builder.toString()).queue();
                }
            } catch(IOException e){
                event.getChannel().sendMessage("Exception in reading output"+ e).queue();
                e.printStackTrace();
            }
        } catch(IOException e){
            event.getChannel().sendMessage("Exception in reading output"+ e).queue();
            e.printStackTrace();
        }
    }

    private void sendError(final GuildMessageReceivedEvent event, final StringBuilder builder) {
        final MessageEmbed embed = new EmbedBuilder()
                .setTitle("Sortie erreur")
                .setColor(Color.RED)
                .setDescription("```" + builder + "```")
                .setTimestamp(Instant.now())
                .setFooter(event.getMember().getEffectiveName(), event.getAuthor().getAvatarUrl())
                .build();
        event.getChannel().sendMessageEmbeds(embed).queue();
    }

}
