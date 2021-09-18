package fr.bakaaless.mp2i.starter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.bakaaless.mp2i.bot.Bot;
import jline.console.ConsoleReader;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Starter {

    private static final Logger logger;
    private static final Gson gson;

    static {
        PropertyConfigurator.configure(ClassLoader.getSystemResourceAsStream("log4j.properties"));
        logger = Logger.getLogger("Bot Zoe");
        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .setPrettyPrinting()
                .create();
    }

    private static Thread consoleThread;
    private static Bot bot;

    public static void main(final String... args) throws LoginException, InterruptedException {
        logger.setLevel(Level.ALL);
        final File file = new File("config", "config.json");
        if (!file.exists()) {
            logger.log(Level.WARN, "Config file does not exist. Creating it...");
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                final String json = gson.toJson(Config.get());
                FileUtils.write(file, json, StandardCharsets.UTF_8);
                logger.log(Level.INFO, "Config file created. Shutdown to let you write your token in the config file.");
                System.exit(0);
            } catch (IOException e) {
                logger.log(Level.FATAL, "Can't create config file or write in it.", e);
                System.exit(1);
            }
        } else {
            try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                final StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                final Config config = gson.fromJson(stringBuilder.toString().replace(System.lineSeparator(), ""), Config.class);
                final Config template = Config.createBlank();
                if (!config.getVersion().equalsIgnoreCase(template.getVersion())) {
                    logger.log(Level.WARN, "A new version of the config file has been detected.");
                    final File oldConfig = new File("config", "config.old-" + (new File("config").listFiles().length - 1));
                    if (!oldConfig.exists()) {
                        Files.copy(Path.of(file.toURI()), Path.of(oldConfig.toURI()));
                    }
                    FileUtils.write(file, gson.toJson(template), StandardCharsets.UTF_8);
                    logger.log(Level.INFO, "Config file modified. Please change values of new keys");
                }
                Config.setConfig(config);
                logger.log(Level.INFO, "Config retrieved.");
            } catch (IOException e) {
                logger.log(Level.FATAL, "Can't read config file.", e);
                System.exit(2);
            }
        }
        bot = Bot.generate(Config.get().getToken());
        logger.log(Level.INFO, "Setup misc' commands...");
        bot.setupMisc();

        consoleThread = new Thread(() -> {
            try {
                bot.getJda().awaitReady();
                final ConsoleReader reader = new ConsoleReader("Bot Zoé", System.in, System.out, null);
                String line;
                while (true) {
                    line = reader.readLine("\33[31m> \33[0m");
                    if (line == null)
                        line = "stop";

                    if (line.length() == 0)
                        continue;

                    switch (line.toLowerCase()) {
                        case "quit":
                        case "stop": {
                            logger.log(Level.INFO, "Shutdown...");
                            bot.shutdown();
                            logger.log(Level.INFO, "Goodbye!");
                            System.exit(0);
                            break;
                        }
                        default: {
                            logger.log(Level.INFO, "Unknown command.");
                        }
                    }
                }
            } catch (IOException | InterruptedException exception) {
                exception.printStackTrace();
            }
        });
        consoleThread.setName("ConsoleThread");
        consoleThread.setDaemon(true);
        consoleThread.start();

    }

    public static Logger getLogger() {
        return logger;
    }

    public static Gson getGson() {
        return gson;
    }
}
