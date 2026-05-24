package moe.vitamin.campuslink.command.impl;

import moe.vitamin.campuslink.command.api.SlashCommandSource;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HelpSlashCommand implements SlashCommandSource {
    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("도움말", getDescription());
    }

    @Override
    public void onTriggered(SlashCommandInteractionEvent event) {
        event.reply("테스트에요").queue();
    }

    @NotNull
    @Override
    public String getDescription() {
        return "소라의 도움말을 확인합니다.";
    }

    @Nullable
    @Override
    public String getUsage() {
        return "/도움말";
    }
}
