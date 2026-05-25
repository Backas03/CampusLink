package moe.vitamin.campuslink.service.certification;

import moe.vitamin.campuslink.service.certification.database.EmailCertificationDao;
import moe.vitamin.campuslink.service.certification.database.EmailCertificationData;
import moe.vitamin.campuslink.service.certification.result.EmailCertificationRequestResult;
import moe.vitamin.campuslink.service.certification.result.EmailCertificationVerificationResult;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

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

    public CompletableFuture<EmailCertificationRequestResult> requestCertification(
            String email,
            User user
    ) {
        if (!isValidEmail(email)) {
            return CompletableFuture.completedFuture(EmailCertificationRequestResult.INVALID_EMAIL);
        }
        EmailCertificationProcess process = new EmailCertificationProcess(user, email);
        this.certificationProcess.put(user.getIdLong(), process);

        return isCertified(user).thenApply(certified -> {
            if (certified) {
                return EmailCertificationRequestResult.ALREADY_CERTIFIED;
            }
            Boolean emailSendResult = process.sendVerificationEmail().join();
            if (!emailSendResult) {
                this.certificationProcess.remove(user.getIdLong());
                return EmailCertificationRequestResult.FAILED;
            }
            return EmailCertificationRequestResult.SUCCESS;
        });
    }

    public CompletableFuture<EmailCertificationVerificationResult> verifyCode(User user, String code) {
        EmailCertificationProcess process = this.certificationProcess.get(user.getIdLong());
        if (process == null) {
            return CompletableFuture.completedFuture(EmailCertificationVerificationResult.NOT_IN_PROGRESS);
        }
        return process.verifyCode(code).thenApply(result -> {
            if (process.getStatus() == EmailCertificationProcess.Status.WAITING_TO_FLUSH) {
                this.certificationProcess.remove(user.getIdLong());
            }
            return result;
        });
    }

    public boolean isValidEmail(String email) {
        return emailPattern.matcher(email).matches();
    }

    public CompletableFuture<Boolean> isCertified(User user) {
        return CompletableFuture.supplyAsync(() -> {
            EmailCertificationData data = EmailCertificationDao.loadCertificationData(user.getIdLong());
            return data != null;
        });
    }

}
