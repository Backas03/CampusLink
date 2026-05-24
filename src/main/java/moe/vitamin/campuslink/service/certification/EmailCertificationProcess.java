package moe.vitamin.campuslink.service.certification;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.config.impl.CertificationConfig;
import moe.vitamin.campuslink.service.email.EmailService;
import moe.vitamin.campuslink.util.RandomCodeGenerator;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.CompletableFuture;

@Slf4j
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

    private long emailSendAt;

    public EmailCertificationProcess(User user, String email) {
        this.user = user;
        this.email = email;
    }

    public CompletableFuture<Boolean> sendVerificationEmail() {
        if (status != null) {
            // TODO: throw exception
            return CompletableFuture.completedFuture(false);
        }
        this.status = Status.PENDING_EMAIL_SEND;
        this.verificationCode = RandomCodeGenerator.generate();

        CertificationConfig certificationConfig = CampusLink.getInstance()
                .getConfigManager()
                .getCertificationConfig();

        return CompletableFuture.supplyAsync(() -> {
            try {
                EmailService.sendEmailAsHTML(email,
                        certificationConfig.getEmailSubject(),
                        certificationConfig.getPlainHTMLMessage());

                this.emailSendAt = System.currentTimeMillis();
                this.status = Status.WAITING_FOR_CODE_INPUT;
                return true;
            } catch (MessagingException e) {
                log.error("Failed to send verification email to {} for user {}", email, user.getId(), e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> verifyCode(String code) {
        if (status != Status.WAITING_FOR_CODE_INPUT) {
            // TODO: throw exception when status is not WAITING_FOR_CODE_INPUT
            return CompletableFuture.completedFuture(false);
        }

        this.status = Status.VERIFYING;
        return CompletableFuture.supplyAsync(() -> {
            boolean result = this.verificationCode.equals(code);
            if (!result) {
                this.status = Status.WAITING_FOR_CODE_INPUT;

                // TODO: insert db, log, etc for failed attempt
            }
            return result;
        });
    }

}
