package moe.vitamin.campuslink.service.certification;

import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.service.certification.database.EmailCertificationDao;
import moe.vitamin.campuslink.service.certification.database.EmailCertificationData;
import moe.vitamin.campuslink.service.certification.result.EmailCertificationRequestResult;
import moe.vitamin.campuslink.service.certification.result.EmailCertificationVerificationResult;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Slf4j
public class EmailCertificationManager {

    public static EmailCertificationManager init() {
        EmailCertificationDao.init();

        return new EmailCertificationManager();
    }

    private final Pattern emailPattern;
    private final Map<Long, EmailCertificationProcess> certificationProcess;

    private EmailCertificationManager() {
        this.emailPattern = Pattern.compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@daegu\\.ac\\.kr");
        this.certificationProcess = new HashMap<>();
    }

    public boolean isProcessing(long discordUserId) {
        return this.certificationProcess.containsKey(discordUserId);
    }

    public CompletableFuture<EmailCertificationRequestResult> requestCertification(
            User user,
            String email,
            long guildId
    ) {
        if (!isValidEmail(email)) {
            return CompletableFuture.completedFuture(EmailCertificationRequestResult.INVALID_EMAIL);
        }
        if (isProcessing(user.getIdLong())) {
            EmailCertificationProcess process = this.certificationProcess.get(user.getIdLong());
            if (process.getStatus() == EmailCertificationProcess.Status.WAITING_FOR_CODE_INPUT) {
                if (!process.isExpireFlag()) {
                    process.setExpireFlag(true);
                    return CompletableFuture.completedFuture(EmailCertificationRequestResult.CONFIRM_CLEAR_PROGRESS);
                }
                this.certificationProcess.remove(user.getIdLong());
            }
            log.info("{}", this.certificationProcess);
            if (isProcessing(user.getIdLong())) {
                return CompletableFuture.completedFuture(EmailCertificationRequestResult.PROCESSING_PREVIOUS_REQUEST);
            }
        }

        return isCertified(user, guildId).thenApply(certified -> {
            if (certified) {
                return EmailCertificationRequestResult.ALREADY_CERTIFIED;
            }
            EmailCertificationProcess process = new EmailCertificationProcess(user, email);
            this.certificationProcess.put(user.getIdLong(), process);

            Boolean emailSendResult = process.sendVerificationEmail().join();
            if (!emailSendResult) {
                this.certificationProcess.remove(user.getIdLong());
                return EmailCertificationRequestResult.EMAIL_SEND_FAILED;
            }
            return EmailCertificationRequestResult.SUCCESS;
        });
    }

    public CompletableFuture<EmailCertificationVerificationResult> verifyCode(User user, String code, long guildId) {
        EmailCertificationProcess process = this.certificationProcess.get(user.getIdLong());
        if (process == null) {
            return CompletableFuture.completedFuture(EmailCertificationVerificationResult.NOT_IN_PROGRESS);
        }
        return process.verifyCode(code, guildId).thenApply(result -> {
            if (process.getStatus() == EmailCertificationProcess.Status.WAITING_TO_FLUSH
                    || result == EmailCertificationVerificationResult.TIMEOUT
                    || result == EmailCertificationVerificationResult.EXPIRED
                    || result == EmailCertificationVerificationResult.SUCCESS) {
                this.certificationProcess.remove(user.getIdLong());
                log.info("Email certification process for user {} has been removed. process_status={}, verification_result={}",
                        user.getIdLong(), process.getStatus(), result);
            }
            return result;
        });
    }


    public boolean isValidEmail(String email) {
        return emailPattern.matcher(email).matches();
    }

    public CompletableFuture<Boolean> isCertified(User user, long guildId) {
        return CompletableFuture.supplyAsync(() -> {
            EmailCertificationData data = EmailCertificationDao.loadCertificationData(user.getIdLong(), guildId);
            return data != null;
        });
    }

}
