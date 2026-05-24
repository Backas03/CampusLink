package moe.vitamin.campuslink.command;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.command.api.ChatCommandSource;
import moe.vitamin.campuslink.command.api.SlashCommandSource;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Slf4j
public class InternalCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        consumeSlashCommandSourceIfExists(event.getName(),
                source -> source.onTriggered(event));
        log.info("Slash command triggered. command={}, user={}", event.getFullCommandName(), event.getUser());
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        consumeSlashCommandSourceIfExists(event.getName(),
                source -> source.onCommandAutoCompleteInteractionEvent(event));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        String content = event.getMessage().getContentRaw();
        CommandManager commandManager = CampusLink.getInstance()
                .getSora()
                .getCommandManager();

        String[] contentPreprocessed = content.split(ChatCommandSource.COMMAND_SPLIT_IDENTIFIER);
        if (contentPreprocessed.length < 1) {
            return;
        }
        String commandWithPrefix = contentPreprocessed[0];
        if (commandWithPrefix == null) {
            return;
        }
        ChatCommandSource commandSource = commandManager.getChatCommandSourceWithPrefix(commandWithPrefix);
        if (commandSource == null) {
            return;
        }
        commandSource.onTriggered(event);
        log.info("Chat command triggered. input={}, user={}, command={}", content, event.getAuthor(), commandSource);
    }

    private void consumeSlashCommandSourceIfExists(String command, Consumer<SlashCommandSource> sourceConsumer) {
        SlashCommandSource source = CampusLink.getInstance()
                .getSora()
                .getCommandManager()
                .getSlashCommandSource(command);
        if (source != null) {
            sourceConsumer.accept(source);
        }
    }

}
