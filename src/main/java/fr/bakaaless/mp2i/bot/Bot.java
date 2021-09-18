package fr.bakaaless.mp2i.bot;

import fr.bakaaless.mp2i.bot.commands.CommandManager;
import fr.bakaaless.mp2i.bot.commands.misc.ExecuteCommand;
import fr.bakaaless.mp2i.starter.Starter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.log4j.Level;

import javax.security.auth.login.LoginException;

public class Bot {

    private static Bot instance;

    public static Bot get() {
        return instance;
    }

    public static Bot generate(final String token) throws LoginException, InterruptedException {
        if (instance == null)
            instance = new Bot(token);
        return instance;
    }

    private final JDA jda;

    private Bot(final String token) throws LoginException, InterruptedException {
        this.jda = JDABuilder
                .createDefault(
                        token,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .enableCache(
                        CacheFlag.VOICE_STATE
                )
                .disableCache(
                        CacheFlag.EMOTE
                )
                .setStatus(OnlineStatus.ONLINE)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(new fr.bakaaless.mp2i.bot.listeners.BotListener())
                .build();
        this.jda.awaitReady();
    }

    public void setupMisc() {
        CommandManager.register("execute", new ExecuteCommand());
    }

    public JDA getJda() {
        return this.jda;
    }

    public void shutdown() {
        try {
            this.jda.getPresence().setStatus(OnlineStatus.OFFLINE);
            this.jda.shutdown();
        } catch (Exception e) {
            Starter.getLogger().log(Level.FATAL, "Can't shutdown properly", e);
        }
    }
}
