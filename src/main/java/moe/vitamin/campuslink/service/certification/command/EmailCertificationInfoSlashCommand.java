package moe.vitamin.campuslink.service.certification.command;

import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.command.api.SlashCommandSource;
import moe.vitamin.campuslink.service.certification.database.EmailCertificationDao;
import moe.vitamin.campuslink.service.certification.database.EmailCertificationData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class EmailCertificationInfoSlashCommand implements SlashCommandSource {
    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("인증정보", getDescription());
    }

    @Override
    public void onTriggered(SlashCommandInteractionEvent event) {
        event.deferReply().queue(hook -> {
            CompletableFuture.runAsync(() -> {
                EmailCertificationData data = EmailCertificationDao.loadCertificationData(event.getUser().getIdLong());
                if (data == null) {
                    hook.editOriginalEmbeds(new EmbedBuilder()
                            .setTitle("인증 정보를 찾을 수 없습니다.")
                            .setDescription("이메일 인증이 완료된 상태에서만 인증 정보를 열람할 수 있습니다.")
                            .setColor(0xFF0000)
                            .setFooter(CampusLink.VERSION)
                            .setTimestamp(LocalDateTime.now())
                            .build()
                    ).queue();
                    return;
                }
                hook.editOriginalEmbeds(new EmbedBuilder()
                        .setTitle("인증 정보")
                        .addField("이메일", data.getEmail(), false)
                        .addField("디스코드 유저 아이디", String.valueOf(data.getDiscordUserId()), false)
                        .addField("인증 날짜", data.getCertifiedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), false)
                        .setColor(0x00FF00)
                        .setFooter(CampusLink.VERSION)
                        .setTimestamp(LocalDateTime.now())
                        .build()
                ).queue();
            });
        });
    }

    @NotNull
    @Override
    public String getDescription() {
        return "인증 정보를 열람합니다. 이메일 인증이 완료된 상태에서만 사용할 수 있습니다.";
    }

    @Nullable
    @Override
    public String getUsage() {
        return "/인증정보";
    }
}
