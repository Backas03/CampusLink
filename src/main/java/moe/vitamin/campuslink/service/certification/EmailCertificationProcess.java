package moe.vitamin.campuslink.service.certification;

import moe.vitamin.campuslink.util.RandomCodeGenerator;
import net.dv8tion.jda.api.entities.User;
import java.util.concurrent.CompletableFuture;
/**
 * 이메일 인증 프로세스를 상태 머신으로 관리
 * 상태 흐름: PENDING_EMAIL_SEND -> WAITING_INPUT -> VERIFYING -> COMPLETED (-> 소멸)
 */
public class EmailCertificationProcess {

    public enum Status {
        PENDING_EMAIL_SEND,
        WAITING_FOR_CODE_INPUT,
        VERIFYING
    }

    private final User user;
    private final String email;

    private String verificationCode;
    private Status status;

    private long emailSendAtMs;

    public EmailCertificationProcess(User user, String email) {
        this.user = user;
        this.email = email;
    }

    public CompletableFuture<Boolean> sendVerificationEmail() {
        if (status != null) {
            return CompletableFuture.completedFuture(false);
        }
        this.status = Status.PENDING_EMAIL_SEND;
        this.verificationCode = RandomCodeGenerator.generate();

        // TODO: send email and handle result
        throw new UnsupportedOperationException("Email sending not implemented yet");
    }

}
