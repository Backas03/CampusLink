package moe.vitamin.campuslink.service.certification.result;

import lombok.AllArgsConstructor;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.service.certification.command.EmailCertificationSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Function;

@AllArgsConstructor
public enum EmailCertificationRequestResult {
    PROCESSING_PREVIOUS_REQUEST(interaction -> new EmbedBuilder()
            .setTitle("이메일 인증 요청을 처리중입니다.")
            .setColor(Color.decode("#ff9d00"))
            .setDescription("이전 이메일 인증 요청을 처리중입니다. 잠시만 기다려주세요.")
            .setTimestamp(LocalDateTime.now())
            .setFooter(CampusLink.VERSION)
    ),
    INVALID_EMAIL(interaction -> new EmbedBuilder()
            .setTitle("인증에 실패했습니다.")
            .setColor(Color.RED)
            .setDescription("유효하지 않은 이메일 형식입니다. 대구대학교(@daegu.ac.kr) 이메일을 입력해주세요.")
            .setTimestamp(LocalDateTime.now())
            .setFooter(CampusLink.VERSION)
    ),
    ALREADY_CERTIFIED(interaction -> new EmbedBuilder()
            .setTitle(interaction.getUser().getName() + " 님은 이미 인증이 완료된 상태입니다.")
            .setDescription("인증 정보를 확인하려면 아래 명령어를 입력하세요.")
            .setColor(Color.RED)
            .addField("/인증정보", "이메일 인증 정보를 열람하실 수 있습니다.", false)
            .setTimestamp(LocalDateTime.now())
            .setFooter(CampusLink.VERSION)
    ),
    EMAIL_SEND_FAILED(interaction -> new EmbedBuilder()
            .setTitle("인증에 실패했습니다.")
            .setColor(Color.RED)
            .setDescription("이메일을 전송하는 과정에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
            .setTimestamp(LocalDateTime.now())
            .setFooter(CampusLink.VERSION)
    ),
    CONFIRM_CLEAR_PROGRESS(interaction -> new EmbedBuilder()
            .setTitle("이메일 인증이 진행중입니다")
            .setColor(Color.decode("#ff9d00"))
            .setDescription("""
                                        이메일을 받지 못하였거나 재발급을 원하시면
                                        아래 명령어를 다시 한번 입력해주세요.
                                        
                                        """ +
            "/인증 " + EmailCertificationSlashCommand.COMMAND_ARGUMENT_EMAIL + ":[이메일주소]")
            .setTimestamp(LocalDateTime.now())
            .setFooter(CampusLink.VERSION)),
    SUCCESS(interaction -> new EmbedBuilder()
            .setTitle("대구대학교 인증 확인 (클릭)", "https://outlook.com/daegu.ac.kr")
            .setDescription("해당 메일로 인증 코드를 보내드렸습니다.\n아래 절차를 따라 학교 인증을 완료해주세요.")
            .addField("아래 명령어를 입력하여 학교 인증을 완료합니다.", "/인증 " + EmailCertificationSlashCommand.COMMAND_ARGUMENT_CODE + ":[인증코드]\nex) /인증 " + EmailCertificationSlashCommand.COMMAND_ARGUMENT_CODE + ":1A612C", false)
            .addField("아이디 또는 비밀번호를 잊어버리셨다면?", "[대구대학교 이메일 아이디/비밀번호 찾기](https://office.daegu.ac.kr/Case1/FindPwd.aspx)", false)
            .addField("계정 중복인증 방지를 위해 아래 사진과 같이 개인정보를 수집하고 있습니다.", "해당 개인정보는 이메일 인증일로부터 해당 디스코드 커뮤니티 퇴장일까지 보관되며, 이메일 인증 절차 완료시 해당 동의서에 동의하는 것으로 간주합니다.", false)
            .setTimestamp(LocalDateTime.now())
            .setFooter(CampusLink.VERSION)
            .setImage("attachment://agreement.png")
            .setColor(Color.decode("#9047ff")),

            (builder, hook) -> hook.editOriginalEmbeds(builder.build())
                    .setFiles(FileUpload.fromData(
                            CampusLink.class.getResourceAsStream("/image/agreement.png"),
                            "agreement.png"
                    )).queue()
            );

    @Nullable
    private final Function<CommandInteraction, EmbedBuilder> replyEmbedMessageBuilderFunction;
    @Nullable
    private final BiConsumer<EmbedBuilder, InteractionHook> interactionHookConsumer;

    EmailCertificationRequestResult(@Nullable Function<CommandInteraction, EmbedBuilder> replyEmbedMessageBuilderFunction) {
        this.replyEmbedMessageBuilderFunction = replyEmbedMessageBuilderFunction;
        this.interactionHookConsumer = null;
    }

    public boolean replyEmbeds(CommandInteraction interaction) {
        if (replyEmbedMessageBuilderFunction == null) {
            return false;
        }
        EmbedBuilder builder = replyEmbedMessageBuilderFunction.apply(interaction);
        interaction.getHook().setEphemeral(true)
                .editOriginalEmbeds(builder.build())
                .queue(message -> {
                    if (interactionHookConsumer != null) {
                        interactionHookConsumer.accept(builder, interaction.getHook().setEphemeral(true));
                    }
                });
        return true;
    }

}
