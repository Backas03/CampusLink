package moe.vitamin.campuslink.command.api;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface ChatCommandSource extends CommandSource {

    String COMMAND_SPLIT_IDENTIFIER = "\\s+";

    void onTriggered(MessageReceivedEvent event);

    // TODO: add permission supports?

    default String getArgument(String input, int index) {
        String[] split = input.split(COMMAND_SPLIT_IDENTIFIER);
        if (split.length - 1 > index) {
            return split[index + 1];
        }
        return null;
    }

    default String getArgument(MessageReceivedEvent event, int index) {
        return getArgument(event.getMessage().getContentRaw(), index);
    }

    default String getArgument(String input, int start, int end) {
        String[] split = input.split(COMMAND_SPLIT_IDENTIFIER);
        if (split.length - 1 > start) {
            StringBuilder builder = new StringBuilder(split[start + 1]);
            for (int i=start+2; i<end+1; i++) {
                builder.append(" ").append(split[i]);
            }
            return builder.toString();
        }
        return null;
    }

    default String getArgument(MessageReceivedEvent event, int start, int end) {
        return getArgument(event.getMessage().getContentRaw(), start, end);
    }

}
