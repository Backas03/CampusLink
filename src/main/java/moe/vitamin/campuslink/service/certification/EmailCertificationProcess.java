package moe.vitamin.campuslink.service.certification;

import jakarta.mail.MessagingException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.config.impl.CertificationConfig;
import moe.vitamin.campuslink.service.certification.database.EmailCertificationDao;
import moe.vitamin.campuslink.service.certification.result.EmailCertificationVerificationResult;
import moe.vitamin.campuslink.service.email.EmailService;
import moe.vitamin.campuslink.util.RandomCodeGenerator;
import moe.vitamin.campuslink.util.TimeUtil;
import net.dv8tion.jda.api.entities.User;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EmailCertificationProcess {

    public enum Status {
        PENDING_EMAIL_SEND,
        WAITING_FOR_CODE_INPUT,
        VERIFYING,
        WAITING_TO_FLUSH
    }

    private final User user;
    private final String email;

    private String verificationCode;
    @Getter
    private Status status;

    // 이메일을 다시 보내기 위한 flag, false 일 시 인증 명령어 한번 더 입력하라고 하고 true로 변경,
    // true 일 시 해당 process expire 시키고 새로운 process를 시작함
    @Setter
    @Getter
    private boolean expireFlag;

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
                String fullEmailMessage = certificationConfig.getPlainHTMLMessage()
                        .replace("{code}", verificationCode)
                        .replace("{expire_duration}", TimeUtil
                                .convertMillisecondToHumanReadable(
                                        certificationConfig.getVerificationExpireTimeMs()));

                EmailService.sendEmailAsHTML(email,
                        certificationConfig.getEmailSubject(),
                        fullEmailMessage);

                this.emailSendAt = System.currentTimeMillis();
                this.status = Status.WAITING_FOR_CODE_INPUT;
                return true;
            } catch (MessagingException e) {
                log.error("Failed to send verification email to {} for user {}", email, user.getIdLong(), e);
                return false;
            }
        });
    }

    public CompletableFuture<EmailCertificationVerificationResult> verifyCode(String code, long guildId) {
        if (status != Status.WAITING_FOR_CODE_INPUT) {
            return CompletableFuture.completedFuture(EmailCertificationVerificationResult.NOT_IN_PROGRESS);
        }

        CertificationConfig certificationConfig = CampusLink.getInstance()
                .getConfigManager()
                .getCertificationConfig();

        if (emailSendAt + certificationConfig.getVerificationExpireTimeMs() <= System.currentTimeMillis()) {
            return CompletableFuture.completedFuture(EmailCertificationVerificationResult.EXPIRED);
        }

        this.status = Status.VERIFYING;
        return CompletableFuture.supplyAsync(() -> {
            boolean result = this.verificationCode.equals(code);
            if (!result) {
                this.status = Status.WAITING_FOR_CODE_INPUT;
                return EmailCertificationVerificationResult.INVALID_CODE;
            }
            EmailCertificationDao.insertEmailCertification(email, user.getIdLong(), guildId, LocalDateTime.now());
            // TODO: call event?, give role to user?

            this.status = Status.WAITING_TO_FLUSH;
            return EmailCertificationVerificationResult.SUCCESS;
        }).exceptionally(e -> {
            log.error("Failed to verify email certification code for user {}", user.getIdLong(), e);
            this.status = Status.WAITING_TO_FLUSH;
            return EmailCertificationVerificationResult.INTERNAL_ERROR;
        }).completeOnTimeout(EmailCertificationVerificationResult.TIMEOUT,
                certificationConfig.getProcessTimeoutThreshold(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return "EmailCertificationProcess{" +
                "user=" + user +
                ", email='" + email + '\'' +
                ", verificationCode='" + verificationCode + '\'' +
                ", status=" + status +
                ", expireFlag=" + expireFlag +
                ", emailSendAt=" + emailSendAt +
                '}';
    }
}
