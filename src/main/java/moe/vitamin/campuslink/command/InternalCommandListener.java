package moe.vitamin.campuslink.command;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.command.api.ChatCommandSource;
import moe.vitamin.campuslink.command.api.SlashCommandSource;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Slf4j
public class InternalCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        consumeSlashCommandSourceIfExists(event.getName(),
                source -> {
                    String permissionNode = source.getPermissionNode();
                    if (permissionNode != null && !CampusLink.getInstance().getPermissionManager()
                            .hasPermission(event.getUser(), permissionNode)) {
                        event.replyEmbeds(new EmbedBuilder()
                                .setColor(Color.decode("#d90000"))
                                .setTitle("명령어 실행이 거부되었습니다")
                                .addField("사유", "이 명령어를 실행할 권한이 없습니다.", false)
                                .setFooter(CampusLink.VERSION)
                                .setTimestamp(LocalDateTime.now())
                                .build()).setEphemeral(true).queue();
                        return;
                    }
                    source.onTriggered(event);
                });
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

        String permissionNode = commandSource.getPermissionNode();
        if (permissionNode != null
                && !CampusLink.getInstance().getPermissionManager().hasPermission(event.getAuthor(), permissionNode)) {
            event.getMessage().replyEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#d90000"))
                    .setTitle("명령어 실행이 거부되었습니다")
                    .addField("사유", "이 명령어를 실행할 권한이 없습니다.", false)
                    .setFooter(CampusLink.VERSION)
                    .setTimestamp(LocalDateTime.now())
                    .build()).queue();
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
