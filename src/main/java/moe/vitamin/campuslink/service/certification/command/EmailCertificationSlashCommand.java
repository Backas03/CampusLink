package moe.vitamin.campuslink.service.certification.command;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.command.api.SlashCommandSource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class EmailCertificationSlashCommand implements SlashCommandSource {

    public static final String COMMAND_ARGUMENT_EMAIL = "email";
    public static final String COMMAND_ARGUMENT_CODE = "code";

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("인증", getDescription())
                .addOption(OptionType.STRING,
                        COMMAND_ARGUMENT_EMAIL,
                        "해당 이메일로 인증코드를 받습니다",
                        false)
                .addOption(OptionType.STRING,
                        COMMAND_ARGUMENT_CODE,
                        "이메일로 받은 인증코드를 입력하여 인증을 완료합니다",
                        false);
    }

    @Override
    public void onTriggered(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        List<OptionMapping> options = event.getOptions();
        if (options.size() != 1) {
            event.reply(getUsage()).queue();
            return;
        }
        OptionMapping option = options.getFirst();
        String optionName = option.getName();
        switch (optionName) {
            case COMMAND_ARGUMENT_EMAIL -> {
                event.deferReply().queue(interactionHook -> {
                    String email = option.getAsString();
                    CampusLink.getInstance()
                            .getEmailCertificationManager()
                            .requestCertification(user, email)
                            .thenAccept(result -> {
                                boolean success = result.replyEmbeds(event);
                                if (!success) {
                                    event.reply("인증 요청 결과: " + result.name()).queue();
                                }
                            });
                });

            }
            case COMMAND_ARGUMENT_CODE -> {
                event.deferReply().queue(interactionHook -> {
                    String code = option.getAsString();
                    CampusLink.getInstance()
                            .getEmailCertificationManager()
                            .verifyCode(user, code)
                            .thenAccept(result -> {
                                log.info("User {} requested email certification verification with code {}, result: {}",
                                        user, code, result);
                                switch (result) {
                                    case SUCCESS -> {
                                        EmbedBuilder builder = new EmbedBuilder()
                                                .setTitle("인증이 완료되었습니다!")
                                                .setDescription("이제부터 학교 인증이 필요한 모든 서비스를 이용하실 수 있습니다.")
                                                .addField("인증 정보 확인 명령어", "/인증정보 - 인증 정보를 열람하실 수 있습니다.", false)
                                                .addField("인증 시간", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), false)
                                                .setColor(Color.decode("#0ee111"))
                                                .setTimestamp(LocalDateTime.now())
                                                .setFooter(CampusLink.VERSION);
                                        interactionHook.editOriginalEmbeds(builder.build()).queue();
                                    }
                                    case INVALID_CODE -> {
                                        EmbedBuilder builder = new EmbedBuilder()
                                                .setColor(Color.decode("#d90000"))
                                                .setTitle("인증에 실패했습니다.")
                                                .addField("인증 코드가 일치하지 않습니다.", "코드를 다시 입력하거나 재인증을 원하시면 /인증 명령어를 다시 입력해주세요.", false)
                                                .setFooter(CampusLink.VERSION);
                                        interactionHook.editOriginalEmbeds(builder.build()).queue();
                                    }
                                    case null, default -> {
                                        EmbedBuilder builder = new EmbedBuilder()
                                                .setColor(Color.decode("#d90000"))
                                                .setTitle("인증에 실패했습니다.")
                                                .addField("인증 과정에서 오류가 발생했습니다.", "잠시 후 다시 시도해주세요.", false)
                                                .setFooter(CampusLink.VERSION);
                                        interactionHook.editOriginalEmbeds(builder.build()).queue();
                                    }
                                }
                            }).exceptionally(e -> {
                                log.error("Failed to verify email certification code for user {}", user.getIdLong(), e);
                                interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                        .setColor(Color.decode("#d90000"))
                                        .setTitle("인증에 실패했습니다.")
                                        .addField("인증 과정에서 오류가 발생했습니다.", "잠시 후 다시 시도해주세요.", false)
                                        .setFooter(CampusLink.VERSION)
                                        .build()).queue();
                                return null;
                            });
                });

            }
            default -> event.reply(getUsage()).queue();
        }
    }

    @Nullable
    @Override
    public String getDescription() {
        return "대구대학교 이메일로 대구대학교 학생 인증을 진행합니다.";
    }

    @NotNull
    @Override
    public String getUsage() {
        return "/인증 " + COMMAND_ARGUMENT_EMAIL + ":[대구대학교 이메일 주소] - 인증코드를 이메일로 발송합니다. \n" +
                "/인증 " + COMMAND_ARGUMENT_CODE + ":[인증 코드] - 인증코드를 입력하여 이메일 인증을 완료합니다.";

    }
}
