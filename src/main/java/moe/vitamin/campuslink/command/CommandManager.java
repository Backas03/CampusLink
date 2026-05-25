package moe.vitamin.campuslink.command;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.command.api.ChatCommandSource;
import moe.vitamin.campuslink.command.api.SlashCommandSource;
import moe.vitamin.campuslink.discord.Sora;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
public class CommandManager {

    private final Sora sora;
    private final Map<String, ChatCommandSource> registeredChatCommands;
    private final Map<String, SlashCommandSource> registeredSlashCommands;

    private boolean initialized;

    public CommandManager(Sora sora) {
        this.sora = sora;

        this.registeredChatCommands = new HashMap<>();
        this.registeredSlashCommands = new HashMap<>();
    }

    public void init() {
        if (initialized) {
            throw new IllegalStateException("CommandManager has already been initialized");
        }
        this.sora.getJDA().addEventListener(new InternalCommandListener());
        this.initialized = true;
    }

    public void registerChatCommand(String command, ChatCommandSource source) {
        this.registeredChatCommands.put(command, source);
    }

    public void registerSlashCommand(SlashCommandSource source) {
        SlashCommandData commandData = source.buildCommand();
        this.sora.getJDA()
                .upsertCommand(commandData)
                .queue(command -> {
                    this.registeredSlashCommands.put(commandData.getName(), source);
                    log.info("Slash command registered. command={}, source={}", commandData.getName(), source);
                });
    }

    public ChatCommandSource getChatCommandSource(String command) {
        return registeredChatCommands.get(command);
    }

    public ChatCommandSource getChatCommandSourceWithPrefix(String commandWithPrefix) {
        final String commandPrefix = CampusLink.getInstance()
                .getConfigManager()
                .getSoraConfig()
                .getChatCommandPrefix();
        if (!commandWithPrefix.startsWith(commandPrefix)) {
            return null;
        }
        return getChatCommandSource(commandWithPrefix.substring(commandPrefix.length()));
    }

    public boolean hasChatCommandSource(String command) {
        return registeredChatCommands.containsKey(command);
    }

    public SlashCommandSource getSlashCommandSource(String command) {
        return registeredSlashCommands.get(command);
    }
}
