package fr.bakaaless.mp2i.bot.commands.misc;

import fr.bakaaless.mp2i.bot.commands.CommandExecutor;
import fr.bakaaless.mp2i.starter.Starter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
                    final Process pythonProcess = Runtime.getRuntime().exec("python3 " + executeCode.getAbsolutePath() + args);
                    printResultCommand(event, pythonProcess);
                    new Thread(() -> {
                        deleteCacheFiles(executeCode, pythonProcess);
                    }).start();
                } catch (IOException exception) {
                    event.getChannel().sendMessage(exception.toString()).queue();
                    event.getChannel().sendMessage(exception.getMessage()).queue();
                    exception.printStackTrace();
                }
            } else if (message.getContentRaw().toLowerCase().startsWith("```c")) {
                final String fileName = "tempoc_" + message.getAuthor().getId() + "_" + System.currentTimeMillis();
                final File executeCode = new File("tempo", fileName + ".c");
                try {
                    executeCode.getParentFile().mkdirs();
                    executeCode.createNewFile();
                    FileUtils.writeByteArrayToFile(executeCode, message.getContentStripped().getBytes(StandardCharsets.UTF_8));
                    final Process cProcess = Runtime.getRuntime().exec("gcc " + executeCode.getAbsolutePath() + " -o " + fileName + ".h");
                    printResultCommand(event, cProcess);
                    new Thread(() -> {
                        deleteCacheFiles(executeCode, cProcess);
                        try {
                            final Process hProcess = Runtime.getRuntime().exec("./" + fileName + ".h" + args);
                            printResultCommand(event, hProcess);
                            new Thread(() -> {
                                final File hFile = new File(fileName + ".h");
                                deleteCacheFiles(hFile, hProcess);

                            }).start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } catch (IOException exception) {
                    event.getChannel().sendMessage(exception.toString()).queue();
                    event.getChannel().sendMessage(exception.getMessage()).queue();
                    exception.printStackTrace();
                }
            } else if (message.getContentRaw().toLowerCase().startsWith("```ocaml")) {
                final String fileName = "tempoocaml_" + message.getAuthor().getId() + "_" + System.currentTimeMillis();
                final File executeCode = new File("tempo", fileName + ".ml");
                try {
                    executeCode.getParentFile().mkdirs();
                    executeCode.createNewFile();
                    FileUtils.writeByteArrayToFile(executeCode, message.getContentStripped().getBytes(StandardCharsets.UTF_8));
                    final Process ocamlProcess = Runtime.getRuntime().exec("ocaml " + executeCode.getAbsolutePath() + args);
                    printResultCommand(event, ocamlProcess);
                    new Thread(() -> {
                        deleteCacheFiles(executeCode, ocamlProcess);
                    }).start();
                } catch (IOException exception) {
                    event.getChannel().sendMessage(exception.toString()).queue();
                    event.getChannel().sendMessage(exception.getMessage()).queue();
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
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8))) {
            try {
                while ((line = reader.readLine()) != null) {
                    event.getChannel().sendMessage(line).queue();
                    Starter.getLogger().log(Level.INFO, line);
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

}
